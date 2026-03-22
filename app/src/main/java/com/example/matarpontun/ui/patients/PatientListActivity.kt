package com.example.matarpontun.ui.patients

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private lateinit var offlineBanner: TextView
    private lateinit var adapter: PatientListAdapter
    private lateinit var btnOrderWard: Button
    private lateinit var btnCreateRoom: Button
    private var wardId: Long = -1
    private var roomFilter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wardId = intent.getLongExtra("WARD_ID", -1)
        roomFilter = intent.getStringExtra("ROOM_FILTER")
        //wardId = 3

        setContentView(R.layout.activity_patient_list)

        btnOrderWard = findViewById(R.id.btnOrderWard)
        btnCreateRoom = findViewById(R.id.btnCreateRoom)
        offlineBanner = findViewById(R.id.offlineBanner)
        val btnRefresh = findViewById<ImageButton>(R.id.btnRefresh)
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
            },
            onRoomToggled = { roomName ->
                viewModel.toggleRoom(roomName)
            },
            onEditClicked = { row ->
                showEditPatientDialog(row)
            }
        )

        recyclerView.adapter = adapter

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
        if (roomFilter != null) {
            btnOrderWard.visibility = View.GONE
            btnCreateRoom.visibility = View.GONE
            btnRefresh.visibility = View.GONE
        }

        viewModel.setRoomFilter(roomFilter)
        viewModel.loadFoodTypesIfNeeded()
        viewModel.loadPatientsIfNeeded(wardId)

        btnBack.setOnClickListener {
            finish()   // goes back to previous screen
        }

        btnOrderWard.setOnClickListener {
            viewModel.orderForWard(wardId)
        }

        btnCreateRoom.setOnClickListener {
            showCreateRoomDialog()
        }

        btnRefresh.setOnClickListener {
            viewModel.loadPatients(wardId)
        }
    }

    private fun showEditPatientDialog(row: PatientListViewModel.PatientRowUi) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_patient, null)
        val etName = dialogView.findViewById<EditText>(R.id.etPatientName)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerFoodType)
        val etRestrictions = dialogView.findViewById<EditText>(R.id.etRestrictions)

        etName.setText(row.name)
        etRestrictions.setText(row.restrictions.joinToString(", "))

        val foodTypeNames = viewModel.getFoodTypeNames()
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, foodTypeNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        val currentIndex = foodTypeNames.indexOf(row.foodTypeName)
        if (currentIndex >= 0) spinner.setSelection(currentIndex)

        AlertDialog.Builder(this)
            .setTitle("Edit Patient")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val foodType = spinner.selectedItem?.toString() ?: row.foodTypeName
                val restrictions = etRestrictions.text.toString()
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                if (name.isNotEmpty()) {
                    viewModel.updatePatient(row.patientId, name, foodType, restrictions)
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCreateRoomDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_room, null)
        val etRoomNumber = dialogView.findViewById<EditText>(R.id.etRoomNumber)
        val etNumPatients = dialogView.findViewById<EditText>(R.id.etNumPatients)

        AlertDialog.Builder(this)
            .setTitle("Create Room")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val roomNumber = etRoomNumber.text.toString().trim()
                val numPatients = etNumPatients.text.toString().toIntOrNull() ?: 0
                if (roomNumber.isNotEmpty() && numPatients > 0) {
                    viewModel.createRoom(wardId, roomNumber, numPatients)
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
                        adapter.submitItems(state.items)
                        btnOrderWard.isEnabled = state.canOrderWard
                        offlineBanner.visibility = if (state.isOffline) View.VISIBLE else View.GONE
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

