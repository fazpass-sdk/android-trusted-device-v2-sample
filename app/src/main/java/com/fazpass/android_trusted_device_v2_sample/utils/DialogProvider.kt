package com.fazpass.android_trusted_device_v2_sample.utils

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.fazpass.android_trusted_device_v2_sample.objects.NotifiableDeviceModel
import com.fazpass.android_trusted_device_v2_sample.objects.Settings

class DialogProvider(private val activity: Activity) {

    fun showPickActionsDialog(
        title: String,
        actions: Array<String>,
        onPickAction: (Int) -> Unit
    ) {
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setItems(actions) { dialog, i ->
                onPickAction(i)
                dialog.dismiss()
            }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    fun showSettingsDialog(
        oldSettings: Settings,
        onSaveSettings: (Settings) -> Unit
    ) {
        val newSettings = Settings()
        AlertDialog.Builder(activity)
            .setTitle("Settings")
            .setMultiChoiceItems(
                arrayOf(
                    "Enable location",
                    "Enable Sim Information",
                    "Enable High Biometric Level",
                    "Enable Biometric Auth"
                ),
                booleanArrayOf(
                    oldSettings.isLocationEnabled,
                    oldSettings.isSimInfoEnabled,
                    oldSettings.isHighLevelBiometricEnabled,
                    oldSettings.isBiometricEnabled
                )
            ) { _, i, newValue ->
                when (i) {
                    0 -> newSettings.isLocationEnabled = newValue
                    1 -> newSettings.isSimInfoEnabled = newValue
                    2 -> newSettings.isHighLevelBiometricEnabled = newValue
                    3 -> newSettings.isBiometricEnabled = newValue
                }
            }
            .setPositiveButton("Save") { dialog, _ ->
                onSaveSettings(newSettings)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    fun showPickNotifiableDeviceDialog(
        notifiableDevices: ArrayList<NotifiableDeviceModel>,
        callback: (String) -> Unit
    ) {
        val ids = arrayListOf<String>()
        val names = arrayListOf<String>()
        notifiableDevices.forEach {
            ids.add(it.id)
            names.add("${it.name}, ${it.series}")
        }
        AlertDialog.Builder(activity).apply {
            setTitle("Pick Device")
            setItems(names.toTypedArray()) { dialog, which ->
                callback(ids[which])
                dialog.dismiss()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        }.show()
    }

    fun showCrossDeviceRequestDialog(
        deviceName: String,
        onAccept: () -> Unit,
        onDeny: () -> Unit
    ) {
        AlertDialog.Builder(activity).apply {
            setTitle("Cross Device Request")
            setMessage("Device $deviceName is asking to login into your account. Authorize?")
            setPositiveButton("YES") { dialog, _ ->
                onAccept()
                dialog.dismiss()
            }
            setNegativeButton("NO") { dialog, _ ->
                onDeny()
                dialog.dismiss()
            }
        }.show()
    }

    fun showCrossDeviceValidateDialog(
        deviceName: String,
        action: String
    ) {
        AlertDialog.Builder(activity).apply {
            setTitle("Cross Device Validate")
            setMessage("Your request has been responded by device $deviceName with \"$action\".")
            setNeutralButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        }.show()
    }
}