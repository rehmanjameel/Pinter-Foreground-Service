package com.atallo.pwent.backgroundservices

import android.app.*
import android.content.Intent
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WifiLock
import android.os.*
import android.os.PowerManager.WakeLock
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.atallo.pwent.MainActivity
import com.atallo.pwent.R

class PrinterService: Service() {

    private val ID_SERVICE = 101
    private var wifiLock: WifiLock? = null
    private var wakeLock: WakeLock? = null

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate() {
        super.onCreate()

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0,
            notificationIntent, PendingIntent.FLAG_MUTABLE
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel(
                notificationManager
            ) else ""
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Printer is working")
            .setSmallIcon(R.drawable.ic_outline_print_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        startForeground(ID_SERVICE, notification)

//        val wm = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
//        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "MyWifiLock")
//        wifiLock?.acquire()
//        val pm = applicationContext.getSystemService(POWER_SERVICE) as PowerManager
//        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock")
//        wakeLock?.acquire()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val mainActivity = MainActivity()
        mainActivity.start()
//        Thread {
//            while (true) {
//                Log.e("Service", "Service is running...")
//                try {
//
//                    Thread.sleep(2000)
//                } catch (e: InterruptedException) {
//                    e.printStackTrace()
//                }
//            }
//        }.start()
        return START_NOT_STICKY

    }

    private fun createNotificationChannel(notificationManager: NotificationManager): String {
        val channelId = "my_service_channelid"
        val channelName = "My Foreground Service"
        var channel: NotificationChannel? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN)
            channel.importance = NotificationManager.IMPORTANCE_DEFAULT
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notificationManager.createNotificationChannel(channel)
        }
        return channelId
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        Handler(Looper.getMainLooper()).postDelayed({
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(rootIntent)
            } else {
                startService(rootIntent)
            }
        }, 1000)
    }

    override fun onDestroy() {
        if (wakeLock != null) {
            if (wakeLock!!.isHeld) {
                wakeLock!!.release()
            }
        }
        if (wifiLock != null) {
            if (wifiLock!!.isHeld) {
                wifiLock!!.release()
            }
        }
        super.onDestroy()
    }

    companion object {
        private val ACTION_STOP_LISTEN = "action_stop_listen"
    }
}