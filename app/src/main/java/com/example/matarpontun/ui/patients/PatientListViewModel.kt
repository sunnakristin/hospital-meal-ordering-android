package com.example.matarpontun.ui.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.Patient
import com.example.matarpontun.domain.service.DailyOrderService
import com.example.matarpontun.domain.service.PatientService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class PatientListViewModel(
    private val patientService: PatientService,
    private val dailyOrderService: DailyOrderService
) : ViewModel() {

    private val _uiState = MutableStateFlow<PatientListUiState>(PatientListUiState.Idle)
    val uiState: StateFlow<PatientListUiState> = _uiState

    private var expandedIds: Set<Long> = emptySet()
    private var patients: List<Patient> = emptyList()
    private var todaysOrders: Map<Long, DailyOrder> = emptyMap()

    fun loadPatients(wardId: Long) {
        _uiState.value = PatientListUiState.Loading

        viewModelScope.launch {
            val patientResult = patientService.getPatientsByWard(wardId)
            val orderResult = dailyOrderService.getDailyOrdersForWard(wardId)

            patientResult.onSuccess {
                patients = it
            }.onFailure {
                _uiState.value = PatientListUiState.Error(it.message ?: "Error loading patients")
                return@launch
            }

            orderResult.onSuccess {
                todaysOrders = it.associateBy { order -> order.patient.patientId }
            }.onFailure {
                todaysOrders = emptyMap()
            }

            rebuildUi()
        }
    }

    fun orderForPatient(patientId: Long) {
        viewModelScope.launch {
            val patient = patients.find { it.patientId == patientId } ?: return@launch

            val result = dailyOrderService.createOrderForPatient(patient.patientId, patient.foodType)

            result.onSuccess { order ->
                todaysOrders = todaysOrders + (patientId to order)
                rebuildUi()
                _events.emit(PatientListEvent.ShowToast("Order created for ${patient.name}"))
            }.onFailure {
                _events.emit(PatientListEvent.ShowToast(it.message ?: "Order failed"))
            }
        }
    }

    fun orderForWard(wardId: Long) {
        viewModelScope.launch {
            val result = dailyOrderService.createOrdersForWard(wardId, patients)

            result.onSuccess { orders ->
                val updated = orders.associateBy { it.patient.patientId }
                todaysOrders = todaysOrders + updated
                rebuildUi()
                _events.emit(PatientListEvent.ShowToast("Orders created for ward"))
            }.onFailure {
                _events.emit(PatientListEvent.ShowToast(it.message ?: "Failed to order ward"))
            }
        }
    }

    fun toggleExpand(patientId: Long) {
        expandedIds = if (patientId in expandedIds) expandedIds - patientId else expandedIds + patientId
        rebuildUi()
    }

    fun fixConflicts(patientId: Long) {
        viewModelScope.launch {
            val result = dailyOrderService.fixConflicts(patientId)

            result.onSuccess { updatedOrder ->
                todaysOrders = todaysOrders + (patientId to updatedOrder)
                rebuildUi()
                _events.emit(PatientListEvent.ShowToast("Conflicts checked"))
            }.onFailure {
                _events.emit(PatientListEvent.ShowToast(it.message ?: "Unable to fix conflicts"))
            }
        }
    }

    private fun rebuildUi() {
        val rows = patients.map { patient ->
            val order = todaysOrders[patient.patientId]
            val status = order?.status  // raw backend/mock status

            val statusText = when (status) {
                "SUBMITTED" -> "SUBMITTED"
                "AUTO CHANGED" -> "AUTO CHANGED"
                "NEEDS MANUAL CHANGE" -> "NEEDS MANUAL CHANGE ⚠"
                null -> "Not ordered"
                else -> status
            }

            val showFixButton = (status == "NEEDS MANUAL CHANGE")

            // Primary button = ordering button, but we don’t want it to say ORDERED for conflicts.
            val primaryButtonText = when (status) {
                null -> "ORDER"
                "SUBMITTED" -> "SUBMITTED"
                "AUTO CHANGED" -> "AUTO CHANGED"
                "NEEDS MANUAL CHANGE" -> "REVIEW"
                else -> "ORDERED"
            }

            val primaryButtonEnabled = (status == null) // only order when no order exists

            PatientRowUi(
                patientId = patient.patientId,
                name = patient.name,
                room = patient.room,
                foodTypeName = patient.foodType.typeName,

                hasOrder = order != null,
                orderStatus = status,
                statusText = statusText,

                primaryButtonText = primaryButtonText,
                primaryButtonEnabled = primaryButtonEnabled,
                showFixButton = showFixButton,

                expanded = patient.patientId in expandedIds,

                breakfast = order?.breakfast?.name,
                lunch = order?.lunch?.name,
                afternoonSnack = order?.afternoonSnack?.name,
                dinner = order?.dinner?.name,
                nightSnack = order?.nightSnack?.name
            )
        }

        _uiState.value = PatientListUiState.Success(rows = rows, canOrderWard = canOrderWard())
    }

    // checks if there are patients without orders for the ward
    private fun canOrderWard(): Boolean {
        return patients.any { patient ->
            todaysOrders[patient.patientId] == null
        }
    }

    private val _events = MutableSharedFlow<PatientListEvent>()
    val events: SharedFlow<PatientListEvent> = _events

    sealed class PatientListEvent {
        data class NavigateToOrderDetails(val order: DailyOrder) : PatientListEvent()
        data class ShowToast(val message: String) : PatientListEvent()
    }

    sealed class PatientListUiState {
        object Idle : PatientListUiState()
        object Loading : PatientListUiState()
        data class Success(val rows: List<PatientRowUi>, val canOrderWard: Boolean) : PatientListUiState()
        data class Error(val message: String) : PatientListUiState()
    }

    data class PatientRowUi(
        val patientId: Long,
        val name: String,
        val room: String,
        val foodTypeName: String,
        val hasOrder: Boolean,
        val orderStatus: String?,     // raw status
        val statusText: String,       // pretty text
        val primaryButtonText: String,
        val primaryButtonEnabled: Boolean,
        val showFixButton: Boolean,
        val expanded: Boolean,
        val breakfast: String?,
        val lunch: String?,
        val afternoonSnack: String?,
        val dinner: String?,
        val nightSnack: String?
    )
}