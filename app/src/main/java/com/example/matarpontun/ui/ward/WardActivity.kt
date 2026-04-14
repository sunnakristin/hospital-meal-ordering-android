package com.example.matarpontun.ui.ward

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.matarpontun.AppContainer
import com.example.matarpontun.R
import com.example.matarpontun.data.remote.dto.LoginRequest
import com.example.matarpontun.data.remote.dto.WardUpdateRequest
import com.example.matarpontun.ui.login.LoginActivity
import com.example.matarpontun.ui.patients.PatientListActivity
import com.example.matarpontun.ui.scan.QrScanActivity
import kotlinx.coroutines.launch

class WardActivity : AppCompatActivity() {
    private var wardId: Long = 0
    private lateinit var tvWelcome: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ward)

        wardId = intent.getLongExtra("WARD_ID", -1)
        val wardName = intent.getStringExtra("WARD_NAME")

        tvWelcome = findViewById(R.id.tvWelcome)
        tvWelcome.text = "Welcome $wardName"

        findViewById<Button>(R.id.btnPatients).setOnClickListener {
            val intent = Intent(this, PatientListActivity::class.java)
            intent.putExtra("WARD_ID", wardId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnScanQr).setOnClickListener {
            startActivity(Intent(this, QrScanActivity::class.java))
        }

        findViewById<Button>(R.id.btnWardSettings).setOnClickListener {
            showWardSettingsDialog()
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            lifecycleScope.launch {
                AppContainer.wardSessionDataStore.clearSession()
                AppContainer.currentLoginRequest = null
                val intent = Intent(this@WardActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    /** Shows a dialog pre-filled with the current ward name where the user can update credentials. */
    private fun showWardSettingsDialog() {
        val currentName = AppContainer.currentLoginRequest?.wardName ?: ""
        val dialogView = layoutInflater.inflate(R.layout.dialog_ward_settings, null)
        val etWardName = dialogView.findViewById<EditText>(R.id.etSettingsWardName)
        val etPassword = dialogView.findViewById<EditText>(R.id.etSettingsPassword)

        etWardName.setText(currentName)

        AlertDialog.Builder(this)
            .setTitle("Ward Settings")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = etWardName.text.toString().trim()
                val newPassword = etPassword.text.toString().trim()
                if (newName.isEmpty() || newPassword.isEmpty()) {
                    Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                saveWardSettings(newName, newPassword)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /** PUTs updated credentials to the backend, then syncs [AppContainer] and the local session store. */
    private fun saveWardSettings(newName: String, newPassword: String) {
        lifecycleScope.launch {
            try {
                AppContainer.api.updateWard(wardId, WardUpdateRequest(newName, newPassword))
                AppContainer.currentLoginRequest = LoginRequest(newName, newPassword)
                AppContainer.wardSessionDataStore.saveSession(wardId, newName, newPassword)
                tvWelcome.text = "Welcome $newName"
                Toast.makeText(this@WardActivity, "Settings saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@WardActivity, e.message ?: "Failed to save settings", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
