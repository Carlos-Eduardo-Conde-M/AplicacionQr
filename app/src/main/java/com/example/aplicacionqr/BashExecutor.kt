package com.example.aplicacionqr

import android.util.Log
import java.io.*

class BashExecutor {

    fun executeScriptText(scriptContent: String): Boolean {
        return try {
            // Ejecuta el script como texto usando bash -c
            val process = ProcessBuilder("bash", "-c", scriptContent)
                .redirectErrorStream(true)
                .start()

            val output = readProcessOutput(process)
            val exitCode = process.waitFor()

            Log.d("BashExecutor", "📋 Output: $output")
            Log.d("BashExecutor", "🔚 Exit code: $exitCode")

            exitCode == 0
        } catch (e: Exception) {
            Log.e("BashExecutor", "❌ Error ejecutando script: ${e.message}")
            false
        }
    }

    private fun readProcessOutput(process: Process): String {
        return try {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            output.toString()
        } catch (e: Exception) {
            "Error reading output: ${e.message}"
        }
    }
}