package com.atallo.pwent.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.atallo.pwent.PrinterManager
import com.atallo.pwent.ServerManager


class BridgeApplication: Application() {

    init {
        context = this
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate!!!!!!!!!")
        printerManager.connect(this)
        context = applicationContext()
        Log.e("Check ", "yes")

        sharedPref = context!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onTerminate() {
        super.onTerminate()

        Log.d(TAG, "Terminate!!!!!")
        printerManager.disconnect(this)
    }

    fun saveString(KEY_NAME: String, text: String) {

        Log.e("Check ", "Here")

        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putString(KEY_NAME, text)

        editor.apply()
    }

    fun saveInt(KEY_NAME: String, value: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putInt(KEY_NAME, value)

        editor.apply()
    }

    fun saveServerStatus(statusKey: String, status: Boolean) {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putBoolean(statusKey, status)

        editor.apply()
    }

    fun getValueString(KEY_NAME: String): String? {

        return sharedPref.getString(KEY_NAME, null)

    }

    fun getValueInt(KEY_NAME: String): Int {

        return sharedPref.getInt(KEY_NAME, 0)
    }


    fun isServerConnected(statusKey: String): Boolean {

        return sharedPref.getBoolean(statusKey, false)

    }

    fun logoutOrClearSharedPreference() {
        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.clear()
        editor.apply()
    }

    fun removeValue(KEY_NAME: String) {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.remove(KEY_NAME)
        editor.apply()
    }

//    fun foregroundServiceRunning(): Boolean {
//        val activityManager = context!!.getSystemService(ACTIVITY_SERVICE) as ActivityManager
//        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
//            if (PrinterService::class.java.name == service.service.className) {
//                return true
//            }
//        }
//        return false
//    }

    companion object {

        const val TAG = "BridgeApplication"
        private var context: Context? = null
        val PREFS_NAME = "sharedPrefs"

        private lateinit var sharedPref: SharedPreferences

        fun applicationContext() : Context {
            return context!!.applicationContext
        }


        val serverManager = ServerManager(9100)
        val printerManager = PrinterManager()
        private val KEY_SERVER_STATUS = "logged_in"
    }
}