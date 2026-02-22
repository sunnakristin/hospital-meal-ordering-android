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

    fun loadPatients(wardId: Long) {
        _uiState.value = PatientListUiState.Loading

        viewModelScope.launch {
            val result = patientService.getPatientsByWard(wardId)
            _uiState.value = result.fold(
                onSuccess = { patients -> PatientListUiState.Success(patients) },
                onFailure = { error -> PatientListUiState.Error(error.message ?: "Unknown error") }
            )
        }
    }

    fun orderForPatient(patient: Patient) {
        val current = _uiState.value
        if (current !is PatientListUiState.Success) return

        // If already ordered or already ordering, ignore click
        if (patient.patientId in current.orderedPatientIds) return
        if (patient.patientId in current.orderingPatientIds) return

        // Mark as ORDERING immediately
        _uiState.value = current.copy(
            orderingPatientIds = current.orderingPatientIds + patient.patientId
        )

        viewModelScope.launch {
            val result = dailyOrderService.createOrderForPatient(patient.patientId, patient.foodType)

            result.fold(
                onSuccess = { order ->
                    val latest = _uiState.value
                    if (latest is PatientListUiState.Success) {
                        _uiState.value = latest.copy(
                            orderingPatientIds = latest.orderingPatientIds - patient.patientId,
                            orderedPatientIds = latest.orderedPatientIds + patient.patientId
                        )
                    }
                    _events.emit(PatientListEvent.ShowToast("Order created for ${patient.name}"))
                },
                onFailure = { error ->
                    val latest = _uiState.value
                    if (latest is PatientListUiState.Success) {
                        _uiState.value = latest.copy(
                            orderingPatientIds = latest.orderingPatientIds - patient.patientId
                        )
                    }
                    _events.emit(PatientListEvent.ShowToast(error.message ?: "Order failed"))
                }
            )
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

        data class Success(
            val patients: List<Patient>,
            val orderedPatientIds: Set<Long> = emptySet(),
            val orderingPatientIds: Set<Long> = emptySet()
        ) : PatientListUiState()

        data class Error(val message: String) : PatientListUiState()
    }
}
