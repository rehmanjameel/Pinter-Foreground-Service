package com.atallo.pwent

import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.atallo.pwent.backgroundservices.PrinterService
import com.atallo.pwent.databinding.ActivityMainBinding
import com.atallo.pwent.utils.BridgeApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.activity.result.ActivityResultCallback

import androidx.activity.result.contract.ActivityResultContracts

import androidx.activity.result.ActivityResultLauncher





class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val bridgeApplication = BridgeApplication()

    private val serverCallBack: ServerCallBack = (object : ServerCallBack {
        override fun onReceiveData(data: ByteArray) {
            BridgeApplication.printerManager.sendRawData(data)
        }
    })

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // set the app name with version name
        try {
            val pInfo: PackageInfo =
                packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            binding.appNameVersion.text = "${resources.getString(R.string.app_name)} ($version)"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        setupIPAddressText()
        requestPermission()

        val serviceIntent = Intent(this, PrinterService::class.java)

        // save value of start server app checkbox and start/stop server with background service
        binding.toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                bridgeApplication.saveServerStatus(statusKey = "server_status", status = true)
                if (!isMyServiceRunning(PrinterService::class.java)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }
                }
//                start()
            } else {
                bridgeApplication.saveServerStatus(statusKey = "server_status", false)
                if (isMyServiceRunning(PrinterService::class.java)) {
                    stopService(Intent(this, PrinterService::class.java))
                    stop()
                }
            }
        }

        // set state of server connection
        binding.toggleButton.isChecked =
            bridgeApplication.isServerConnected(statusKey = "server_status")

        // save value of auto start app checkbox
        binding.autoStartWithAndroid.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                bridgeApplication.saveServerStatus(statusKey = "set_reboot", true)
            } else {
                bridgeApplication.saveServerStatus(statusKey = "set_reboot", false)
            }
        }

        // set state of auto start app on reboot
        binding.autoStartWithAndroid.isChecked =
            bridgeApplication.isServerConnected(statusKey = "set_reboot")
    }

//    override fun onDestroy() {
//        super.onDestroy()
//
//        stop()
//    }

    private fun setupIPAddressText() {
        val connectivityManager = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val linkProperties = connectivityManager.getLinkProperties(connectivityManager.activeNetwork)
        linkProperties?.let {
            val ipAddress = it.linkAddresses.firstOrNull { it.toString().contains("192.") }
            ipAddress?.let { binding.ipAddressTextView.text = getString(R.string.ip_address_text, it) }
        }
    }

    fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            BridgeApplication.serverManager.start(serverCallBack)
        }
    }

    fun stop() {
        BridgeApplication.serverManager.stop()
    }

    fun isNetworkAvailable(): Boolean {
        val cm = applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }


    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + this.packageName)
                )
                startActivityIntent.launch(intent)
            } else {
                //Permission Granted-System will work
            }
        }
    }

    var startActivityIntent: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Add same code that you want to add in onActivityResult method
    }

    companion object {
        const val TAG = "MainActivity"

        var sInstance: MainActivity? = null

        fun getInstance(): MainActivity? {
            return sInstance
        }
    }
}