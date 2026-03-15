package com.example.matarpontun.ui.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.matarpontun.domain.service.DailyOrderService
import com.example.matarpontun.domain.service.PatientService

class PatientListViewModelFactory(
    private val patientService: PatientService,
    private val dailyOrderService: DailyOrderService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PatientListViewModel(patientService, dailyOrderService) as T
    }
}