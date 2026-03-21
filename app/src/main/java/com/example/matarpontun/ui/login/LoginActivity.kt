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
    private lateinit var btnCreateAccount: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = LoginViewModel(AppContainer.wardService)

        initViews()
        observeViewModel()
        setupListeners()
    }

    private fun initViews() {
        etWardName = findViewById(R.id.editWardName)
        etPassword = findViewById(R.id.editPassword)
        btnLogin = findViewById(R.id.buttonLogin)
        btnCreateAccount = findViewById(R.id.buttonCreateAccount)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            val wardName = etWardName.text.toString().trim()
            val password = etPassword.text.toString().trim()

            AppContainer.currentLoginRequest = LoginRequest(wardName, password)
            viewModel.login(wardName, password)
        }

        btnCreateAccount.setOnClickListener {
            val wardName = etWardName.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (wardName.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AppContainer.currentLoginRequest = LoginRequest(wardName, password)
            viewModel.createAccount(wardName, password)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is LoginUiState.Idle -> {
                        progressBar.visibility = View.GONE
                        setButtonsEnabled(true)
                    }

                    is LoginUiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        setButtonsEnabled(false)
                    }

                    is LoginUiState.Success -> {
                        Log.d("LOGIN", "Ward id from backend = ${state.ward.id}")
                        progressBar.visibility = View.GONE
                        setButtonsEnabled(true)

                        val intent = Intent(this@LoginActivity, WardActivity::class.java)
                        intent.putExtra("WARD_NAME", state.ward.wardName)
                        intent.putExtra("WARD_ID", state.ward.id)
                        startActivity(intent)
                        finish()
                    }

                    is LoginUiState.Error -> {
                        progressBar.visibility = View.GONE
                        setButtonsEnabled(true)

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

    private fun setButtonsEnabled(enabled: Boolean) {
        btnLogin.isEnabled = enabled
        btnCreateAccount.isEnabled = enabled
    }
}
