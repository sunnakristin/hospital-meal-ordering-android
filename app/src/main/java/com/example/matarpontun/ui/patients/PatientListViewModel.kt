package com.example.matarpontun.ui.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.Patient
import com.example.matarpontun.AppContainer
import com.example.matarpontun.data.remote.dto.FoodTypeDto
import com.example.matarpontun.data.remote.dto.PatientConflictDto
import com.example.matarpontun.data.remote.dto.PatientSummaryDto
import com.example.matarpontun.domain.model.ConflictInfo
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

    /** Restricts the visible rooms to [filter]. Also auto-expands the filtered room. */
    fun setRoomFilter(filter: String?) {
        roomFilter = filter
        if (filter != null) expandedRooms = expandedRooms + filter
    }

    /** Loads patients only if the list is currently empty (avoids redundant network calls). */
    fun loadPatientsIfNeeded(wardId: Long) {
        if (patients.isNotEmpty()) return
        loadPatients(wardId)
    }

    /** Fetches the full patient list for [wardId] from the repository and rebuilds the UI state. */
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

    /** Places a meal order for the patient with [patientId] using their assigned food type. */
    fun orderForPatient(patientId: Long) {

        viewModelScope.launch {

            val patient = patients.find { it.patientId == patientId } ?: return@launch

            val result = dailyOrderService.orderForPatient(
                patient.patientId,
                patient.foodType
            )

            result.onSuccess { order ->

                val hasUnresolvableConflicts = order.conflicts.any { it.replacementMeal == null }

                if (hasUnresolvableConflicts) {
                    // Order saved on backend but needs manual attention — reflect status locally
                    // without marking it as "ordered" so the UI shows "Manual review required"
                    patients = patients.map { p ->
                        if (p.patientId == patientId) p.copy(status = "NEEDS MANUAL CHANGE") else p
                    }
                } else {
                    todaysOrders = todaysOrders + (patientId to order)
                    if (order.conflicts.isNotEmpty()) {
                        // All conflicts were auto-resolved — update status so UI shows correct text
                        patients = patients.map { p ->
                            if (p.patientId == patientId) p.copy(status = "AUTO CHANGED") else p
                        }
                    }
                }
                rebuildUi()

                if (order.conflicts.isNotEmpty()) {
                    _events.emit(PatientListEvent.ShowConflictDialog(patient.name, order.conflicts))
                } else {
                    _events.emit(PatientListEvent.ShowToast("Order placed for ${patient.name}"))
                }

            }.onFailure {

                _events.emit(
                    PatientListEvent.ShowToast(it.message ?: "Order failed")
                )
            }
        }
    }

    /** Places meal orders for every patient in the ward in one batch request. */
    fun orderForWard(wardId: Long) {

        viewModelScope.launch {

            val result = dailyOrderService.orderForWard(wardId)

            result.onSuccess { response ->

                loadPatients(wardId)

                val patientConflicts = response.conflicts.orEmpty()
                if (patientConflicts.isNotEmpty()) {
                    _events.emit(PatientListEvent.ShowWardConflictDialog(patientConflicts))
                } else {
                    _events.emit(PatientListEvent.ShowToast("Orders created for ward"))
                }

            }.onFailure {

                _events.emit(
                    PatientListEvent.ShowToast(it.message ?: "Ward order failed")
                )
            }
        }
    }

    /** Toggles the expanded/collapsed detail view for a patient row. */
    fun toggleExpand(patientId: Long) {
        expandedIds =
            if (patientId in expandedIds) expandedIds - patientId
            else expandedIds + patientId

        rebuildUi()
    }

    /** Toggles a room's expand/collapse state in the list view. */
    fun toggleRoom(roomName: String) {
        expandedRooms =
            if (roomName in expandedRooms) expandedRooms - roomName
            else expandedRooms + roomName

        rebuildUi()
    }
    /** Fetches available food types from the API if not already loaded. Used to populate edit dialogs. */
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

    /** Returns the display names of all loaded food types for spinner population. */
    fun getFoodTypeNames(): List<String> = foodTypes.map { it.typeName }

    /**
     * Cancels today's meal order for the given patient (US6).
     * Also removes the local cached order so the UI reflects the change immediately.
     */
    fun cancelTodaysOrder(patientId: Long) {
        viewModelScope.launch {
            try {
                AppContainer.api.cancelTodaysOrder(patientId)
                todaysOrders = todaysOrders - patientId
                // Clear backend status so rebuildUi() treats the patient as unordered
                patients = patients.map { p ->
                    if (p.patientId == patientId) p.copy(status = "") else p
                }
                rebuildUi()
                _events.emit(PatientListEvent.ShowToast("Meal cancelled"))
            } catch (e: Exception) {
                _events.emit(PatientListEvent.ShowToast(e.message ?: "Failed to cancel meal"))
            }
        }
    }

    /** PATCHes a patient's name, food type, and restrictions, then reloads the list. */
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

    /** Creates a room with auto-generated (random) patients, then reloads the list. */
    fun createRoom(wardId: Long, roomNumber: String, maxPatients: Int) {
        viewModelScope.launch {
            try {
                AppContainer.api.createRoom(wardId, RoomCreateRequest(roomNumber, maxPatients))
                loadPatients(wardId)
                _events.emit(PatientListEvent.ShowToast("Room $roomNumber created"))
            } catch (e: Exception) {
                _events.emit(PatientListEvent.ShowToast(e.message ?: "Failed to create room"))
            }
        }
    }

    /**
     * Creates a room then emits [PatientListEvent.ShowPatientFillDialogs] with the generated
     * patient stubs so the UI can prompt the user to fill in each patient's details.
     */
    fun createRoomManual(wardId: Long, roomNumber: String, maxPatients: Int) {
        viewModelScope.launch {
            try {
                val response = AppContainer.api.createRoom(wardId, RoomCreateRequest(roomNumber, maxPatients))
                _events.emit(PatientListEvent.ShowPatientFillDialogs(response.patients))
            } catch (e: Exception) {
                _events.emit(PatientListEvent.ShowToast(e.message ?: "Failed to create room"))
            }
        }
    }

    /**
     * PATCHes a patient without triggering a list reload. Calls [onComplete] with
     * `true` on success or `false` on failure. Used during the manual room-fill dialog chain.
     */
    fun patchPatientQuiet(patientId: Long, name: String, foodTypeName: String, restrictions: List<String>, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                AppContainer.api.updatePatient(patientId, PatientUpdateRequest(name, foodTypeName, restrictions))
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    private fun rebuildUi() {
        val items = mutableListOf<PatientListItem>()
        val boxItems = mutableListOf<PatientListItem>()

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
                // NEEDS MANUAL CHANGE intentionally excluded — room shows ✗ until resolved
            }
            val qrCode = roomPatients.firstOrNull()?.roomQrCode
            items.add(PatientListItem.RoomHeader(roomName, isExpanded, allOrdered, qrCode))
            boxItems.add(PatientListItem.RoomHeader(roomName, isExpanded, allOrdered, qrCode))

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
                val needsManualChange = patient.status == "NEEDS MANUAL CHANGE"
                val autoChanged = patient.status == "AUTO CHANGED"
                val row = PatientListItem.PatientRow(PatientRowUi(
                    patientId = patient.patientId,
                    name = patient.name,
                    bedNumber = patient.bedNumber,
                    room = patient.room,
                    foodTypeName = patient.foodType.typeName,
                    hasOrder = hasOrder,
                    hasConflict = needsManualChange,
                    isAutoChanged = autoChanged,
                    statusText = statusText,
                    primaryButtonText = when {
                        needsManualChange -> "CONFLICT"
                        hasOrder -> "ORDERED"
                        else -> "ORDER"
                    },
                    primaryButtonEnabled = !hasOrder,
                    expanded = patient.patientId in expandedIds,
                    restrictions = patient.restrictions,
                    breakfast = localOrder?.breakfast?.name ?: patient.breakfast,
                    lunch = localOrder?.lunch?.name ?: patient.lunch,
                    afternoonSnack = localOrder?.afternoonSnack?.name ?: patient.afternoonSnack,
                    dinner = localOrder?.dinner?.name ?: patient.dinner,
                    nightSnack = localOrder?.nightSnack?.name ?: patient.nightSnack
                ))
                boxItems.add(row)
                if (isExpanded) items.add(row)
            }
        }

        val canOrderWard = boxItems.filterIsInstance<PatientListItem.PatientRow>().any { !it.data.hasOrder }
        _uiState.value = PatientListUiState.Success(items, boxItems, canOrderWard, AppContainer.isOffline)
    }

    sealed class PatientListEvent {
        data class ShowToast(val message: String) : PatientListEvent()
        data class ShowPatientFillDialogs(val patients: List<PatientSummaryDto>) : PatientListEvent()
        /** Emitted after ordering for a single patient when restriction conflicts were found. */
        data class ShowConflictDialog(
            val patientName: String,
            val conflicts: List<ConflictInfo>
        ) : PatientListEvent()
        /** Emitted after ordering for an entire ward when at least one patient had conflicts. */
        data class ShowWardConflictDialog(
            val patientConflicts: List<PatientConflictDto>
        ) : PatientListEvent()
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
            val boxItems: List<PatientListItem>,
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
        val hasConflict: Boolean,
        val isAutoChanged: Boolean,
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