package com.storozhevykh.mygoings.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.storozhevykh.mygoings.Constants
import com.storozhevykh.mygoings.R
import com.storozhevykh.mygoings.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    lateinit var sPref: SharedPreferences
    lateinit var binding: FragmentNotificationsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sPref = requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        binding = FragmentNotificationsBinding.inflate(layoutInflater)

        binding.notificationsPrioritySpinner.adapter = ArrayAdapter(requireContext(), R.layout.settings_spinner, resources.getStringArray(R.array.notification_priorities))
        binding.notificationsPrioritySpinner.isEnabled = sPref.getBoolean(Constants.PREF_NOTIFY_KEY, true)
        binding.notificationsPrioritySpinner.setSelection(sPref.getInt(Constants.PREF_NOTIFY_PRIORITY, 0))
        binding.notificationsPrioritySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val editor = sPref.edit()
                editor.putInt(Constants.PREF_NOTIFY_PRIORITY, position)
                editor.apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.notificationsSwitch.isChecked = sPref.getBoolean(Constants.PREF_NOTIFY_KEY, true)
        binding.notificationsSwitch.setOnCheckedChangeListener(object: CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                val editor = sPref.edit()
                editor.putBoolean(Constants.PREF_NOTIFY_KEY, isChecked)
                editor.apply()
                enablingFields(isChecked)
            }
        })

        binding.notificationsDelayEdit.setText(sPref.getInt(Constants.PREF_NOTIFY_MINUTES_KEY, 30).toString())
        binding.notificationsDelayEdit.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if(s?.length!! > 0) {
                    val editor = sPref.edit()
                    editor.putInt(Constants.PREF_NOTIFY_MINUTES_KEY, Integer.valueOf(s.toString()))
                    editor.apply()
                }
            }
        })

        enablingFields(binding.notificationsSwitch.isChecked)

        return binding.root
    }

    fun enablingFields(on: Boolean) {
        if(on) {
            binding.notificationsDelayEdit.isEnabled = true
            binding.notificationsPrioritySpinner.isEnabled = true
            binding.notificationsOnTV.isEnabled = true
            binding.notificationsPriorityTV.isEnabled = true
            binding.notificationsDelayEdit.isEnabled = true
        }
        else {
            binding.notificationsDelayEdit.isEnabled = false
            binding.notificationsPrioritySpinner.isEnabled = false
            binding.notificationsOnTV.isEnabled = false
            binding.notificationsPriorityTV.isEnabled = false
            binding.notificationsDelayEdit.isEnabled = false
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            NotificationsFragment()
    }
}