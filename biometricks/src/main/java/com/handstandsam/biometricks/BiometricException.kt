package com.handstandsam.biometricks

import androidx.annotation.IntDef
import androidx.biometric.BiometricPrompt

/**
 * Error for when the biometric prompt fails for whatever reason.
 */
class BiometricException(
    /**
     * The error code, see `BiometricPrompt.ERROR_*`
     */
    @BiometricError val code: Int,
    /**
     * The error string to possibly show to users.
     */
    val errString: CharSequence,
    /**
     * Sometimes the user has already seen the error in the prompt itself, sometimes not. If this is
     * true, you should show the error to the user yourself.
     */
    val shouldShow: Boolean
) : Exception("$errString ($code)")

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    BiometricPrompt.ERROR_HW_UNAVAILABLE,
    BiometricPrompt.ERROR_UNABLE_TO_PROCESS,
    BiometricPrompt.ERROR_TIMEOUT,
    BiometricPrompt.ERROR_NO_SPACE,
    BiometricPrompt.ERROR_CANCELED,
    BiometricPrompt.ERROR_LOCKOUT,
    BiometricPrompt.ERROR_VENDOR,
    BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
    BiometricPrompt.ERROR_USER_CANCELED,
    BiometricPrompt.ERROR_NO_BIOMETRICS,
    BiometricPrompt.ERROR_HW_NOT_PRESENT,
    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
    BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL
)
private annotation class BiometricError
