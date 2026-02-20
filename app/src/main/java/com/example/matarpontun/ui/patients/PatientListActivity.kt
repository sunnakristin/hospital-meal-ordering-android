package com.example.matarpontun.ui.patients

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.matarpontun.R
import com.example.matarpontun.data.repository.MockPatientRepository
import com.example.matarpontun.domain.service.PatientService
import kotlinx.coroutines.launch

class PatientListActivity : AppCompatActivity() {

    private lateinit var viewModel: PatientListViewModel

    private lateinit var listView: ListView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)

        val repo = MockPatientRepository()
        val service = PatientService(repo)
        viewModel = PatientListViewModel(service)

        listView = findViewById(R.id.listPatients)
        progressBar = findViewById(R.id.progressBar)

        val wardId = intent.getLongExtra("WARD_ID", -1)

        observe()
        viewModel.loadPatients(wardId)
    }

    private fun observe() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is PatientListUiState.Idle -> Unit

                    is PatientListUiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                    }

                    is PatientListUiState.Success -> {
                        progressBar.visibility = View.GONE

                        val rows = state.patients.map { p ->
                            "${p.name} — Room ${p.room}"
                        }

                        listView.adapter = ArrayAdapter(
                            this@PatientListActivity,
                            android.R.layout.simple_list_item_1,
                            rows
                        )
                    }

                    is PatientListUiState.Error -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@PatientListActivity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}
