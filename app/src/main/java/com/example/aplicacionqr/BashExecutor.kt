package com.example.aplicacionqr

import android.util.Log
import java.io.*

class BashExecutor {

    companion object {
        private const val TAG = "BashExecutor"
    }

    fun executeScript(scriptFile: File): Boolean {
        return try {
            Log.d(TAG, "🔧 Ejecutando script: ${scriptFile.absolutePath}")

            // Verificar que el script existe y es ejecutable
            if (!scriptFile.exists() || !scriptFile.canExecute()) {
                Log.e(TAG, "❌ Script no ejecutable")
                return false
            }

            // Ejecutar el script
            val process = ProcessBuilder("sh", scriptFile.absolutePath)
                .redirectErrorStream(true)
                .start()

            // Leer output
            val output = readProcessOutput(process)
            val exitCode = process.waitFor()

            Log.d(TAG, "📋 Output: $output")
            Log.d(TAG, "🔚 Exit code: $exitCode")

            exitCode == 0

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error ejecutando script: ${e.message}")
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