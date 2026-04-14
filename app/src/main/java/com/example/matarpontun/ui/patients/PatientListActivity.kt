package com.example.matarpontun.ui.patients

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.ProgressBar
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.matarpontun.AppContainer
import com.example.matarpontun.R
import com.example.matarpontun.data.remote.dto.PatientConflictDto
import com.example.matarpontun.data.remote.dto.PatientSummaryDto
import com.example.matarpontun.domain.model.ConflictInfo
import com.example.matarpontun.ui.patients.PatientListViewModel.PatientListUiState

import kotlinx.coroutines.launch

class PatientListActivity : AppCompatActivity() {
    private lateinit var viewModel: PatientListViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var composeBoxView: ComposeView
    private lateinit var progressBar: ProgressBar
    private lateinit var offlineBanner: TextView
    private lateinit var adapter: PatientListAdapter
    private lateinit var btnOrderWard: Button
    private lateinit var btnCreateRoom: Button
    private var wardId: Long = -1
    private var roomFilter: String? = null
    private var isBoxView = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wardId = intent.getLongExtra("WARD_ID", -1)
        roomFilter = intent.getStringExtra("ROOM_FILTER")

        setContentView(R.layout.activity_patient_list)

        btnOrderWard = findViewById(R.id.btnOrderWard)
        btnCreateRoom = findViewById(R.id.btnCreateRoom)
        offlineBanner = findViewById(R.id.offlineBanner)
        composeBoxView = findViewById(R.id.composeBoxView)
        val btnRefresh = findViewById<ImageButton>(R.id.btnRefresh)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnToggleView = findViewById<Button>(R.id.btnToggleView)
        val btnViewMenu = findViewById<Button>(R.id.btnViewMenu)

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
                    is PatientListViewModel.PatientListEvent.ShowPatientFillDialogs -> {
                        showFillPatientDialogs(event.patients, 0)
                    }
                    is PatientListViewModel.PatientListEvent.ShowConflictDialog -> {
                        showPatientConflictDialog(event.patientName, event.conflicts)
                    }
                    is PatientListViewModel.PatientListEvent.ShowWardConflictDialog -> {
                        showWardConflictDialog(event.patientConflicts)
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
            finish()
        }

        btnToggleView.setOnClickListener {
            isBoxView = !isBoxView
            if (isBoxView) {
                recyclerView.visibility = View.GONE
                composeBoxView.visibility = View.VISIBLE
                btnToggleView.text = "☰ List View"
            } else {
                recyclerView.visibility = View.VISIBLE
                composeBoxView.visibility = View.GONE
                btnToggleView.text = "⊞ Box View"
            }
        }

        btnViewMenu.setOnClickListener {
            startActivity(Intent(this, com.example.matarpontun.ui.menu.MenuActivity::class.java))
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
        val btnCancelMeal = dialogView.findViewById<android.widget.Button>(R.id.btnCancelMeal)

        etName.setText(row.name)
        etRestrictions.setText(row.restrictions.joinToString(", "))

        val foodTypeNames = viewModel.getFoodTypeNames()
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, foodTypeNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        val currentIndex = foodTypeNames.indexOf(row.foodTypeName)
        if (currentIndex >= 0) spinner.setSelection(currentIndex)

        // Only show the cancel button if the patient already has an order today
        if (row.hasOrder) {
            btnCancelMeal.visibility = View.VISIBLE
        }

        val dialog = AlertDialog.Builder(this)
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
            .setNegativeButton("Close", null)
            .create()

        btnCancelMeal.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cancel Today's Meal")
                .setMessage("Are you sure you want to cancel the meal for ${row.name}?")
                .setPositiveButton("Yes, Cancel") { _, _ ->
                    viewModel.cancelTodaysOrder(row.patientId)
                    dialog.dismiss()
                }
                .setNegativeButton("No", null)
                .show()
        }

