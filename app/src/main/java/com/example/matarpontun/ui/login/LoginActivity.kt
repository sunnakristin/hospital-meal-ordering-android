package com.example.matarpontun.ui.login


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.matarpontun.R
import com.example.matarpontun.data.repository.MockWardRepository
import com.example.matarpontun.domain.service.WardService
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

        // Create dependencies manually (simple way)
        val repository = MockWardRepository()
        val service = WardService(repository)
        viewModel = LoginViewModel(service)

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
                        progressBar.visibility = View.GONE
                        btnLogin.isEnabled = true

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
