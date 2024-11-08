package com.fazpass.android_trusted_device_v2_sample.services

import android.app.Activity
import android.util.Log
import com.fazpass.android_trusted_device_v2.FazpassFactory
import com.fazpass.android_trusted_device_v2.FazpassSettings
import com.fazpass.android_trusted_device_v2.SensitiveData
import com.fazpass.android_trusted_device_v2_sample.objects.Settings
import org.json.JSONObject

class FazpassService(private val activity: Activity) {

    private val fazpass = FazpassFactory.getInstance()

    companion object {
        private const val PUBLIC_KEY_ASSET_FILENAME = "new-public-key.pub"
        private const val ACCOUNT_INDEX = 0
    }

    private var enabledBiometricAuth = false

    var meta: String = ""
        private set

    fun init() {
        fazpass.init(activity, PUBLIC_KEY_ASSET_FILENAME)
        Log.i("APP SIGNATURES", fazpass.getAppSignatures(activity).toString())
    }

    fun getSettings(): Settings {
        val fazpassSettings = fazpass.getSettings(ACCOUNT_INDEX)
        return Settings(
            fazpassSettings?.sensitiveData?.contains(SensitiveData.location) ?: false,
            fazpassSettings?.sensitiveData?.contains(SensitiveData.simNumbersAndOperators) ?: false,
            fazpassSettings?.isBiometricLevelHigh ?: false,
            enabledBiometricAuth
        )
    }

    fun setSettings(settings: Settings) {
        val newFazpassSettings = FazpassSettings.Builder()

        if (settings.isLocationEnabled) {
            newFazpassSettings.enableSelectedSensitiveData(SensitiveData.location)
        } else {
            newFazpassSettings.disableSelectedSensitiveData(SensitiveData.location)
        }

        if (settings.isSimInfoEnabled) {
            newFazpassSettings.enableSelectedSensitiveData(SensitiveData.simNumbersAndOperators)
        } else {
            newFazpassSettings.disableSelectedSensitiveData(SensitiveData.simNumbersAndOperators)
        }

        if (settings.isHighLevelBiometricEnabled) {
            fazpass.generateNewSecretKey(activity)
            newFazpassSettings.setBiometricLevelToHigh()
        } else {
            newFazpassSettings.setBiometricLevelToLow()
        }

        enabledBiometricAuth = settings.isBiometricEnabled

        fazpass.setSettings(activity, ACCOUNT_INDEX, newFazpassSettings.build())
    }

    fun generateMeta(onError: (e: Exception) -> Unit, onFinished: () -> Unit) {
        fazpass.generateMeta(activity, accountIndex = ACCOUNT_INDEX, biometricAuth = enabledBiometricAuth) { meta, fazpassException ->
            this.meta = meta

            if (fazpassException != null) {
                onError(fazpassException.exception)
            } else {
                onFinished()
            }
        }
    }

    fun listenToIncomingCrossDeviceData(
        onRequest: (deviceName: String, notificationId: String) -> Unit,
        onValidate: (deviceName: String, action: String) -> Unit
    ) {
        val fromNotification = fazpass.getCrossDeviceDataFromNotification(activity.intent)
        if (fromNotification != null) {
            when (fromNotification.status) {
                "request" -> onRequest(localizeDeviceName(fromNotification.deviceRequest), fromNotification.notificationId!!)
                "validate" -> onValidate(localizeDeviceName(fromNotification.deviceReceive), fromNotification.action!!)
                else -> println(JSONObject(fromNotification.toMap()).toString())
            }
        }

        fazpass.getCrossDeviceDataStreamInstance(activity).listen {
            when (it.status) {
                "request" -> onRequest(localizeDeviceName(it.deviceRequest), it.notificationId!!)
                "validate" -> onValidate(localizeDeviceName(it.deviceReceive), it.action!!)
                else -> println(JSONObject(it.toMap()).toString())
            }
        }
    }

    /**
     * FROM: VIVO;Vivo V1;MT6769V/CZ;Android 31
     *
     * TO: VIVO, Vivo V1
     */
    private fun localizeDeviceName(rawDeviceName: String): String {
        return rawDeviceName.split(";").subList(0, 2).joinToString(", ")
    }
}