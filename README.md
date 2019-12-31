# Biometricks - Tricks for Android Biometrics

Provides the ability to detect the type of Biometric the device supports for better user messaging.


<table>
<tr>
<th>Fingerprint</th>
<th>Face</th>
</tr>
<tr>
<td><img src="https://github.com/handstandsam/biometricks/raw/master/static/images/sample_fingerprint.gif" height="400"/></td>
<td><img src="https://github.com/handstandsam/biometricks/raw/master/static/images/sample_face.gif" height="400"/></td>
</tr>
</table>

# Why?
The [AndroidX Biometric Library](https://developer.android.com/jetpack/androidx/releases/biometric) doesn't tell you what type of Biometric feature the device has and will be used.  This library figures that out for you so you can create better user messaging than just "Biometric" in most cases.


## Usage
``` kotlin
val biometricks = Biometricks.from(applicationContext)

val userMessage = when (biometricks) {
    Face -> "Face"
    Fingerprint -> "Fingerprint"
    Iris -> "Iris"
    Unknown,
    Multiple -> "Biometric"
}
```

**NOTE**: *This type is only valid when you show a [`BiometricPrompt`](https://developer.android.com/reference/android/hardware/biometrics/BiometricPrompt) and send in a [`CryptoObject`](https://developer.android.com/reference/android/hardware/biometrics/BiometricPrompt.CryptoObject).*

## The Types of Biometricks ([Source](https://github.com/handstandsam/biometricks/blob/master/biometricks/src/main/java/com/handstandsam/biometricks/Biometricks.kt))
``` kotlin
sealed class Biometricks {

    /** The device has no support for Biometric Authentication */
    object None : Biometricks()

    /** The device has support for Biometric Authentication */
    sealed class Available : Biometricks() {
        object Face : Available()
        object Fingerprint : Available()
        object Iris : Available()
        object Multiple : Available()
        object Unknown : Available()
    }
}
```

## Contributors
* [Sam Edwards](https://github.com/handstandsam)
* [Evan Tatarka](https://github.com/evant)
* [Travis Himes](https://github.com/thimes)