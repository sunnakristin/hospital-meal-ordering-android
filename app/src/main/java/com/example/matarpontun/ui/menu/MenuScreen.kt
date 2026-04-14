package com.example.matarpontun.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matarpontun.data.remote.dto.FoodTypeDto
import com.example.matarpontun.data.remote.dto.MealSlotDto
import com.example.matarpontun.data.remote.dto.MenuDetailDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Top-level menu screen shown by MenuActivity. */
@Composable
fun MenuScreen(
    foodTypes: List<FoodTypeDto>,
    menuState: MenuUiState,
    onFoodTypeSelected: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        Text(
            text = "Today's Menu  $today",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Food type pill selector
        if (foodTypes.isEmpty()) {
            Text("Loading food types…", color = Color.Gray)
        } else {
            FoodTypeSelector(foodTypes = foodTypes, onSelected = onFoodTypeSelected)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menu content area
        when (menuState) {
            is MenuUiState.Idle -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Select a food type above to view today's menu.", color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
            is MenuUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is MenuUiState.Success -> {
                MenuDetailView(menu = menuState.menu)
            }
            is MenuUiState.NoMenu -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No menu assigned for today.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is MenuUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${menuState.message}", color = Color.Red, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

/** Horizontally scrollable row of pill buttons — one per food type. */
@Composable
private fun FoodTypeSelector(
    foodTypes: List<FoodTypeDto>,
    onSelected: (Long) -> Unit
) {
    var selectedId by remember { mutableStateOf<Long?>(null) }
    val scrollState = rememberScrollState()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            foodTypes.forEach { ft ->
                val isSelected = ft.id == selectedId
                Button(
                    onClick = {
                        selectedId = ft.id
                        onSelected(ft.id)
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF1565C0) else Color(0xFFBBDEFB),
                        contentColor = if (isSelected) Color.White else Color(0xFF1565C0)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = ft.typeName,
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // Scroll indicator track + thumb
        if (foodTypes.size > 3) {
            Spacer(modifier = Modifier.height(6.dp))
            ScrollIndicator(scrollState = scrollState)
        }
    }
}

/** Thin progress-bar style scroll indicator showing position within the chip row. */
@Composable
private fun ScrollIndicator(scrollState: androidx.compose.foundation.ScrollState) {
    val fraction = if (scrollState.maxValue > 0) {
        scrollState.value.toFloat() / scrollState.maxValue.toFloat()
    } else 0f

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(Color(0xFFDDDDDD), RoundedCornerShape(50))
    ) {
        val trackWidth = maxWidth
        // Thumb is 30% of the track width
        val thumbWidth = trackWidth * 0.30f
        val thumbOffset = (trackWidth - thumbWidth) * fraction

        Box(
            modifier = Modifier
                .width(thumbWidth)
                .height(3.dp)
                .offset(x = thumbOffset)
                .background(Color(0xFF1565C0), RoundedCornerShape(50))
        )
    }
}

/** Scrollable list of meal slots for a given menu. */
@Composable
private fun MenuDetailView(menu: MenuDetailDto) {
    val slots = listOf(
        "Breakfast" to menu.breakfast,
        "Lunch" to menu.lunch,
        "Afternoon Snack" to menu.afternoonSnack,
        "Dinner" to menu.dinner,
        "Night Snack" to menu.nightSnack
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(
                text = "Menu #${menu.menuId}  ·  ${menu.foodTypeName}",
                fontSize = 13.sp,
                color = Color(0xFF757575),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(slots) { (label, meal) ->
            MealSlotCard(label = label, meal = meal)
        }
    }
}

/** Card for a single meal slot, showing the meal name and its ingredients. */
@Composable
private fun MealSlotCard(label: String, meal: MealSlotDto?) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            if (meal == null) {
                Text("Not assigned", color = Color.Gray, fontSize = 14.sp)
            } else {
                Text(
                    text = meal.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (!meal.ingredients.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ingredients: ${meal.ingredients}",
                        fontSize = 13.sp,
                        color = Color(0xFF555555)
                    )
                }
            }
        }
    }
}
