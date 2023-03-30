package com.atallo.pwent.backgroundservices

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.atallo.pwent.MainActivity
import com.atallo.pwent.utils.BridgeApplication

class PrinterReceiver: BroadcastReceiver() {

    val bridgeApplication = BridgeApplication()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent != null) {
            if(intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                val serviceIntent = Intent(context, PrinterService::class.java)
                context!!.startForegroundService(serviceIntent)
                Log.e("on reeboot", intent.action.toString())

//                if (bridgeApplication.isServerConnected("set_reboot")) {
//                    Log.e("on reeboot connected", bridgeApplication.isServerConnected("set_reboot").toString())
//
//                    val activityIntent = Intent(context, MainActivity::class.java)
//
//                    activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//
//                    context.startActivity(activityIntent)
//                }
            }
        }
    }
}