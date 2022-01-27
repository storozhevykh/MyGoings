package com.storozhevykh.mygoings.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.storozhevykh.mygoings.App
import com.storozhevykh.mygoings.R
import com.storozhevykh.mygoings.databinding.DialogSyncBinding
import com.storozhevykh.mygoings.firebase.SyncHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SyncDialog: DialogFragment(), View.OnClickListener {

    lateinit var binding: DialogSyncBinding

    @Inject
    lateinit var syncHelper: SyncHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        App.component.inject(this)

        binding = DialogSyncBinding.inflate(layoutInflater)

        binding.btnMoveToCloud.setOnClickListener(this)
        binding.btnDelete.setOnClickListener(this)
        binding.cancelRegistration.setOnClickListener(this)

        syncHelper = SyncHelper()

        return binding.root
    }

    override fun onClick(v: View?) {
        CoroutineScope(Dispatchers.IO).launch {
            when (v?.id) {
                R.id.btn_moveToCloud -> {
                    syncHelper.synchronize()
                    dismiss()
                }
                R.id.btn_delete -> {
                    syncHelper.deleteFromLocal()
                    syncHelper.synchronize()
                    dismiss()
                }
                R.id.cancel_registration -> {
                    Firebase.auth.signOut()
                    (requireActivity() as MainActivity).runOnUiThread(Runnable { (requireActivity() as MainActivity).navHeaderUpdate() })
                    dismiss()
                }
            }
        }

    }
}