package com.example.matarpontun.ui.login


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.matarpontun.AppContainer
import com.example.matarpontun.data.remote.dto.LoginRequest
import com.example.matarpontun.R
import com.example.matarpontun.ui.ward.WardActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    private lateinit var etWardName: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // now we use network service
        viewModel = LoginViewModel(AppContainer.wardService)

        initViews()
        observeViewModel()
        setupListeners()
    }

    private fun initViews() {
        etWardName = findViewById(R.id.editWardName)
        etPassword = findViewById(R.id.editPassword)
        btnLogin = findViewById(R.id.buttonLogin)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            val wardName = etWardName.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Store credentials so patient endpoints that require WardDTO can use them
            AppContainer.currentLoginRequest = LoginRequest(wardName, password)

            viewModel.login(wardName, password)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {

                    is LoginUiState.Idle -> {
                        progressBar.visibility = View.GONE
                        btnLogin.isEnabled = true
                    }

                    is LoginUiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        btnLogin.isEnabled = false
                    }

                    is LoginUiState.Success -> {

                        Log.d("LOGIN", "Ward id from backend = ${state.ward.id}")

                        progressBar.visibility = View.GONE
                        btnLogin.isEnabled = true

                        // Persist ward id for offline-first repository and session store
                        AppContainer.currentWardId = state.ward.id
                        launch {
                            AppContainer.wardSessionDataStore.saveSession(
                                wardId = state.ward.id,
                                wardName = state.ward.wardName,
                                password = etPassword.text.toString().trim()
                            )
                        }

                        val intent = Intent(this@LoginActivity, WardActivity::class.java)
                        intent.putExtra("WARD_NAME", state.ward.wardName)
                        intent.putExtra("WARD_ID", state.ward.id)
                        startActivity(intent)
                        finish()

                    }

                    is LoginUiState.Error -> {
                        progressBar.visibility = View.GONE
                        btnLogin.isEnabled = true

                        Toast.makeText(
                            this@LoginActivity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}
