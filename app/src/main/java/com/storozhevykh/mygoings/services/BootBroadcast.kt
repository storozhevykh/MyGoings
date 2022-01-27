package com.storozhevykh.mygoings.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootBroadcast: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.startService(Intent(context, NotificationService::class.java))
    }
}