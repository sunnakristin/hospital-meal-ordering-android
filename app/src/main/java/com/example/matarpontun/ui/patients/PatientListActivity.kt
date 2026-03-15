package com.example.matarpontun.ui.patients

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.matarpontun.AppContainer
import com.example.matarpontun.R
import com.example.matarpontun.ui.patients.PatientListViewModel.PatientListUiState

import kotlinx.coroutines.launch

class PatientListActivity : AppCompatActivity() {
    private lateinit var viewModel: PatientListViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: PatientListAdapter
    private lateinit var btnOrderWard: Button
    private var wardId: Long = -1

    // singleton container for repositories and services - now order survivies navigation while app is running
    /*object AppContainer {
        val dailyOrderRepository = MockDailyOrderRepository()
        val dailyOrderService = DailyOrderService(dailyOrderRepository)
    }*/
    // -> hef núna í sér skrá

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wardId = intent.getLongExtra("WARD_ID", -1)
        //wardId = 3

        setContentView(R.layout.activity_patient_list)

        btnOrderWard = findViewById(R.id.btnOrderWard)
        val btnBack = findViewById<Button>(R.id.btnBack)

        viewModel = ViewModelProvider(
            this,
            PatientListViewModelFactory(
                AppContainer.patientService,
                AppContainer.dailyOrderService
            )
        )[PatientListViewModel::class.java]

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

        //wardId = intent.getLongExtra("WARD_ID", -1)

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
                }
            }
        }
        viewModel.loadPatientsIfNeeded(wardId)

        btnBack.setOnClickListener {
            finish()   // goes back to previous screen
        }

        btnOrderWard.setOnClickListener {
            viewModel.orderForWard(wardId) // order for ward
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
                        adapter.submitRows(state.rows)
                        btnOrderWard.isEnabled = state.canOrderWard
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

