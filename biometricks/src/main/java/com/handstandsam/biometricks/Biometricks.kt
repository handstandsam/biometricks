package com.handstandsam.biometricks

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.ViewTreeObserver
import androidx.annotation.MainThread
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.handstandsam.biometricks.internal.BiometricksHelper
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

        /**
         * Allows a client to query the type of Biometrics available on the device.
         *
         * It is recommended that you cache this value as it will
         * not change over time, but it will be computed every time.
         */
        fun from(context: Context): Biometricks {
            return BiometricksHelper(context).type
        }

        /**
         * Wrapper around [BiometricPrompt.authenticate] which handles many tricky issues for you.
         * It will:
         * - Wait for the app to be focused if not currently, as the prompt will fail to show
         * otherwise.
         * - Allow you to show/hide a loading indicator for when there's a delay showing the prompt.
         * - Give you information on if you need to show an error to the user or not.
         * ```
         * lifecycleScope.launch {
         *     try {
         *         val unlockedCryptObject = Biometricks.showPrompt(
         *             this@MainActivity,
         *             lockedCryptoObject,
         *             BiometricPrompt.PromptInfo.Builder()
         *                 .setTitle(...)
         *                 .setNegativeButtonText(...)
         *                 .build()
         *         ) { showLoading -> progressBar.visibility = if (showLoading) View.VISIBLE else View.INVISIBLE }
         *
         *         // success
         *     } catch (e: BiometricException) {
         *         // failure
         *         if (e.shouldShow) {
         *             // show error to the user
         *         }
         *     }
         * }
         * ```
         *
         * @param activity The host activity.
         * @param cryptoObject The [BiometricPrompt.CryptoObject] to unlock. This is always required
         * as you many not get 'secure' biometrics if you don't include it.
         * @param promptInfo The [BiometricPrompt.PromptInfo] to display in the prompt.
         * @param showLoading Callback to show/hide a loading indicator for when there's a delay
         * showing the prompt. This is only used on api 28 as after that it shows immediately.
         */
        @MainThread
        suspend fun showPrompt(
            activity: FragmentActivity,
            cryptoObject: BiometricPrompt.CryptoObject,
            promptInfo: BiometricPrompt.PromptInfo,
            showLoading: (Boolean) -> Unit
        ): BiometricPrompt.CryptoObject {
            ensureFocus(activity)

            return handleApi28Loading(activity, showLoading) {
                suspendCoroutine<BiometricPrompt.CryptoObject> { continuation ->
                    BiometricPrompt(
                        activity,
                        ContextCompat.getMainExecutor(activity),
                        Callback(continuation)
                    ).authenticate(promptInfo, cryptoObject)
                }
            }
        }

        /**
         * Wrapper around [BiometricPrompt.authenticate] which handles many tricky issues for you.
         * It will:
         * - Wait for the app to be focused if not currently, as the prompt will fail to show
         * otherwise.
         * - Allow you to show/hide a loading indicator for when there's a delay showing the prompt.
         * - Give you information on if you need to show an error to the user or not.
         * ```
         * lifecycleScope.launch {
         *     try {
         *         val unlockedCryptObject = Biometricks.showPrompt(
         *             this@MainActivity,
         *             lockedCryptoObject,
         *             BiometricPrompt.PromptInfo.Builder()
         *                 .setTitle(...)
         *                 .setNegativeButtonText(...)
         *                 .build()
         *         ) { showLoading -> progressBar.visibility = if (showLoading) View.VISIBLE else View.INVISIBLE }
         *
         *         // success
         *     } catch (e: BiometricException) {
         *         // failure
         *         if (e.shouldShow) {
         *             // show error to the user
         *         }
         *     }
         * }
         * ```
         *
         * @param fragment The host fragment.
         * @param cryptoObject The [BiometricPrompt.CryptoObject] to unlock. This is always required
         * as you many not get 'secure' biometrics if you don't include it.
         * @param promptInfo The [BiometricPrompt.PromptInfo] to display in the prompt.
         * @param showLoading Callback to show/hide a loading indicator for when there's a delay
         * showing the prompt. This is only used on api 28 as after that it shows immediately.
         */
        @MainThread
        suspend fun showPrompt(
            fragment: Fragment,
            cryptoObject: BiometricPrompt.CryptoObject,
            promptInfo: BiometricPrompt.PromptInfo,
            showLoading: (Boolean) -> Unit
        ): BiometricPrompt.CryptoObject {
            val activity = fragment.requireActivity()

            ensureFocus(activity)

            return handleApi28Loading(activity, showLoading) {
                suspendCoroutine<BiometricPrompt.CryptoObject> { continuation ->
                    BiometricPrompt(
                        fragment,
                        ContextCompat.getMainExecutor(fragment.requireContext()),
                        Callback(continuation)
                    ).authenticate(promptInfo, cryptoObject)
                }
            }
        }

        private suspend fun <T> handleApi28Loading(
            activity: Activity,
            showLoading: (Boolean) -> Unit,
            showPrompt: suspend () -> T
        ): T {
            // On api 28 if the user is locked out of biometrics from too many failed
            // attempts there will be a long delay before getting the error back. So the
            // user isn't confused as to what is going on, show a loading indicator.
            if (Build.VERSION.SDK_INT == 28) {
                val window = activity.window
                val handler = Handler(Looper.getMainLooper())

                var showingLoading = false

                // Don't show if the prompt shows quickly
                val showLoadingRunnable = Runnable {
                    showingLoading = true
                    showLoading(true)
                }
                handler.postDelayed(showLoadingRunnable, 150)

                // The only way to tell that the prompt is shown is to listen to the window
                // losing focus events.
                window.decorView.viewTreeObserver.addOnWindowFocusChangeListener(object :
                    ViewTreeObserver.OnWindowFocusChangeListener {
                    override fun onWindowFocusChanged(hasFocus: Boolean) {
                        if (!hasFocus) {
                            window.decorView.viewTreeObserver.removeOnWindowFocusChangeListener(this)
                            handler.removeCallbacks(showLoadingRunnable)
                            showingLoading = false
                            showLoading(false)
                        }
                    }
                })

                try {
                    return showPrompt()
                } finally {
                    handler.removeCallbacks(showLoadingRunnable)
                    // Hide loading if the prompt fails to show
                    if (showingLoading) {
                        showLoading(false)
                    }
                }
            } else {
                // The prompt shows immediately on api 29+ so don't worry about any of this.
                return showPrompt()
            }
        }

        private suspend fun ensureFocus(activity: Activity) {
            suspendCoroutine<Unit> { continuation ->
                // Showing the biometrics prompt will be ignored if the app does not have focus. You may
                // think that this will always be the case if you are resumed, it is not.
                if (activity.hasWindowFocus()) {
                    continuation.resume(Unit)
                } else {
                    val window = activity.window
                    window.decorView.viewTreeObserver.addOnWindowFocusChangeListener(object :
                        ViewTreeObserver.OnWindowFocusChangeListener {
                        override fun onWindowFocusChanged(hasFocus: Boolean) {
                            if (hasFocus) {
                                window.decorView.viewTreeObserver.removeOnWindowFocusChangeListener(
                                    this
                                )
                                continuation.resume(Unit)
                            }
                        }
                    })
                }
            }
        }

        private class Callback(private val continuation: Continuation<BiometricPrompt.CryptoObject>) :
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
    }
}