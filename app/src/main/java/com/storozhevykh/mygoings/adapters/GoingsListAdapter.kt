package com.storozhevykh.mygoings.adapters

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.storozhevykh.mygoings.App
import com.storozhevykh.mygoings.Constants
import com.storozhevykh.mygoings.R
import com.storozhevykh.mygoings.R.*
import com.storozhevykh.mygoings.database.DataBase
import com.storozhevykh.mygoings.database.GoingDao
import com.storozhevykh.mygoings.firebase.SyncHelper
import com.storozhevykh.mygoings.model.Going
import com.storozhevykh.mygoings.model.StaticStorage
import com.storozhevykh.mygoings.services.NotificationService
import com.storozhevykh.mygoings.view.AddingGoingDialog
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.time.Duration

class GoingsListAdapter(goingsList: List<Going>, val pagePosition: Int, val activity: FragmentActivity?): RecyclerView.Adapter<GoingsListAdapter.ViewHolder>() {

    @Inject
    lateinit var dataBase: DataBase
    var goingDAO: GoingDao

    @Inject
    lateinit var syncHelper: SyncHelper

    private var goings = ArrayList<Going>(goingsList)
    val sPref: SharedPreferences? = activity?.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
    val theme = sPref?.getString(Constants.PREF_THEME_KEY, Constants.THEME_POSITIVE)
    val nm: NotificationManager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        App.component.inject(this)
        goingDAO = dataBase.goingDao()
        StaticStorage.goingsList = goingsList
        StaticStorage.currentGoingsListAdapter = this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view: View
        if (theme.equals(Constants.THEME_BUSINESS))
            view = LayoutInflater.from(parent.context).inflate(layout.going_item_business, parent, false)
        else if (theme.equals(Constants.THEME_BLACK_GOLD))
            view = LayoutInflater.from(parent.context).inflate(layout.going_item_black_gold, parent, false)
        else
            view = LayoutInflater.from(parent.context).inflate(layout.going_item, parent, false)

        val title: TextView = view.findViewById(id.cardTitle)
        val expandImg: ImageView = view.findViewById(id.cardExpand)
        val editImg: ImageView = view.findViewById(id.cardEdit)
        val desc: TextView = view.findViewById(id.cardDescription)

        expandImg.setOnClickListener {
            if (desc.visibility == View.GONE) {
                desc.visibility = View.VISIBLE
                expandImg.setImageResource(drawable.arrow_drop_up_24)
            }
            else if (desc.visibility == View.VISIBLE) {
                desc.visibility = View.GONE
                expandImg.setImageResource(drawable.arrow_drop_down_24)
            }
        }

        val viewHolder = ViewHolder(view, theme)

        editImg.setOnClickListener {
            val dialog = AddingGoingDialog.newInstance(title.text.toString(), Constants.ADDING_MODE_EDIT, viewHolder.pos, pagePosition)
            activity?.supportFragmentManager?.let { it1 -> dialog.show(it1, "TAG") }
            Log.d("mylog", "Edit going")
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val going = goings.get(position)
        holder.goingMenu.setOnClickListener {
            val menu: PopupMenu = PopupMenu(activity?.baseContext, holder.goingMenu)
            menu.inflate(R.menu.menu_going)

            if (going.state == Constants.GOING_STATE_DONE)
                menu.menu.findItem(R.id.going_menu_done).setTitle("UNDONE")
            if (going.state == Constants.GOING_STATE_SKIPPED)
                menu.menu.findItem(R.id.going_menu_skip).setTitle("RESTORE")

            menu.show()
            menu.setOnMenuItemClickListener {

                nm.cancel(going.hashCode())

                when (it.itemId) {
                    id.going_menu_done -> {
                        if (going.state != Constants.GOING_STATE_DONE)
                            going.state = Constants.GOING_STATE_DONE
                        else
                            going.state = Constants.GOING_STATE_ACTIVE
                        goings[position] = going
                        notifyItemChanged(position)
                        CoroutineScope(Dispatchers.Main).launch {
                            goingDAO.update(going)
                            moveUtil(position)
                            updateInCloud(going, Constants.MODE_UPDATE)
                        }
                    }
                    id.going_menu_skip -> {
                        if (going.state != Constants.GOING_STATE_SKIPPED)
                            going.state = Constants.GOING_STATE_SKIPPED
                        else
                            going.state = Constants.GOING_STATE_ACTIVE
                        goings[position] = going
                        notifyItemChanged(position)
                        CoroutineScope(Dispatchers.Main).launch {
                            goingDAO.update(going)
                            moveUtil(position)
                            updateInCloud(going, Constants.MODE_UPDATE)
                        }
                    }
                    id.going_menu_delete -> {
                        goings.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, goings.size)
                        CoroutineScope(Dispatchers.Main).launch {
                            goingDAO.delete(going)
                            updateInCloud(going, Constants.MODE_REMOVE)
                        }
                    }
                }
                ViewPagerAdapter.updatePages(pagePosition)
                StaticStorage.goingsList = goings
                activity?.startService(Intent(activity, NotificationService::class.java))
                return@setOnMenuItemClickListener true
            }
        }

