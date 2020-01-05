package com.handstandsam.biometricks.internal

import androidx.biometric.BiometricPrompt
import com.handstandsam.biometricks.BiometricPromptInfo

/**
 * Takes our [BiometricPromptInfo] and converts to AndroidX [BiometricPrompt.PromptInfo]
 */
internal fun BiometricPromptInfo.toAndroidX(): BiometricPrompt.PromptInfo {
    val builder = BiometricPrompt.PromptInfo.Builder()
    builder.setTitle(title)
    builder.setNegativeButtonText(negativeButtonText)
    builder.setConfirmationRequired(confirmationRequired)
    builder.setDeviceCredentialAllowed(deviceCredentialAllowed)

    subtitle?.let {
        builder.setSubtitle(subtitle)
    }
    description?.let {
        builder.setDescription(description)
    }

    return builder.build()
}