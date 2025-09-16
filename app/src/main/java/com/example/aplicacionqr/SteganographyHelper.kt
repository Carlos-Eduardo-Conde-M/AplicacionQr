package com.example.aplicacionqr

import android.content.Context
import android.util.Log
import androidx.annotation.DrawableRes
import java.io.InputStream

class SteganographyLSBHelper(private val context: Context) {


    fun extractTextFromImage(@DrawableRes drawableResId: Int, maxChars: Int = 2048): String? {
        try {
            val inputStream: InputStream = context.resources.openRawResource(drawableResId)
            val bytes = inputStream.readBytes()
            inputStream.close()

            val isBmp = bytes.size > 2 && bytes[0] == 'B'.code.toByte() && bytes[1] == 'M'.code.toByte()
            val pixelDataOffset = if (isBmp) {
                (bytes[10].toInt() and 0xFF) or
                        ((bytes[11].toInt() and 0xFF) shl 8) or
                        ((bytes[12].toInt() and 0xFF) shl 16) or
                        ((bytes[13].toInt() and 0xFF) shl 24)
            } else 0

            val bits = mutableListOf<Int>()
            var i = pixelDataOffset
            var pixelCount = 0
            while (i + 2 < bytes.size && pixelCount < maxChars * 2) {
                // B, G, R canales por píxel
                for (c in 0..2) {
                    for (bit in 0..3) {
                        bits.add((bytes[i + c].toInt() shr bit) and 1)
                    }
                }
                i += 3
                pixelCount++
            }

            // Reconstruir bytes
            val chars = StringBuilder()
            var ffCount = 0
            for (j in 0 until bits.size step 8) {
                if (j + 7 >= bits.size) break
                var byte = 0
                for (b in 0..7) {
                    byte = byte or (bits[j + b] shl (7 - b))
                }
                if (byte == 0) break
                chars.append(byte.toChar())
                if (byte == 0xFF) {
                    ffCount++
                    if (ffCount >= 8) break
                } else {
                    ffCount = 0
                }
            }
            val hexPreview = chars.toString().take(64).toCharArray().joinToString(" ") { "%02x".format(it.code) }
            Log.d("LSBHelper", "HEX 4LSB primeros 64: $hexPreview")
            Log.d("LSBHelper", "Texto extraído 4LSB: ${chars.toString().take(200)}")
            return chars.toString()
        } catch (e: Exception) {
            Log.e("LSBHelper", "Error al extraer texto: ${e.message}")
            return null
        }
    }
}