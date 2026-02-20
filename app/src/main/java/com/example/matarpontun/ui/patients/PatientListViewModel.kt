package com.example.matarpontun.ui.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matarpontun.domain.model.Patient
import com.example.matarpontun.domain.service.PatientService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PatientListViewModel(
    private val patientService: PatientService
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
}

sealed class PatientListUiState {
    object Idle : PatientListUiState()
    object Loading : PatientListUiState()
    data class Success(val patients: List<Patient>) : PatientListUiState()
    data class Error(val message: String) : PatientListUiState()
}
