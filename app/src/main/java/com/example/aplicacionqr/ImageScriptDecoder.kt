package com.example.aplicacionqr

import android.content.Context
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.png.PngDirectory
import java.nio.charset.StandardCharsets

data class DecodedScript(
    val type: String,   // "sh", "js" o "unknown"
    val code: String    // contenido del script en texto plano UTF-8
)

object ImageScriptDecoder {

    
    fun decodeFromAssets(context: Context, assetName: String): DecodedScript? {
        val bytes = try {
            context.assets.open(assetName).use { it.readBytes() }
        } catch (_: Exception) {
            return null
        }

        // 1) Intento JPEG (EXIF)
        val exifResult = tryDecodeFromExif(bytes)
        if (exifResult != null) return exifResult

        // 2) Intento PNG (tEXt/iTXt)
        val pngResult = tryDecodeFromPng(bytes)
        if (pngResult != null) return pngResult

        return null
    }

    private fun tryDecodeFromExif(bytes: ByteArray): DecodedScript? {
        return try {
            ExifInterface(bytes.inputStream()).use { exif ->
                val userComment = exif.getAttribute(ExifInterface.TAG_USER_COMMENT) ?: return null
                parseAndDecode(userComment)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun tryDecodeFromPng(bytes: ByteArray): DecodedScript? {
        return try {
            val metadata = ImageMetadataReader.readMetadata(bytes.inputStream())
            val dirs = metadata.getDirectoriesOfType(PngDirectory::class.java)
            val texts = dirs.flatMap { it.textEntries ?: emptyList() }

            val preferredKeys = listOf("script_b64", "Description", "Comment")
            val match = preferredKeys
                .firstNotNullOfOrNull { key -> texts.firstOrNull { it.keyword.equals(key, ignoreCase = true) } }
                ?: texts.firstOrNull() // último recurso: primer texto disponible

            match?.let { parseAndDecode(it.text) }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseAndDecode(raw: String): DecodedScript? {
        val trimmed = raw.trim()
        val type: String
        val b64: String

        when {
            trimmed.startsWith("sh:", ignoreCase = true) -> {
                type = "sh"
                b64 = trimmed.removePrefix("sh:").trimStart()
            }
            trimmed.startsWith("js:", ignoreCase = true) -> {
                type = "js"
                b64 = trimmed.removePrefix("js:").trimStart()
            }
            else -> {
                type = "unknown"
                b64 = trimmed
            }
        }

        return base64ToUtf8(b64)?.let { DecodedScript(type = type, code = it) }
    }

    private fun base64ToUtf8(b64: String): String? {
        return try {
            val clean = b64.replace("\r", "").replace("\n", "").trim()
            val decoded = Base64.decode(clean, Base64.DEFAULT)
            String(decoded, StandardCharsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }
}