        dialog.show()
    }

    private fun showCreateRoomDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_room, null)
        val etRoomNumber = dialogView.findViewById<EditText>(R.id.etRoomNumber)
        val etNumPatients = dialogView.findViewById<EditText>(R.id.etNumPatients)
        val cbAutoFill = dialogView.findViewById<CheckBox>(R.id.cbAutoFill)

        AlertDialog.Builder(this)
            .setTitle("Create Room")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val roomNumber = etRoomNumber.text.toString().trim()
                val numPatients = etNumPatients.text.toString().toIntOrNull() ?: 0
                if (roomNumber.isNotEmpty() && numPatients > 0) {
                    if (cbAutoFill.isChecked) {
                        viewModel.createRoom(wardId, roomNumber, numPatients)
                    } else {
                        viewModel.createRoomManual(wardId, roomNumber, numPatients)
                    }
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFillPatientDialogs(patients: List<PatientSummaryDto>, index: Int) {
        if (index >= patients.size) {
            viewModel.loadPatients(wardId)
            return
        }
        val patient = patients[index]
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_patient, null)
        val etName = dialogView.findViewById<EditText>(R.id.etPatientName)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerFoodType)
        val etRestrictions = dialogView.findViewById<EditText>(R.id.etRestrictions)

        val foodTypeNames = viewModel.getFoodTypeNames()
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, foodTypeNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        AlertDialog.Builder(this)
            .setTitle("Patient ${index + 1} of ${patients.size} — Bed ${patient.bedNumber}")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Next") { _, _ ->
                val name = etName.text.toString().trim()
                val foodType = spinner.selectedItem?.toString() ?: patient.foodType
                val restrictions = etRestrictions.text.toString()
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                if (name.isNotEmpty()) {
                    viewModel.patchPatientQuiet(patient.id, name, foodType, restrictions) {
                        showFillPatientDialogs(patients, index + 1)
                    }
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    showFillPatientDialogs(patients, index)
                }
            }
            .show()
    }

    /** Shows a conflict dialog after ordering for a single patient. */
    private fun showPatientConflictDialog(patientName: String, conflicts: List<ConflictInfo>) {
        val message = buildString {
            append("Restriction conflict(s) found for $patientName:\n\n")
            conflicts.forEach { c ->
                append("• ${c.slot}: \"${c.originalMeal}\" contains \"${c.matchedRestriction}\"\n")
                if (c.replacementMeal != null) {
                    append("  → Auto-replaced with \"${c.replacementMeal}\"\n")
                } else {
                    append("  → No alternative found — manual change required\n")
                }
                append("\n")
            }
        }
        AlertDialog.Builder(this)
            .setTitle("⚠ Dietary Conflict Warning")
            .setMessage(message.trim())
            .setPositiveButton("OK", null)
            .show()
    }

    /** Shows a conflict summary dialog after ordering for the entire ward. */
    private fun showWardConflictDialog(patientConflicts: List<PatientConflictDto>) {
        val message = buildString {
            append("${patientConflicts.size} patient(s) had restriction conflicts:\n\n")
            patientConflicts.forEach { pc ->
                append("${pc.patientName} (${pc.status}):\n")
                pc.conflicts.forEach { c ->
                    append("  • ${c.slot}: \"${c.originalMeal}\" contains \"${c.matchedRestriction}\"\n")
                    if (c.replacementMeal != null) {
                        append("    → Auto-replaced with \"${c.replacementMeal}\"\n")
                    } else {
                        append("    → Manual change required\n")
                    }
                }
                append("\n")
            }
        }
        AlertDialog.Builder(this)
            .setTitle("⚠ Dietary Conflict Warning")
            .setMessage(message.trim())
            .setPositiveButton("OK", null)
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
                        composeBoxView.setContent {
                            com.example.matarpontun.ui.theme.MatarpontunTheme {
                                PatientBoxView(items = state.boxItems)
                            }
                        }
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

