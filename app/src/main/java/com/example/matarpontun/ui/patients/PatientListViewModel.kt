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

    private var expandedIds: Set<Long> = emptySet()
    private var patients: List<Patient> = emptyList()
    private var todaysOrders: Map<Long, DailyOrder> = emptyMap()
    val uiState: StateFlow<PatientListUiState> = _uiState

    fun loadPatients(wardId: Long) {
        _uiState.value = PatientListUiState.Loading

        viewModelScope.launch {

            val patientResult = patientService.getPatientsByWard(wardId)
            val orderResult = dailyOrderService.getDailyOrdersForWard(wardId) // must exist

            patientResult.onSuccess {
                patients = it
            }.onFailure {
                _uiState.value = PatientListUiState.Error(it.message ?: "Error loading patients")
                return@launch
            }

            orderResult.onSuccess {
                todaysOrders = it.associateBy { order -> order.patient.patientId }
            }.onFailure {
                // If orders fail, still show patients
                todaysOrders = emptyMap()
            }

            rebuildUi()
        }
    }

    fun orderForPatient(patientId: Long) {

        viewModelScope.launch {

            val patient = patients.find { it.patientId == patientId } ?: return@launch

            val result = dailyOrderService.createOrderForPatient(
                patient.patientId,
                patient.foodType
            )

            result.onSuccess { order ->

                // Update in-memory orders
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

            val result = dailyOrderService.createOrdersForWard(
                wardId,
                patients
            )

            result.onSuccess { orders ->

                // Update todaysOrders map
                val updated = orders.associateBy { it.patient.patientId }

                todaysOrders = todaysOrders + updated

                rebuildUi()

                _events.emit(
                    PatientListEvent.ShowToast("Orders created for ward")
                )

            }.onFailure {
                _events.emit(
                    PatientListEvent.ShowToast(it.message ?: "Failed to order ward")
                )
            }
        }
    }

    fun toggleExpand(patientId: Long) {
        expandedIds = if (patientId in expandedIds)
            expandedIds - patientId
        else
            expandedIds + patientId

        rebuildUi()
    }

    fun fixConflicts(patientId: Long) {
        viewModelScope.launch {
            val result = dailyOrderService.fixConflicts(patientId)

            result.fold(
                onSuccess = { updatedOrder ->
                    todaysOrders = todaysOrders + (patientId to updatedOrder)
                   rebuildUi()
                },
                onFailure = {
                    _events.emit(PatientListEvent.ShowToast("Unable to fix conflicts"))
                }
            )
        }
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
                statusText = when (order?.status) {
                    "SUBMITTED" -> "SUBMITTED"
                    "AUTO CHANGED" -> "AUTO CHANGED"
                    "NEEDS MANUAL CHANGE" -> "⚠ NEEDS MANUAL CHANGE"
                    null -> "Not ordered"
                    else -> order.status
                },

                expanded = patient.patientId in expandedIds,
                canFixConflicts = order?.status == "NEEDS MANUAL CHANGE",

                breakfast = order?.breakfast?.name,
                lunch = order?.lunch?.name,
                afternoonSnack = order?.afternoonSnack?.name,
                dinner = order?.dinner?.name,
                nightSnack = order?.nightSnack?.name
            )
        }

        _uiState.value = PatientListUiState.Success(rows)
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

        data class Success(
            val rows: List<PatientRowUi>
        ) : PatientListUiState()
        data class Error(val message: String) : PatientListUiState()
    }
    data class PatientRowUi(
        val patientId: Long,
        val name: String,
        val room: String,
        val foodTypeName: String,
        val statusText: String,
        val hasOrder: Boolean,
        val expanded: Boolean,
        val canFixConflicts: Boolean,
        val breakfast: String?,
        val lunch: String?,
        val afternoonSnack: String?,
        val dinner: String?,
        val nightSnack: String?
    )
}
