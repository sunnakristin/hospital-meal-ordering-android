package com.example.matarpontun.ui.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.matarpontun.domain.service.DailyOrderService
import com.example.matarpontun.domain.service.PatientService

/**
 * Factory required because [PatientListViewModel] takes constructor arguments.
 * Android's default ViewModelProvider cannot inject dependencies, so this factory
 * tells the framework how to construct the ViewModel with its services.
 */
class PatientListViewModelFactory(
    private val patientService: PatientService,
    private val dailyOrderService: DailyOrderService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PatientListViewModel(patientService, dailyOrderService) as T
    }
}