        holder.bind(position, going)
    }

    private suspend fun moveUtil(position: Int) {
        delay(1000L)
        if (goings[position].state == Constants.GOING_STATE_DONE || goings[position].state == Constants.GOING_STATE_SKIPPED)
            Toast.makeText(activity?.applicationContext, "The going ${goings[position].title} is moved to archive. You can find it in the Past goings", Toast.LENGTH_LONG).show()
        else
            Toast.makeText(activity?.applicationContext, "The going ${goings[position].title} is removed from archive. You can find it in the Active goings", Toast.LENGTH_LONG).show()

        goings.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, goings.size)
    }

    override fun getItemCount(): Int {
        return goings.size
    }

    fun updateList(newList: List<Going>) {
        goings = ArrayList<Going>(newList)
        StaticStorage.goingsList = newList
        notifyDataSetChanged()
    }

    private fun updateInCloud(going: Going, mode: Int) {
        val user = Firebase.auth.currentUser
        if (user != null) {
            if (mode == Constants.MODE_UPDATE)
                App.fireDataBase.child(StaticStorage.userEmail!!).child(going.timeCreated.toString()).setValue(going)
            else if (mode == Constants.MODE_REMOVE)
                App.fireDataBase.child(StaticStorage.userEmail!!).child(going.timeCreated.toString()).removeValue()
        }
    }

    class ViewHolder(itemView: View, val theme: String?) : RecyclerView.ViewHolder(itemView) {
        //val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val sdf: SimpleDateFormat = SimpleDateFormat(Constants.DATE_FORMAT)
        private val categoryIcon: ImageView = itemView.findViewById(id.category_icon)
        val goingMenu: TextView = itemView.findViewById(id.goingMenu)
        var pos = 0

        fun bind(position: Int, going: Going) {
            pos = position
            val text: TextView = itemView.findViewById(id.cardTitle)
            val desc: TextView = itemView.findViewById(id.cardDescription)
            val expandImg: ImageView = itemView.findViewById(id.cardExpand)
            val expiredText: TextView = itemView.findViewById(id.cardExpired)
            val doneSkippedText: TextView = itemView.findViewById(id.cardDoneSkipped)
            text.text = going.title
            desc.text = going.text
            if (going.text.isEmpty())
                expandImg.visibility = View.GONE
            if (going.timeElapsed < Date().time && going.timeElapsed > 0)
                expiredText.visibility = View.VISIBLE
            else
                expiredText.visibility = View.GONE

            if (going.state == Constants.GOING_STATE_DONE) {
                doneSkippedText.text = "DONE"
                if (theme.equals(Constants.THEME_BLACK_GOLD))
                    doneSkippedText.setTextColor(ContextCompat.getColor(itemView.context, color.doneOnBlack))
                else
                    doneSkippedText.setTextColor(ContextCompat.getColor(itemView.context, color.dialogTextLow))
                doneSkippedText.visibility = View.VISIBLE
            }
            else if (going.state == Constants.GOING_STATE_SKIPPED) {
                doneSkippedText.text = "SKIPPED"
                if (theme.equals(Constants.THEME_BLACK_GOLD))
                    doneSkippedText.setTextColor(ContextCompat.getColor(itemView.context, color.skippedOnBlack))
                else
                    doneSkippedText.setTextColor(ContextCompat.getColor(itemView.context, color.dialogTextHigh))
                doneSkippedText.visibility = View.VISIBLE
            }
            else if (going.state == Constants.GOING_STATE_ACTIVE)
                doneSkippedText.visibility = View.GONE

            val deadline: TextView = itemView.findViewById(id.cardDeadline)
            if (going.timeElapsed == Long.MAX_VALUE)
                deadline.text = itemView.context.getString(string.adding_dialog_deadline_time)
            else
                deadline.text = sdf.format(going.timeElapsed)

            val priorityHeader: ImageView = itemView.findViewById(id.priorityHeader)
            val cardView: MaterialCardView = itemView.findViewById(id.cardView)

            if (going.priority == 0) {
                if (theme.equals(Constants.THEME_BLACK_GOLD))
                    priorityHeader.setImageResource(drawable.going_card_background_low_black)
                else
                    priorityHeader.setImageResource(drawable.going_card_background_low)
                if (theme.equals(Constants.THEME_POSITIVE))
                cardView.setStrokeColor(ContextCompat.getColor(cardView.context, color.priorityLow))
            }
            if (going.priority == 1) {
                if (theme.equals(Constants.THEME_BLACK_GOLD))
                    priorityHeader.setImageResource(drawable.going_card_background_medium_black)
                else
                    priorityHeader.setImageResource(drawable.going_card_background_medium)
                if (theme.equals(Constants.THEME_POSITIVE))
                cardView.setStrokeColor(ContextCompat.getColor(cardView.context, color.priorityMedium))
            }
            if (going.priority == 2) {
                if (theme.equals(Constants.THEME_BLACK_GOLD))
                    priorityHeader.setImageResource(drawable.going_card_background_high_black)
                else
                    priorityHeader.setImageResource(drawable.going_card_background_high)
                if (theme.equals(Constants.THEME_POSITIVE))
                cardView.setStrokeColor(ContextCompat.getColor(cardView.context, color.priorityHigh))
            }

            categoryIcon.visibility = View.VISIBLE
            when (going.category) {
                Constants.CATEGORY_NO_CATEGORY -> categoryIcon.visibility = View.GONE
                Constants.CATEGORY_SPORT -> categoryIcon.setImageResource(drawable.icon_sport)
                Constants.CATEGORY_HOME -> categoryIcon.setImageResource(drawable.icon_home)
                Constants.CATEGORY_WORK -> categoryIcon.setImageResource(drawable.icon_work)
                Constants.CATEGORY_FAMILY -> categoryIcon.setImageResource(drawable.icon_family)
            }

            //cardView.setBackgroundResource(R.drawable.going_card_background_low)
        }
    }

}