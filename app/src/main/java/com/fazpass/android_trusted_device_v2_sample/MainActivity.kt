package com.fazpass.android_trusted_device_v2_sample

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.fazpass.android_trusted_device_v2_sample.services.FazpassService
import com.fazpass.android_trusted_device_v2_sample.services.SeamlessService
import com.fazpass.android_trusted_device_v2_sample.utils.DialogProvider
import com.fazpass.android_trusted_device_v2_sample.utils.JsonBeautifier
import com.fazpass.android_trusted_device_v2_sample.views.EntryView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : FragmentActivity() {

    private val seamlessService = SeamlessService()
    private val fazpassService = FazpassService(this)
    private val dialogProvider = DialogProvider(this)

    private lateinit var infoView: LinearLayout
    private val checkBtn: Button
        get() = Button(this).apply {
        text = "Check"
        setOnClickListener { onClickCheckButton() }
    }
    private val actionBtn: Button
        get() = Button(this).apply {
        text = "Action"
        setOnClickListener { onClickDeviceActionButton() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        infoView = findViewById(R.id.ma_info_view)
        val actionFab = findViewById<FloatingActionButton>(R.id.fab_action)
        actionFab.setOnClickListener {
            val actions = arrayOf("Open Settings", "Generate Meta")
            dialogProvider.showPickActionsDialog("Pick Action", actions) { which ->
                when (which) {
                    0 -> dialogProvider.showSettingsDialog(fazpassService.getSettings()) {
                        fazpassService.setSettings(it)
                    }
                    1 -> onGenerateMeta()
                }
            }
        }

        requestPermissions()

        fazpassService.init()
        fazpassService.listenToIncomingCrossDeviceData(
            this::onCrossDeviceRequest,
            this::onCrossDeviceValidate
        )
    }

    private fun onClickCheckButton() {
        seamlessService.check(fazpassService.meta) { response ->
            onResponse("Check", response)
        }
    }

    private fun onClickDeviceActionButton() {
        val items = arrayListOf("Enroll", "Validate", "Remove")
        if (seamlessService.notifiableDevices.isNotEmpty()) {
            items.add("Send Notification")
        }
        dialogProvider.showPickActionsDialog("Pick Device Action", items.toTypedArray()) { which ->
            when (which) {
                0 -> seamlessService.enroll(fazpassService.meta) { response ->
                    onResponse("Enroll", response)
                }
                1 -> seamlessService.validate(fazpassService.meta) { response ->
                    onResponse("Validate", response)
                }
                2 -> seamlessService.remove(fazpassService.meta) { response ->
                    onResponse("Remove", response)
                }
                3 -> dialogProvider.showPickNotifiableDeviceDialog(seamlessService.notifiableDevices) { pickedDeviceId ->
                    seamlessService.sendNotification(fazpassService.meta, pickedDeviceId) { response ->
                        onResponse("Send Notification", response)
                    }
                }
            }
        }
    }

    private fun onGenerateMeta() {
        infoView.removeAllViews()

        try {
            fazpassService.generateMeta(
                this::onErrorOccurred,
                this::onMetaGenerated
            )
        } catch (e: Exception) {
            onErrorOccurred(e)
        }
    }

    private fun onErrorOccurred(e: Exception) {
        infoView.addView(EntryView(this).apply {
            name = "Error"
            value = "${e.javaClass.name}\n" +
                    "${e.message}\n" +
                    e.stackTraceToString()
        })

        infoView.addView(TextView(this).apply {
            setTextColor(Color.RED)
            text = "*Press 'Generate Meta' button to reset"
        })
    }

    private fun onMetaGenerated() {
        infoView.addView(EntryView(this).apply {
            name = "Generated Meta"
            value = fazpassService.meta
        })

        infoView.addView(checkBtn)
    }

    private fun onResponse(title: String, response: String) {
        if (title != "Send Notification" && title != "Validate Notification") {
            infoView.removeViewAt(infoView.childCount-1)
        }

        infoView.addView(EntryView(this).apply {
            val beautifier = JsonBeautifier(response)
            name = "$title Response"
            value = beautifier
            setSpans(beautifier.spans)
        })

        if (title == "Check") {
            infoView.addView(actionBtn)
        } else {
            infoView.addView(TextView(this).apply {
                setTextColor(Color.RED)
                text = "*Press 'Generate Meta' button to reset"
            })
        }
    }

    private fun onCrossDeviceRequest(deviceName: String, notificationId: String) {
        fun onRespond(answer: Boolean) {
            fazpassService.generateMeta(this::onErrorOccurred) {
                seamlessService.validateNotification(fazpassService.meta, notificationId, answer) { response ->
                    onResponse("Validate Notification", response)
                }
            }
        }

        dialogProvider.showCrossDeviceRequestDialog(
            deviceName,
            onAccept = { onRespond(true) },
            onDeny = { onRespond(false) }
        )
    }

    private fun onCrossDeviceValidate(deviceName: String, action: String) {
        dialogProvider.showCrossDeviceValidateDialog(deviceName, action)
    }

    private fun requestPermissions() {
        val requiredPermissions = ArrayList(
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requiredPermissions.add(Manifest.permission.READ_PHONE_NUMBERS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val deniedPermissions: MutableList<String> = ArrayList()
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                deniedPermissions.add(permission)
            }
        }
        if (deniedPermissions.size != 0) ActivityCompat.requestPermissions(
            this,
            deniedPermissions.toTypedArray(),
            1
        )
    }
}