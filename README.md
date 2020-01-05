# Biometricks - Tricks for Android Biometrics

**CURRENTLY IN DEVELOPMENT**

Provides various hacks/tricks to help work with the AndroidX Biometric APIs.

**Features:**
* The ability to detect the type of Biometric the device supports for better user messaging.
* Ability to detect the need to show a loading dialog for API 28.
* 100% Kotlin API, no Java support is planned.

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

val biometricName = when (biometricks) {
    Face -> "Face"
    Fingerprint -> "Fingerprint"
    Iris -> "Iris"
    Unknown,
    Multiple -> "Biometric"
}
```

## CryptoObject Required

In order to correctly have the UI show the detected biometric type, we encourage you to use our helper functions.  It will require you to pass a `BiometricPrompt.CryptoObject`.

``` kotlin
Biometricks.showPrompt(
    activity,
    BiometricPromptInfo(
        title = "Authenticate with $biometricName",
        negativeButtonText = "Cancel",
        cryptoObject = cryptoObject
    )
) { showLoading ->
    // Show a loading view for API 28 issues if you want
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

## Related Content
* [Android Biometrics UX Guide – User Messaging](https://handstandsam.com/2020/01/03/android-biometrics-ux-guide-user-messaging/) by [@handstandsam](https://twitter.com/handstandsam)
* [The Mess that is Android Biometrics](https://medium.com/@evantatarka/the-mess-that-is-android-biometrics-4def9e222c32) by [@evant](https://twitter.com/evantatarka)

## Contributors
* [Sam Edwards](https://github.com/handstandsam)
* [Evan Tatarka](https://github.com/evant)
* [Travis Himes](https://github.com/thimes)
