package com.storozhevykh.mygoings.view

import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.storozhevykh.mygoings.R
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val PRIORITY_LOW = 0
private const val PRIORITY_MEDIUM = 1
private const val PRIORITY_HIGH = 2
private const val DATE_FORMAT = "dd MMM yyyy HH:mm"

/**
 * A simple [Fragment] subclass.
 * Use the [AddingGoingDialog.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddingGoingDialog : DialogFragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var btnCancel: TextView
    private lateinit var btnAdd : TextView
    private lateinit var deadlineTime : TextView
    private lateinit var radioGroup : RadioGroup
    private lateinit var radioLow : RadioButton
    private lateinit var radioMedium : RadioButton
    private lateinit var radioHigh : RadioButton
    private lateinit var deadlineSpinner : Spinner
    private var calendar: Calendar = Calendar.getInstance()
    private val sdf: SimpleDateFormat = SimpleDateFormat(DATE_FORMAT)

    private var priority = PRIORITY_LOW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.dialog_adding_going, container, false)

        btnCancel = fragmentView.findViewById(R.id.dialog_btn_cancel)
        btnAdd = fragmentView.findViewById(R.id.dialog_btn_add)
        deadlineTime = fragmentView.findViewById(R.id.dialog_deadline)
        radioGroup = fragmentView.findViewById(R.id.priororyRadioGroup)
        radioLow = fragmentView.findViewById(R.id.priority_btn_low)
        radioMedium = fragmentView.findViewById(R.id.priority_btn_medium)
        radioHigh = fragmentView.findViewById(R.id.priority_btn_high)
        deadlineSpinner = fragmentView.findViewById(R.id.deadlineSpinner)
        val spinnerAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(fragmentView.context, R.array.deadline_menu, R.layout.deadline_spinner_item)
        deadlineSpinner.adapter = spinnerAdapter

        btnCancel.setOnClickListener(this)
        btnAdd.setOnClickListener(this)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.priority_btn_low -> {
                        priority = PRIORITY_LOW
                        radioLow.alpha = 1F
                        radioMedium.alpha = 0.5F
                        radioHigh.alpha = 0.5F
                        fragmentView.background = resources.getDrawable(R.drawable.dialog_add_background_low)
                    }
                    R.id.priority_btn_medium -> {
                        priority = PRIORITY_MEDIUM
                        radioLow.alpha = 0.5F
                        radioMedium.alpha = 1F
                        radioHigh.alpha = 0.5F
                        fragmentView.background = resources.getDrawable(R.drawable.dialog_add_background_medium)
                    }
                    R.id.priority_btn_high -> {
                        priority = PRIORITY_HIGH
                        radioLow.alpha = 0.5F
                        radioMedium.alpha = 0.5F
                        radioHigh.alpha = 1F
                        fragmentView.background = resources.getDrawable(R.drawable.dialog_add_background_high)
                    }
                }
        }

        deadlineSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position) {
                    0 ->
                        deadlineTime.text = "no deadline"
                    1 -> {
                        calendar = Calendar.getInstance()
                        changeTime(calendar, 1)
                        deadlineTime.text = sdf.format(calendar.time)
                        //deadlineTime.text = DateFormat.format(DATE_FORMAT,calendar.time)
                    }
                    2 -> {
                        calendar = Calendar.getInstance()
                        changeTime(calendar, 2)
                        deadlineTime.text = sdf.format(calendar.time)
                    }
                    3 -> deadlineTime.text = "soon"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                deadlineTime.text = "No deadline"
            }
        }
        return fragmentView
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddingGoingDialog.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddingGoingDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.dialog_btn_add -> addGoing()
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

    private fun addGoing() {
    }
}