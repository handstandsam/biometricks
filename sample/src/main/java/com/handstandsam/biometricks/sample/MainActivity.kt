package com.handstandsam.biometricks.sample

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.handstandsam.biometricks.Biometricks
import com.handstandsam.biometricks.Biometricks.Available
import com.handstandsam.biometricks.Biometricks.Available.*
import java.util.concurrent.Executors

class MainActivity : FragmentActivity() {

    private val biometricAuthenticationCallback = object : BiometricPrompt.AuthenticationCallback() {

        override fun onAuthenticationError(
            errorCode: Int,
            errString: CharSequence
        ) {
            showToast("Error | Message: $errString | Code: $errorCode")
        }

        /**
         * Called when a biometric is recognized.
         *
         * @param result An object containing authentication-related data
         */
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            showToast("Succeeded")
        }

        /**
         * Called when a biometric is valid but not recognized.
         */
        override fun onAuthenticationFailed() {
            showToast("Failed")
        }
    }

    private val biometrick: Biometricks by lazy { Biometricks.from(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        findViewById<TextView>(R.id.biometrick_type).text = "Type: ${biometrick::class.java.simpleName}"
        when (biometrick) {
            is Available -> {
                findViewById<Button>(R.id.launch_biometric)
                    .setOnClickListener {
                        showBiometricPrompt()
                    }
            }
            Biometricks.None -> {
                showToast("Biometrics Not Available on this Device!!!")
            }
        }
    }

    private fun showBiometricPrompt() {
        val biometricName = when (biometrick) {
            is Available -> {
                when (biometrick as Available) {
                    Face -> "Face"
                    Fingerprint -> "Fingerprint"
                    Iris -> "Iris"
                    Unknown,
                    Multiple -> "Biometric"
                }
            }

            Biometricks.None -> {
                RuntimeException("Shouldn't get here... You should show another UI if the user doesn't have Biometrics")
            }
        }
        BiometricPrompt(
            this,
            Executors.newSingleThreadExecutor(),
            biometricAuthenticationCallback
        )
            .authenticate(
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authenticate with $biometricName")
                    .setNegativeButtonText("Cancel")
                    .setDeviceCredentialAllowed(false)
                    .build()
            )
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }
}