package com.handstandsam.biometricks

import android.content.Context
import com.handstandsam.biometricks.internal.BiometricksHelper

/**
 * Biometricks is the type of Biometric Authentication that
 * is available for this device, represented as a sealed class.
 */
sealed class Biometricks {

    /** The device has no support for Biometric Authentication */
    object None : Biometricks()

    /** The device has support for Biometric Authentication */
    sealed class Available : Biometricks() {

        /** Device has Face Unlock ONLY */
        object Face : Available()

        /** Device has Fingerprint ONLY */
        object Fingerprint : Available()

        /** Device has Iris ONLY */
        object Iris : Available()

        /** Device has more than one biometric feature available ONLY */
        object Multiple : Available()


        /**
         * Device has a biometric type this library isn't aware of.
         *
         * This could happen if an older version of this library is
         * used on a newer device, with new biometric features.
         */
        object Unknown : Available()
    }

    companion object {

        /** Allows us to cache an instance of this helper */
        private var biometricksHelper: BiometricksHelper? = null

        /**
         * Allows a client to query the type of Biometrics available on the device.
         *
         * It is recommended that you cache this value as it will
         * not change over time, but it will be computed every time.
         */

        fun from(context: Context): Biometricks {
            val biometricksHelper = biometricksHelper ?: BiometricksHelper(context)
            return biometricksHelper.type
        }
    }
}