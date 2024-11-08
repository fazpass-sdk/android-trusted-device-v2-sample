package com.fazpass.android_trusted_device_v2_sample.objects

data class Settings(
    var isLocationEnabled: Boolean = false,
    var isSimInfoEnabled: Boolean = false,
    var isHighLevelBiometricEnabled: Boolean = false,
    var isBiometricEnabled: Boolean = true
)
