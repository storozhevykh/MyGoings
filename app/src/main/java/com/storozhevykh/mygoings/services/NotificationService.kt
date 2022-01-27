package com.storozhevykh.mygoings.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.toColor
import com.storozhevykh.mygoings.App
import com.storozhevykh.mygoings.Constants
import com.storozhevykh.mygoings.R
import com.storozhevykh.mygoings.database.DataBase
import com.storozhevykh.mygoings.database.GoingDao
import com.storozhevykh.mygoings.model.FilterGoingsList
import com.storozhevykh.mygoings.model.Going
import com.storozhevykh.mygoings.model.StaticStorage
import com.storozhevykh.mygoings.view.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class NotificationService : Service() {

    @Inject
    lateinit var dataBase: DataBase
    @Inject
    lateinit var filterGoingsList: FilterGoingsList

    private final val NOTIFICATION_ID = 111
    private final val CHANNEL_ID = "MyGoings"

    lateinit var goingDAO: GoingDao
    lateinit var sPref: SharedPreferences
    private var notificationMinutesBefore = 30
    lateinit var notificationQueue: TreeMap<Long, Going>

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        App.component.inject(this)
        goingDAO = dataBase.goingDao()

        sPref = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        notificationMinutesBefore = sPref.getInt(Constants.PREF_NOTIFY_MINUTES_KEY, 30)
        notificationQueue = TreeMap()
        instance = this

        Log.d("MyLog", "Service is started")
        println("Service is started")

        CoroutineScope(Dispatchers.IO).launch {
            when (sPref.getInt(Constants.PREF_NOTIFY_PRIORITY, Constants.NOTIFY_LOW)) {
                Constants.NOTIFY_LOW -> createNotificationQueue(Constants.NOTIFY_LOW)
                Constants.NOTIFY_MEDIUM -> createNotificationQueue(Constants.NOTIFY_MEDIUM)
                Constants.NOTIFY_HIGH -> createNotificationQueue(Constants.NOTIFY_HIGH)
            }
            toNotify()
        }

        return START_STICKY
    }

    private suspend fun createNotificationQueue(notifyPriority: Int) {

        val fitGoings: List<Going> = goingDAO.getActiveWithPriority(notifyPriority)

        for (i in 0 until fitGoings.size) {
            if (fitGoings[i].timeElapsed > System.currentTimeMillis())
            notificationQueue.put(fitGoings[i].timeElapsed - notificationMinutesBefore * 60 * 1000, fitGoings[i])
        }
        Log.d("MyLog", "NotificationQueue is created")
    }

    private suspend fun toNotify() {
        while (notificationQueue.isNotEmpty()) {
            Log.d("MyLog", "Next notification after: ${notificationQueue.firstKey() - System.currentTimeMillis()}")
            println("Next notification after: ${notificationQueue.firstKey() - System.currentTimeMillis()}")
            delay(notificationQueue.firstKey() - System.currentTimeMillis())
            makeNotification(notificationQueue[notificationQueue.firstKey()]!!)
            notificationQueue.remove(notificationQueue.firstKey())
        }
    }

    private suspend fun makeNotification(going: Going) {

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder: NotificationCompat.Builder = NotificationCompat.Builder (this, CHANNEL_ID)
            .setSmallIcon(StaticStorage.categoryDrawableIDs[going.priority].get(going.category)!!)
            .setContentTitle("Going expires soon")
            .setContentText(going.title)
            .setPriority(NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setContentIntent(pendingIntent)

        val nm: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = builder.build()
        notification.defaults = Notification.DEFAULT_ALL
        nm.notify(going.hashCode(), notification)

        Log.d("MyLog", "Notification is shown: ${going.title}")
        println("Notification is shown: ${going.title}")

    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    companion object {
        lateinit var instance: NotificationService
    }
}