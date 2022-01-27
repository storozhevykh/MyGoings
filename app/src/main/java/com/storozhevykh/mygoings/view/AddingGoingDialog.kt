package com.storozhevykh.mygoings.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.storozhevykh.mygoings.App
import com.storozhevykh.mygoings.Constants
import com.storozhevykh.mygoings.R
import com.storozhevykh.mygoings.adapters.ViewPagerAdapter
import com.storozhevykh.mygoings.dagger.RoomModule
import com.storozhevykh.mygoings.database.DataBase
import com.storozhevykh.mygoings.database.GoingDao
import com.storozhevykh.mygoings.databinding.DialogAddingGoingBinding
import com.storozhevykh.mygoings.model.Going
import com.storozhevykh.mygoings.model.StaticStorage
import com.storozhevykh.mygoings.services.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class AddingGoingDialog() : DialogFragment(), View.OnClickListener {

    @Inject
    lateinit var dataBase: DataBase

    lateinit var goingDAO: GoingDao

    lateinit var binding: DialogAddingGoingBinding

    private lateinit var timeListener: TimePickerDialog.OnTimeSetListener
    private lateinit var dateListener: DatePickerDialog.OnDateSetListener
    private lateinit var spinnerAdapter: ArrayAdapter<CharSequence>
    private var calendar: Calendar = Calendar.getInstance()
    private val sdf: SimpleDateFormat = SimpleDateFormat(Constants.DATE_FORMAT)
    private var timeChanged = false

    private var title = "New going"
    private var addingMode = Constants.ADDING_MODE_NEW
    private var pos = -1
    private var pagePosition = -1

    private var priority = Constants.PRIORITY_LOW
    var firstStart = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        dataBase = RoomModule().provideDatabase()
        goingDAO = dataBase.goingDao()

        arguments?.let {
            title = it.getString(TITLE)!!
            addingMode = it.getInt(ADDING_MODE)
            pos = it.getInt(POS)
            pagePosition = it.getInt(PAGE_POS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DialogAddingGoingBinding.inflate(layoutInflater)
        binding.root.background = resources.getDrawable(R.drawable.dialog_add_background_low)

        binding.addingGoingTitle.text = title
        binding.dialogBtnAdd.setBackgroundResource(R.drawable.dialog_btn_background_low)
        binding.dialogBtnCancel.setBackgroundResource(R.drawable.dialog_btn_background_low)
        changeTextColor(R.color.dialogTextLow)

        spinnerAdapter = ArrayAdapter.createFromResource(binding.root.context, R.array.deadline_menu, R.layout.deadline_spinner_item)
        binding.deadlineSpinner.adapter = spinnerAdapter

        spinnerAdapter = ArrayAdapter.createFromResource(binding.root.context, R.array.category_menu, R.layout.deadline_spinner_item)
        binding.categorySpinner.adapter = spinnerAdapter

        if (addingMode == Constants.ADDING_MODE_EDIT) {
            binding.dialogBtnAdd.text = getString(R.string.btn_save_text)
            val editGoing = StaticStorage.pageFragmentsList[pagePosition].filteredGoings[pos]
            binding.goingTitleEdit.setText(editGoing.title)
            binding.goingDescEdit.setText(editGoing.text)
            binding.dialogDeadline.text = sdf.format(editGoing.timeElapsed)
            val categoryArray = activity?.resources?.getStringArray(R.array.category_menu)
            categoryArray?.indexOf(editGoing.category)?.let { binding.categorySpinner.setSelection(it) }
            if (editGoing.timeElapsed < Long.MAX_VALUE) {
                calendar.time = Date(editGoing.timeElapsed)
                binding.deadlineSpinner.setSelection(3)
            }
            when (editGoing.priority) {
                Constants.PRIORITY_LOW -> {
                    settingPriority(R.id.priority_btn_low)
                    changeTextColor(R.color.dialogTextLow)
                    binding.priororyRadioGroup.check(R.id.priority_btn_low)
                }
                Constants.PRIORITY_MEDIUM -> {
                    settingPriority(R.id.priority_btn_medium)
                    changeTextColor(R.color.dialogTextMedium)
                    binding.priororyRadioGroup.check(R.id.priority_btn_medium)
                }
                Constants.PRIORITY_HIGH -> {
                    settingPriority(R.id.priority_btn_high)
                    changeTextColor(R.color.dialogTextHigh)
                    binding.priororyRadioGroup.check(R.id.priority_btn_high)
                }
            }
        }

        timeListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            if (calendar.timeInMillis < Calendar.getInstance().timeInMillis + 1000 * 600)
                //chooseTime(fragmentView.context)
                    binding.expiredText.visibility = View.VISIBLE
            else binding.expiredText.visibility = View.GONE
            binding.dialogDeadline.text = sdf.format(calendar.time)
            timeChanged = true
        }
        dateListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }

        binding.dialogBtnCancel.setOnClickListener(this)
        binding.dialogBtnAdd.setOnClickListener(this)
        binding.priororyRadioGroup.setOnCheckedChangeListener { group, checkedId -> settingPriority(checkedId) }

        binding.deadlineSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position) {
                    0 -> {
                        binding.dialogDeadline.text = "no deadline"
                        calendar.timeInMillis = Long.MAX_VALUE
                        timeChanged = false
                    }
                    1 -> {
                        calendar = Calendar.getInstance()
                        changeTime(calendar, 1)
                        binding.dialogDeadline.text = sdf.format(calendar.time)
                        timeChanged = true
                    }
                    2 -> {
                        calendar = Calendar.getInstance()
                        changeTime(calendar, 2)
                        binding.dialogDeadline.text = sdf.format(calendar.time)
                        timeChanged = true
                    }
                    3 -> {
                        if (addingMode != Constants.ADDING_MODE_EDIT || !firstStart)
                        chooseTime(binding.root.context)
                        firstStart = false
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                if (addingMode == Constants.ADDING_MODE_NEW)
                    binding.dialogDeadline.text = "No deadline"
            }
        }

        binding.categorySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        binding.dialogDeadline.setOnClickListener { chooseTime(binding.root.context) }

        return binding.root
    }

    private fun settingPriority(checkedId: Int) {
        when (checkedId) {
            R.id.priority_btn_low -> {
                priority = Constants.PRIORITY_LOW
                with(binding) {
                    priorityBtnLow.alpha = 1F
                    priorityBtnMedium.alpha = 0.5F
                    priorityBtnHigh.alpha = 0.5F
                    root.background = resources.getDrawable(R.drawable.dialog_add_background_low)
                    dialogBtnAdd.setBackgroundResource(R.drawable.dialog_btn_background_low)
                    dialogBtnCancel.setBackgroundResource(R.drawable.dialog_btn_background_low)
                }
                changeTextColor(R.color.dialogTextLow)
            }
            R.id.priority_btn_medium -> {
                priority = Constants.PRIORITY_MEDIUM
                with(binding) {
                    priorityBtnLow.alpha = 0.5F
                    priorityBtnMedium.alpha = 1F
                    priorityBtnHigh.alpha = 0.5F
                    root.background = resources.getDrawable(R.drawable.dialog_add_background_medium)
                    dialogBtnAdd.setBackgroundResource(R.drawable.dialog_btn_background_medium)
                    dialogBtnCancel.setBackgroundResource(R.drawable.dialog_btn_background_medium)
                }
                changeTextColor(R.color.dialogTextMedium)
            }
            R.id.priority_btn_high -> {
                priority = Constants.PRIORITY_HIGH
                with(binding) {
                    priorityBtnLow.alpha = 0.5F
                    priorityBtnMedium.alpha = 0.5F
                    priorityBtnHigh.alpha = 1F
                    root.background = resources.getDrawable(R.drawable.dialog_add_background_high)
                    dialogBtnAdd.setBackgroundResource(R.drawable.dialog_btn_background_high)
                    dialogBtnCancel.setBackgroundResource(R.drawable.dialog_btn_background_high)
                }
                changeTextColor(R.color.dialogTextHigh)
            }
        }
    }

    private fun changeTextColor(dialogTextColor: Int) {
        val color = resources.getColor(dialogTextColor)
        with(binding) {
            addingGoingTitle.setTextColor(color)
            titleTV.setTextColor(color)
            descTV.setTextColor(color)
            priorityTV.setTextColor(color)
            categoryTV.setTextColor(color)
            deadlineTV.setTextColor(color)
            dialogBtnAdd.setTextColor(color)
            dialogBtnCancel.setTextColor(color)
        }
    }

    fun chooseTime(context: Context) {
        val tempCalendar = calendar
        if (tempCalendar.timeInMillis == Long.MAX_VALUE)
            tempCalendar.timeInMillis = System.currentTimeMillis()

        val timeDialog = TimePickerDialog(context, timeListener, tempCalendar.get(Calendar.HOUR_OF_DAY), tempCalendar.get(Calendar.MINUTE), true)
        timeDialog.show()
        val dateDialog = DatePickerDialog(context, dateListener, tempCalendar.get(Calendar.YEAR), tempCalendar.get(Calendar.MONTH), tempCalendar.get(Calendar.DAY_OF_MONTH))
        dateDialog.show()
        dateDialog.setOnCancelListener { timeDialog.cancel() }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        /*val recyclerAdapter = StaticStorage.recyclerView.adapter as GoingsListAdapter
        CoroutineScope(Dispatchers.Main).launch {
            recyclerAdapter.updateList(goingDAO.getAll())
        }*/
        ViewPagerAdapter.updateCategory(StaticStorage.categoryFiltering)
        Log.d("mylog", "notifyDataSetChanged")
    }

    companion object {

        private val TITLE = "TITLE"
        private val ADDING_MODE = "ADDING_MODE"
        private val POS = "POS"
        private val PAGE_POS = "PAGE_POS"

        @JvmStatic
        fun newInstance(title: String = "New going", addingMode: Int = Constants.ADDING_MODE_NEW, pos: Int = -1, pagePos: Int = -1) : AddingGoingDialog {
            val dialog = AddingGoingDialog()
            dialog.arguments = bundleOf(TITLE to title, ADDING_MODE to addingMode, POS to pos, PAGE_POS to pagePos)
            return dialog
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.dialog_btn_add -> CoroutineScope (Dispatchers.Main).launch { addGoing() }
            R.id.dialog_btn_cancel -> dismiss()
        }
    }

    private fun changeTime(calendar: Calendar, addDay: Int) {
        calendar.add(Calendar.DAY_OF_MONTH, addDay);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private suspend fun addGoing() {
        if (!timeChanged && addingMode != Constants.ADDING_MODE_EDIT)
            calendar.timeInMillis = Long.MAX_VALUE

        var timeCreated = Date().time
        if (addingMode == Constants.ADDING_MODE_EDIT)
            timeCreated = StaticStorage.pageFragmentsList[pagePosition].filteredGoings[pos].timeCreated

        val going = Going(binding.goingTitleEdit.text.toString(), binding.goingDescEdit.text.toString(),
            binding.categorySpinner.selectedItem as String, priority, timeCreated, calendar.timeInMillis, Constants.GOING_STATE_ACTIVE)
        Log.d("mylog", going.toString())

        if (addingMode == Constants.ADDING_MODE_NEW) {
            goingDAO.insert(going)
            insertToCloud(timeCreated, going)
        }
        else if (addingMode == Constants.ADDING_MODE_EDIT) {
            goingDAO.update(going)
            insertToCloud(timeCreated, going)
        }

        requireActivity().startService(Intent(activity, NotificationService::class.java))

        Log.d("mylog", "From dialog: ${goingDAO.getAll().size}")
        val list: ArrayList<Going> = goingDAO.getAll() as ArrayList<Going>
        for (i in 0 until list.size) {
            if (list[i].title.isEmpty())
                Log.d("mylog", "From dialog: Empty title")
            Log.d("mylog", "From dialog: ${list[i].title}")
        }
        dismiss()
    }

    private fun insertToCloud(timeCreated: Long, going: Going) {
        val user = Firebase.auth.currentUser
        if (user != null)
            App.fireDataBase.child(StaticStorage.userEmail!!).child(timeCreated.toString()).setValue(going)
    }
}