package com.handstandsam.biometricks.sample

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.handstandsam.biometricks.BiometricException
import com.handstandsam.biometricks.Biometricks
import com.handstandsam.biometricks.Biometricks.Available
import com.handstandsam.biometricks.Biometricks.Available.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : FragmentActivity() {

    private val biometrick: Biometricks by lazy { Biometricks.from(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        findViewById<TextView>(R.id.biometrick_type).text =
            "Type: ${biometrick::class.java.simpleName}"
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
        lifecycleScope.launch {
            val cryptoObject = withContext(Dispatchers.IO) {
                Crypto().cryptoObject()
            }

            try {
                val unlockedCryptoObject = Biometricks.showPrompt(
                    this@MainActivity,
                    cryptoObject,
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Authenticate with $biometricName")
                        .setNegativeButtonText("Cancel")
                        .setDeviceCredentialAllowed(false)
                        .build()
                ) { showLoading ->
                    findViewById<View>(R.id.loading).visibility =
                        if (showLoading) View.VISIBLE else View.INVISIBLE
                }

                showToast("Succeeded")
            } catch (e: BiometricException) {
                if (e.shouldShow) {
                    showToast("Error | Message: ${e.errString} | Code: ${e.code}")
                }
            }
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }
}