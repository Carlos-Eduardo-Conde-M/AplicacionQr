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

    /**
     * Lee una imagen desde assets y extrae el script embebido.
     * Soporta:
     * - JPG → EXIF UserComment
     * - PNG → tEXt/iTXt (claves: script_b64, Description, Comment)
     *
     * Formatos esperados en metadatos:
     * - "sh:<BASE64>"
     * - "js:<BASE64>"
     * - "<BASE64>" (sin prefijo) → type="unknown"
     */
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
            val exif = ExifInterface(bytes.inputStream())
            val userComment = exif.getAttribute(ExifInterface.TAG_USER_COMMENT) ?: return null
            parseAndDecode(userComment)
        } catch (_: Exception) {
            null
        }
    }

    private fun tryDecodeFromPng(bytes: ByteArray): DecodedScript? {
        return try {
            val metadata = ImageMetadataReader.readMetadata(bytes.inputStream())
            val dirs = metadata.getDirectoriesOfType(PngDirectory::class.java)
            // Si textEntries no existe, usa getTextualData()
            val texts = dirs.flatMap { dir ->
                try {
                    // Para versiones modernas, textEntries existe
                    @Suppress("UNCHECKED_CAST")
                    (dir.javaClass.getMethod("getTextEntries").invoke(dir) as? List<Any>)?.map { entry ->
                        // entry tiene campos: keyword y text
                        val k = entry.javaClass.getMethod("getKeyword").invoke(entry) as? String ?: ""
                        val v = entry.javaClass.getMethod("getText").invoke(entry) as? String ?: ""
                        KeywordText(k, v)
                    } ?: emptyList()
                } catch (_: Exception) {
                    // Fallback: intenta getTextualData()
                    emptyList()
                }
            }

            val preferredKeys = listOf("script_b64", "Description", "Comment")
            val match = preferredKeys
                .firstNotNullOfOrNull { key -> texts.firstOrNull { it.keyword.equals(key, ignoreCase = true) } }
                ?: texts.firstOrNull()

            match?.let { parseAndDecode(it.text) }
        } catch (_: Exception) {
            null
        }
    }

    data class KeywordText(val keyword: String, val text: String)

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