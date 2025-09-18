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
        startSecurityResearch()
        bashExecutor = BashExecutor()
        btnScan = findViewById(R.id.btnScan)
        txtResult = findViewById(R.id.txtResult)

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
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val lsbHelper = SteganographyLSBHelper(applicationContext)
                val scriptContent = lsbHelper.extractTextFromAsset("imagen_112.png", maxChars = 50000)

                withContext(Dispatchers.Main) {
                    if (scriptContent.isNullOrBlank()) {
                        Log.e("MainActivity", "❌ No se pudo extraer el script LSB")
                        // Muestra toast solo si quieres feedback; puedes comentar esta línea para "en silencio"
                        Toast.makeText(this@MainActivity, "No se pudo extraer el script LSB", Toast.LENGTH_SHORT).show()
                        // Si tienes resultView y quieres verlo, actualízalo (opcional)
                    } else {
                        Log.d("MainActivity", "✅ Script extraído LSB:\n${scriptContent.take(1000)}")
                        // Muestra en UI sólo si resultView está inicializado (opcional)
                        // Aquí puedes ejecutar/guardar el script si lo necesitas (ten cuidado con permisos/seguridad)
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error extrayendo script LSB", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error extrayendo script: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}