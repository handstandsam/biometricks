package com.handstandsam.biometricks.internal

import androidx.biometric.BiometricPrompt
import com.handstandsam.biometricks.BiometricException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class AuthenticationCallbackWrapper(private val continuation: Continuation<BiometricPrompt.CryptoObject>) :
    BiometricPrompt.AuthenticationCallback() {
    private var authenticationFailed = false

    override fun onAuthenticationError(
        errorCode: Int,
        errString: CharSequence
    ) {
        val shouldShow =
            !isCancel(errorCode) && !authenticationFailed
        continuation.resumeWithException(
            BiometricException(
                errorCode,
                errString,
                shouldShow
            )
        )
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        continuation.resume(result.cryptoObject!!)
    }

    override fun onAuthenticationFailed() {
        // This means the dialog was shown, so we don't want to show the
        // error again ourselves
        authenticationFailed = true
    }

    /**
     * If the prompt was canceled we don't want to show an error ourselves
     */
    fun isCancel(errorCode: Int) =
        errorCode == BiometricPrompt.ERROR_CANCELED
                || errorCode == BiometricPrompt.ERROR_USER_CANCELED
                || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
}