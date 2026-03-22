package com.example.matarpontun.ui.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.Patient
import com.example.matarpontun.AppContainer
import com.example.matarpontun.data.remote.dto.FoodTypeDto
import com.example.matarpontun.data.remote.dto.PatientUpdateRequest
import com.example.matarpontun.data.remote.dto.RoomCreateRequest
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
    private var expandedRooms: Set<String> = emptySet()
    private var patients: List<Patient> = emptyList()
    private var todaysOrders: Map<Long, DailyOrder> = emptyMap()
    private var foodTypes: List<FoodTypeDto> = emptyList()
    private var roomFilter: String? = null

    fun setRoomFilter(filter: String?) {
        roomFilter = filter
        if (filter != null) expandedRooms = expandedRooms + filter
    }

    fun loadPatientsIfNeeded(wardId: Long) {
        if (patients.isNotEmpty()) return
        loadPatients(wardId)
    }

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

    fun toggleRoom(roomName: String) {
        expandedRooms =
            if (roomName in expandedRooms) expandedRooms - roomName
            else expandedRooms + roomName

        rebuildUi()
    }
    fun loadFoodTypesIfNeeded() {
        if (foodTypes.isNotEmpty()) return
        viewModelScope.launch {
            try {
                foodTypes = AppContainer.api.getFoodTypes()
            } catch (e: Exception) {
                _events.emit(PatientListEvent.ShowToast("Failed to load food types: ${e.message}"))
            }
        }
    }

    fun getFoodTypeNames(): List<String> = foodTypes.map { it.typeName }

    fun updatePatient(patientId: Long, name: String, foodTypeName: String, restrictions: List<String>) {
        viewModelScope.launch {
            try {
                AppContainer.api.updatePatient(patientId, PatientUpdateRequest(name, foodTypeName, restrictions))
                loadPatients(AppContainer.currentWardId)
                _events.emit(PatientListEvent.ShowToast("Patient updated"))
            } catch (e: Exception) {
                _events.emit(PatientListEvent.ShowToast(e.message ?: "Update failed"))
            }
        }
    }

    fun createRoom(wardId: Long, roomNumber: String, numberOfPatients: Int) {
        viewModelScope.launch {
            try {
                AppContainer.api.createRoom(wardId, RoomCreateRequest(roomNumber, numberOfPatients))
                loadPatients(wardId)
                _events.emit(PatientListEvent.ShowToast("Room $roomNumber created"))
            } catch (e: Exception) {
                _events.emit(PatientListEvent.ShowToast(e.message ?: "Failed to create room"))
            }
        }
    }

    private fun rebuildUi() {
        val items = mutableListOf<PatientListItem>()

        val visibleRooms = if (roomFilter != null) {
            patients.groupBy { it.room }.filterKeys { it == roomFilter }
        } else {
            patients.groupBy { it.room }
        }

        visibleRooms.forEach { (roomName, roomPatients) ->
            val isExpanded = roomName in expandedRooms
            val allOrdered = roomPatients.all { patient ->
                val localOrder = todaysOrders[patient.patientId]
                localOrder != null
                    || patient.status == "SUBMITTED"
                    || patient.status == "AUTO CHANGED"
                    || patient.status == "NEEDS MANUAL CHANGE"
            }
            val qrCode = roomPatients.firstOrNull()?.roomQrCode
            items.add(PatientListItem.RoomHeader(roomName, isExpanded, allOrdered, qrCode))
            if (!isExpanded) return@forEach
            roomPatients.sortedBy { it.bedNumber }.forEach { patient ->
                val localOrder = todaysOrders[patient.patientId]
                val backendHasOrder = patient.status == "SUBMITTED"
                        || patient.status == "AUTO CHANGED"
                        || patient.status == "NEEDS MANUAL CHANGE"
                val hasOrder = localOrder != null || backendHasOrder
                val statusText = when {
                    localOrder != null -> "Order placed"
                    patient.status == "SUBMITTED" -> "Order placed"
                    patient.status == "AUTO CHANGED" -> "Order placed (conflict fixed)"
                    patient.status == "NEEDS MANUAL CHANGE" -> "Manual review required ⚠"
                    else -> "Ready to order"
                }
                items.add(PatientListItem.PatientRow(PatientRowUi(
                    patientId = patient.patientId,
                    name = patient.name,
                    bedNumber = patient.bedNumber,
                    room = patient.room,
                    foodTypeName = patient.foodType.typeName,
                    hasOrder = hasOrder,
                    statusText = statusText,
                    primaryButtonText = if (hasOrder) "ORDERED" else "ORDER",
                    primaryButtonEnabled = !hasOrder,
                    expanded = patient.patientId in expandedIds,
                    restrictions = patient.restrictions,
                    breakfast = localOrder?.breakfast?.name ?: patient.breakfast,
                    lunch = localOrder?.lunch?.name ?: patient.lunch,
                    afternoonSnack = localOrder?.afternoonSnack?.name ?: patient.afternoonSnack,
                    dinner = localOrder?.dinner?.name ?: patient.dinner,
                    nightSnack = localOrder?.nightSnack?.name ?: patient.nightSnack
                )))
            }
        }

        val canOrderWard = items.filterIsInstance<PatientListItem.PatientRow>().any { !it.data.hasOrder }
        _uiState.value = PatientListUiState.Success(items, canOrderWard, AppContainer.isOffline)
    }

    sealed class PatientListEvent {
        data class ShowToast(val message: String) : PatientListEvent()
    }

    sealed class PatientListItem {
        data class RoomHeader(val roomName: String, val isExpanded: Boolean, val allOrdered: Boolean, val qrCode: String?) : PatientListItem()
        data class PatientRow(val data: PatientRowUi) : PatientListItem()
    }

    sealed class PatientListUiState {

        object Idle : PatientListUiState()

        object Loading : PatientListUiState()

        data class Success(
            val items: List<PatientListItem>,
            val canOrderWard: Boolean,
            val isOffline: Boolean = false
        ) : PatientListUiState()

        data class Error(
            val message: String
        ) : PatientListUiState()
    }

    data class PatientRowUi(
        val patientId: Long,
        val name: String,
        val bedNumber: Int,
        val room: String,
        val foodTypeName: String,
        val hasOrder: Boolean,
        val statusText: String,
        val primaryButtonText: String,
        val primaryButtonEnabled: Boolean,
        val expanded: Boolean,
        val restrictions: List<String>,
        val breakfast: String?,
        val lunch: String?,
        val afternoonSnack: String?,
        val dinner: String?,
        val nightSnack: String?
    )


}