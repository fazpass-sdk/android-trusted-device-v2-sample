package com.fazpass.tdv2_showcase_mobile.objects

data class Settings(
    var isLocationEnabled: Boolean = false,
    var isSimInfoEnabled: Boolean = false,
    var isHighLevelBiometricEnabled: Boolean = false,
    var isBiometricEnabled: Boolean = true
)
