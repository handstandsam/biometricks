package com.handstandsam.biometricks

import androidx.biometric.BiometricPrompt

/**
 * This will force you to pass [BiometricPrompt.CryptoObject] and deviceCredentialAllowed to false.
 *
 * See Javadocs for [BiometricPrompt.PromptInfo.Builder]
 *
 * https://developer.android.com/reference/androidx/biometric/BiometricPrompt.PromptInfo.Builder
 */
class BiometricPromptInfo(
    /** Required */
    val title: String,
    val negativeButtonText: String,
    val cryptoObject: BiometricPrompt.CryptoObject,

    /** Optional */
    val subtitle: String? = null,
    val description: String? = null,
    val deviceCredentialAllowed: Boolean = false,
    val confirmationRequired: Boolean = true
)
