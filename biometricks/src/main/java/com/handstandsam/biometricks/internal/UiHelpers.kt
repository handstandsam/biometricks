package com.handstandsam.biometricks.internal

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.ViewTreeObserver
import com.handstandsam.biometricks.Biometricks
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 * Internal logic to compute the [Biometricks] type.
 */
internal object UiHelpers {

    internal suspend fun <T> handleApi28Loading(
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

    internal suspend fun ensureFocus(activity: Activity) {
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
}
