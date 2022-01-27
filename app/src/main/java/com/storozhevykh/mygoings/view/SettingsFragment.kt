package com.storozhevykh.mygoings.view

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.storozhevykh.mygoings.App
import com.storozhevykh.mygoings.Constants
import com.storozhevykh.mygoings.R
import com.storozhevykh.mygoings.database.DataBase
import com.storozhevykh.mygoings.database.GoingDao
import com.storozhevykh.mygoings.databinding.FragmentSettingsBinding
import com.storozhevykh.mygoings.firebase.SyncHelper
import com.storozhevykh.mygoings.model.Going
import com.storozhevykh.mygoings.model.StaticStorage
import com.storozhevykh.mygoings.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.random.Random


class SettingsFragment : Fragment(), View.OnClickListener {

    @Inject
    lateinit var dataBase: DataBase
    lateinit var goingDAO: GoingDao
    @Inject
    lateinit var syncHelper: SyncHelper

    lateinit var binding: FragmentSettingsBinding

    var themeId = 0
    private var sPref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App.component.inject(this)
        goingDAO = dataBase.goingDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sPref = activity?.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        binding = FragmentSettingsBinding.inflate(layoutInflater)

        binding.clearDB.setOnClickListener(this)
        binding.mockDB.setOnClickListener(this)
        binding.btnChangeTheme.setOnClickListener(this)
        val themeAdapter = commonSpinnerAdapter(resources.getStringArray(R.array.theme_menu))
        binding.themeSpinner.setAdapter(themeAdapter)
        when (sPref?.getString(Constants.PREF_THEME_KEY, Constants.THEME_POSITIVE)) {
            Constants.THEME_POSITIVE -> binding.themeSpinner.setSelection(0)
            Constants.THEME_BUSINESS -> binding.themeSpinner.setSelection(1)
            Constants.THEME_BLACK_GOLD -> {
                binding.themeSpinner.setSelection(2)
                //binding.btnChangeTheme.setBackgroundColor(resources.getColor(R.color.blackGold_primaryVariant))
            }
        }
        binding.themeSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> themeId = R.style.PositiveTheme
                    1 -> themeId = R.style.BusinessTheme
                    2 -> themeId = R.style.BlackGoldTheme
                }

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return binding.root
    }

    fun commonSpinnerAdapter (strArr: Array<String>): ArrayAdapter<String> {
        return object: ArrayAdapter<String>(requireActivity().applicationContext, R.layout.theme_spinner, strArr) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view:TextView = super.getDropDownView(position, convertView, parent) as TextView
                val curTheme: Resources.Theme = getResources().newTheme()
                val typedValue = TypedValue()
                when (position) {
                    0 -> {
                        view.setTextColor(resources.getColor(R.color.purple_700))
                        curTheme.applyStyle(R.style.PositiveTheme, true)
                        curTheme.resolveAttribute(R.attr.colorPrimaryVariant, typedValue, true)
                        view.setBackgroundColor(resources.getColor(typedValue.resourceId))
                    }
                    1 -> {
                        view.setTextColor(resources.getColor(R.color.textColorBusiness_spinner))
                        view.setBackgroundColor(resources.getColor(R.color.business_primaryVariant))
                    }
                    2 -> {
                        view.setTextColor(resources.getColor(R.color.textColorGold_spinner))
                        view.setBackgroundColor(resources.getColor(R.color.blackGold_primaryVariant))
                    }
                }
                return view
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view:TextView = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(resources.getColor(R.color.black))
                return view
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SettingsFragment()
    }

    override fun onClick(v: View?) {
        Log.d("mylog", "onClick")
        when (v?.id) {
            R.id.clearDB -> {
                Log.d("mylog", "clearDB: v?.id = ${v.id}")
                CoroutineScope(Dispatchers.Main).launch {
                    goingDAO.deleteAll()
                    clearFirebase()
                    Toast.makeText(activity, "The local database was cleared", Toast.LENGTH_SHORT).show()
                    Log.d("mylog", "clearing database")
                }
            }
            R.id.mockDB -> {
                Log.d("mylog", "mockDB: v?.id = ${v.id}")
                CoroutineScope(Dispatchers.Main).launch {
                    val goingsList = createMockList()
                    for (i in 0 until goingsList.size) {
                        Log.d("mylog", "Inserting going $i: ${goingsList[i].title}")
                        goingDAO.insert(goingsList[i])
                    }
                    if (Firebase.auth.currentUser != null) {
                        syncHelper.unregisterListener()
                        for (i in 0 until goingsList.size) {
                            App.fireDataBase.child(StaticStorage.userEmail!!).child((goingsList[i].timeCreated).toString()).setValue(goingsList[i])
                        }
                        syncHelper.registerListener()
                    }
                }
                Toast.makeText(activity, "The mock list of goings was created", Toast.LENGTH_SHORT).show()
            }
            R.id.btn_changeTheme -> {
                val editor = sPref?.edit()
                editor?.remove(Constants.PREF_THEME_KEY)
                if (themeId == R.style.PositiveTheme)
                    editor?.putString(Constants.PREF_THEME_KEY, Constants.THEME_POSITIVE)
                else if (themeId == R.style.BusinessTheme)
                    editor?.putString(Constants.PREF_THEME_KEY, Constants.THEME_BUSINESS)
                else if (themeId == R.style.BlackGoldTheme)
                    editor?.putString(Constants.PREF_THEME_KEY, Constants.THEME_BLACK_GOLD)

                editor?.apply()
                StaticStorage.startShowMode = Constants.SHOW_MODE_SETTINGS
                activity?.recreate()
            }
        }
    }

    suspend fun createMockList(): List<Going> {
        val list: ArrayList<Going> = ArrayList()
        val text = "Many useful information about the going. Words words words words words words words words words words..."
        val categoryArray = resources.getStringArray(R.array.category_menu)
        var category: String
        var priority: Int
        var timeElapsed: Long

        for (i in 1..20) {
            category = categoryArray[Random.nextInt(0, categoryArray.size)]
            priority = Random.nextInt(0, 3)
            timeElapsed = Random.nextLong(Date().time + 3600 * 1000, Date().time + 3600 * 1000 * 500)
            Log.d("mylog", "Going title $i is added")
            val curGoing = Going("Going title $i", text, category, priority, Date().time + i, timeElapsed, Constants.GOING_STATE_ACTIVE)
            list.add(curGoing)
        }
        return list
    }

    suspend fun clearFirebase() {
        if (Firebase.auth.currentUser != null) {
            val firebase = App.fireDataBase.child(StaticStorage.userEmail!!)
            firebase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    firebase.removeValue()
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error when reading data from firedatabase")
                }
            })
        }
    }
}