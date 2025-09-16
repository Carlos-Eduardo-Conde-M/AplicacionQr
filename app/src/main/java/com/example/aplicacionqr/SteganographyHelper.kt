package com.example.aplicacionqr

import android.app.Activity
import android.graphics.BitmapFactory
import com.ayush.imagesteganographylibrary.Text.ImageSteganography
import com.ayush.imagesteganographylibrary.Text.TextDecoding
import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextDecodingCallback
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SteganographyHelper(private val activity: Activity) {

    suspend fun extractFromResource(drawableResId: Int, secretKey: String = ""): File? =
        suspendCoroutine { cont ->
            val bitmap = BitmapFactory.decodeResource(activity.resources, drawableResId)
            val imageSteganography = ImageSteganography(secretKey, bitmap)
            val textDecoding = TextDecoding(activity, object : TextDecodingCallback {
                override fun onStartTextEncoding() {}

                override fun onCompleteTextEncoding(result: ImageSteganography?) {
                    if (result != null && result.isDecoded && !result.isSecretKeyWrong) {
                        val message = result.message
                        // Guarda el mensaje como archivo temporal
                        val file = File.createTempFile("script_", ".sh", activity.cacheDir)
                        file.writeText(message)
                        cont.resume(file)
                    } else {
                        cont.resume(null)
                    }
                }
            })
            textDecoding.execute(imageSteganography)
        }
}
