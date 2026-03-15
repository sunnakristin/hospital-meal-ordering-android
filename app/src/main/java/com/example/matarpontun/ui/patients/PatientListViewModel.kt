package com.example.matarpontun.ui.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.Patient
import com.example.matarpontun.domain.service.DailyOrderService
import com.example.matarpontun.domain.service.PatientService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PatientListViewModel(
    private val patientService: PatientService,
    private val dailyOrderService: DailyOrderService
) : ViewModel() {

    private val _uiState = MutableStateFlow<PatientListUiState>(PatientListUiState.Idle)
    val uiState: StateFlow<PatientListUiState> = _uiState

    private val _events = MutableSharedFlow<PatientListEvent>()
    val events: SharedFlow<PatientListEvent> = _events

    private var expandedIds: Set<Long> = emptySet()
    private var patients: List<Patient> = emptyList()
    private var todaysOrders: Map<Long, DailyOrder> = emptyMap()

    fun loadPatients(wardId: Long) {
        _uiState.value = PatientListUiState.Loading

        viewModelScope.launch {

            val result = patientService.getPatientsByWard(wardId)

            result.onSuccess {
                patients = it
                rebuildUi()
            }.onFailure {
                _uiState.value =
                    PatientListUiState.Error(it.message ?: "Failed to load patients")
            }
        }
    }

    fun orderForPatient(patientId: Long) {

        viewModelScope.launch {

            val patient = patients.find { it.patientId == patientId } ?: return@launch

            val result = dailyOrderService.orderForPatient(
                patient.patientId,
                patient.foodType
            )

            result.onSuccess { order ->

                todaysOrders = todaysOrders + (patientId to order)

                rebuildUi()

                _events.emit(
                    PatientListEvent.ShowToast("Order placed for ${patient.name}")
                )

            }.onFailure {

                _events.emit(
                    PatientListEvent.ShowToast(it.message ?: "Order failed")
                )
            }
        }
    }

    fun orderForWard(wardId: Long) {

        viewModelScope.launch {

            val result = dailyOrderService.orderForWard(wardId)

            result.onSuccess {

                loadPatients(wardId)

                _events.emit(
                    PatientListEvent.ShowToast("Orders created for ward")
                )
            }.onFailure {

                _events.emit(
                    PatientListEvent.ShowToast(it.message ?: "Ward order failed")
                )
            }
        }
    }

    fun toggleExpand(patientId: Long) {
        expandedIds =
            if (patientId in expandedIds) expandedIds - patientId
            else expandedIds + patientId

        rebuildUi()
    }

    private fun rebuildUi() {

        val rows = patients.map { patient ->

            val order = todaysOrders[patient.patientId]

            PatientRowUi(
                patientId = patient.patientId,
                name = patient.name,
                room = patient.room,
                foodTypeName = patient.foodType.typeName,

                hasOrder = order != null,

                primaryButtonText =
                    if (order == null) "ORDER"
                    else "ORDERED",

                primaryButtonEnabled = order == null,

                expanded = patient.patientId in expandedIds,

                breakfast = order?.breakfast?.name,
                lunch = order?.lunch?.name,
                afternoonSnack = order?.afternoonSnack?.name,
                dinner = order?.dinner?.name,
                nightSnack = order?.nightSnack?.name
            )
        }

        _uiState.value = PatientListUiState.Success(rows)
    }

    sealed class PatientListEvent {
        data class ShowToast(val message: String) : PatientListEvent()
    }

    sealed class PatientListUiState {

        object Idle : PatientListUiState()

        object Loading : PatientListUiState()

        data class Success(
            val rows: List<PatientRowUi>
        ) : PatientListUiState()

        data class Error(
            val message: String
        ) : PatientListUiState()
    }

    data class PatientRowUi(
        val patientId: Long,
        val name: String,
        val room: String,
        val foodTypeName: String,
        val hasOrder: Boolean,
        val primaryButtonText: String,
        val primaryButtonEnabled: Boolean,
        val expanded: Boolean,
        val breakfast: String?,
        val lunch: String?,
        val afternoonSnack: String?,
        val dinner: String?,
        val nightSnack: String?
    )


}