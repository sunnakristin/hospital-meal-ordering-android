package com.example.matarpontun.ui.patients

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colour palette for the box view cards and dialog
private val BoxDialogBg = Color(0xFF7C6FCD)
private val BoxCapacityBg = Color(0xFF37474F)
private val BoxCardTop = Color(0xFF607D8B)
private val BoxCardBottom = Color(0xFF546E7A)
private val BoxAccentBorder = Color(0xFF90A4AE)
private val BoxOccupiedBedBorder = Color(0x661565C0)
private val BoxEmptyBedColor = Color(0x9978909C)

/**
 * Alternative visual layout for the patient list (added feature, not in original user stories).
 * Groups patients by room and renders each room as a card containing a bed grid.
 * Each occupied bed shows the patient's initials and is tappable to reveal a details dialog.
 * Empty beds within a room's capacity are shown as greyed-out circles.
 *
 * Receives the same [items] list as the RecyclerView adapter so both views share one data source.
 */
@Composable
fun PatientBoxView(items: List<PatientListViewModel.PatientListItem>) {
    data class RoomEntry(val name: String, val patients: List<PatientListViewModel.PatientRowUi>)

    val rooms = mutableListOf<RoomEntry>()
    var currentName: String? = null
    val currentPatients = mutableListOf<PatientListViewModel.PatientRowUi>()

    for (item in items) {
        when (item) {
            is PatientListViewModel.PatientListItem.RoomHeader -> {
                if (currentName != null) rooms.add(RoomEntry(currentName, currentPatients.toList()))
                currentName = item.roomName
                currentPatients.clear()
            }
            is PatientListViewModel.PatientListItem.PatientRow -> currentPatients.add(item.data)
        }
    }
    if (currentName != null) rooms.add(RoomEntry(currentName, currentPatients.toList()))

    var selectedPatient by remember { mutableStateOf<PatientListViewModel.PatientRowUi?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rooms.forEach { room ->
            BoxRoomCard(room.name, room.patients, onPatientClick = { selectedPatient = it })
        }
    }

    selectedPatient?.let { patient ->
        AlertDialog(
            onDismissRequest = { selectedPatient = null },
            containerColor = BoxDialogBg,
            title = {
                Text(patient.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Bed: ${patient.bedNumber}", color = Color.White)
                    Text("Food type: ${patient.foodTypeName}", color = Color.White)
                    Text("Status: ${patient.statusText}", color = Color.White)
                    if (patient.restrictions.isNotEmpty()) {
                        Text(
                            "Restrictions: ${patient.restrictions.joinToString(", ")}",
                            color = Color.White
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedPatient = null }) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }
}

/**
 * Card representing a single room in the box view.
 * Calculates bed capacity from the highest bed number among the patients,
 * then lays beds out in a 2-column grid — occupied beds show initials, empty ones are grey.
 */
@Composable
private fun BoxRoomCard(
    roomName: String,
    patients: List<PatientListViewModel.PatientRowUi>,
    onPatientClick: (PatientListViewModel.PatientRowUi) -> Unit
) {
    val capacity = patients.maxOfOrNull { it.bedNumber } ?: patients.size
    val byBed = patients.associateBy { it.bedNumber }
    val beds = (1..capacity).map { byBed[it] }
    val occupiedCount = patients.size
    val cardShape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = cardShape, ambientColor = Color.Black.copy(alpha = 0.15f), spotColor = Color.Black.copy(alpha = 0.15f))
            .clip(cardShape)
    ) {
        // Left accent border
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(BoxAccentBorder)
        )

        // Card body with gradient
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(BoxCardTop, BoxCardBottom)))
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = roomName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .background(BoxCapacityBg, CircleShape)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$capacity beds",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }

            // Bed grid
            Column(
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                beds.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { patient ->
                            val initials = patient?.name
                                ?.split(" ")
                                ?.mapNotNull { it.firstOrNull()?.toString() }
                                ?.take(2)
                                ?.joinToString("") ?: ""

                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(if (patient != null) Color.White else BoxEmptyBedColor)
                                        .then(
                                            if (patient != null)
                                                Modifier
                                                    .border(2.dp, BoxOccupiedBedBorder, CircleShape)
                                                    .clickable { onPatientClick(patient) }
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (patient != null) {
                                        Text(
                                            text = initials,
                                            color = Color(0xFF37474F),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
