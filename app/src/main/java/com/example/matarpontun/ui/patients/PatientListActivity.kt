package com.example.matarpontun.ui.patients

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.matarpontun.R
import com.example.matarpontun.data.repository.MockDailyOrderRepository
import com.example.matarpontun.data.repository.MockPatientRepository
import com.example.matarpontun.domain.service.DailyOrderService
import com.example.matarpontun.domain.service.PatientService
import com.example.matarpontun.ui.patients.PatientListViewModel.PatientListUiState

import kotlinx.coroutines.launch

class PatientListActivity : AppCompatActivity() {
    private lateinit var viewModel: PatientListViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: PatientListAdapter

    // singleton container for repositories and services - now order survivies navigation while app is running
    object AppContainer {
        val dailyOrderRepository = MockDailyOrderRepository()
        val dailyOrderService = DailyOrderService(dailyOrderRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)

        val btnBack = findViewById<Button>(R.id.btnBack)

        val patientRepo = MockPatientRepository()
        val patientService = PatientService(patientRepo)

        viewModel = PatientListViewModel(
            patientService,
            AppContainer.dailyOrderService
        )

        recyclerView = findViewById(R.id.recyclerPatients)
        recyclerView.layoutManager = LinearLayoutManager(this)

        progressBar = findViewById(R.id.progressBar)

        adapter = PatientListAdapter(
            onOrderClicked = { patientId ->
                viewModel.orderForPatient(patientId)
            },
            onToggleClicked = { patientId ->
                viewModel.toggleExpand(patientId)
            }
        )

        recyclerView.adapter = adapter

        val wardId = intent.getLongExtra("WARD_ID", -1)

        observe()

        lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is PatientListViewModel.PatientListEvent.ShowToast -> {
                        Toast.makeText(
                            this@PatientListActivity,
                            event.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is PatientListViewModel.PatientListEvent.NavigateToOrderDetails -> {
                        // Later: start OrderDetailsActivity
                        // For now we show toast
                    }
                }
            }
        }
        viewModel.loadPatients(wardId)

        btnBack.setOnClickListener {
            finish()   // goes back to previous screen
        }
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
                        adapter.submitRows(state.rows)                    }

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

