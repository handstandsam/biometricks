package com.handstandsam.biometricks.internal

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.biometric.BiometricManager
import com.handstandsam.biometricks.Biometricks


/**
 * Internal logic to compute the [Biometricks] type.
 */
internal class BiometricksHelper(context: Context) {

    /**
     * Used to query available features on the device.
     */
    private val packageManager: PackageManager = context.packageManager

    /**
     * Allows us to determine if the device is capable of authenticating with Biometrics.
     */
    private val biometricManager: BiometricManager = BiometricManager.from(context)

    /**
     * A list of all known biometrics, filtered by those which are available.
     */
    private val availableFeatures: List<BiometricFeature> = listOf(
        BiometricFeature(
            packageManager = packageManager,
            feature = "android.hardware.fingerprint", // PackageManager.FEATURE_FINGERPRINT
            minSdk = 23,
            type = Biometricks.Available.Fingerprint
        ),
        BiometricFeature(
            packageManager = packageManager,
            feature = "android.hardware.biometrics.face", // PackageManager.FEATURE_FACE
            minSdk = 29,
            type = Biometricks.Available.Face
        ),
        BiometricFeature(
            packageManager = packageManager,
            feature = "android.hardware.biometrics.iris", // PackageManager.FEATURE_IRIS
            minSdk = 29,
            type = Biometricks.Available.Iris
        )
    ).filter { it.isAvailable }

    /**
     * This Biometricks type is computed lazily and cached.
     */
    val type: Biometricks by lazy {
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> when {
                availableFeatures.isEmpty() -> Biometricks.None
                availableFeatures.size == 1 -> availableFeatures[0].type
                else -> Biometricks.Available.Multiple
            }
            else -> Biometricks.None
        }
    }

    /**
     * Class to compute availability of a biometric feature from the [PackageManager]
     */
    private class BiometricFeature(
        packageManager: PackageManager,
        feature: String,
        minSdk: Int,
        val type: Biometricks.Available
    ) {
        val isAvailable: Boolean by lazy {
            if (Build.VERSION.SDK_INT >= minSdk) {
                packageManager.hasSystemFeature(feature)
            } else {
                false
            }
        }
    }
}
