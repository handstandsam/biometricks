package com.handstandsam.biometricks

import androidx.biometric.BiometricPrompt

/**
 * Takes our [BiometricPromptInfo] and converts to AndroidX [BiometricPrompt.PromptInfo]
 */
fun BiometricPromptInfo.toAndroidX(): BiometricPrompt.PromptInfo {
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