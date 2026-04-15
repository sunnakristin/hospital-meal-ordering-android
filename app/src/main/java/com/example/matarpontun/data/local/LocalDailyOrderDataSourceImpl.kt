package com.example.matarpontun.data.local

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.matarpontun.data.local.dao.DailyOrderDao
import com.example.matarpontun.data.local.entity.DailyOrderEntity
import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.FoodType
import com.example.matarpontun.domain.model.Meal
import com.example.matarpontun.domain.model.Menu
import com.example.matarpontun.domain.model.Patient
import java.time.LocalDate

/**
 * Room-backed implementation of [LocalDailyOrderDataSource].
 * Converts between [DailyOrder] domain models and [DailyOrderEntity] Room entities.
 */
class LocalDailyOrderDataSourceImpl(
    private val dao: DailyOrderDao
) : LocalDailyOrderDataSource {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun saveOrder(wardId: Long, order: DailyOrder) {
        dao.insert(order.toEntity(wardId))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getOrderForPatient(patientId: Long, date: LocalDate): DailyOrder? =
        dao.getOrderForPatient(patientId, date.toString())?.toDomain()

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getOrdersForWard(wardId: Long, date: LocalDate): List<DailyOrder> =
        dao.getOrdersForWard(wardId, date.toString()).map { it.toDomain() }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun deleteOrderForPatient(patientId: Long, date: LocalDate) {
        dao.deleteOrderForPatient(patientId, date.toString())
    }
}

/** Maps a [DailyOrder] domain model to a flat [DailyOrderEntity] for Room storage. */
@RequiresApi(Build.VERSION_CODES.O)
private fun DailyOrder.toEntity(wardId: Long) = DailyOrderEntity(
    id = 0,
    wardId = wardId,
    patientId = patient.patientId,
    patientName = patient.name,
    orderDate = orderDate.toString(),
    status = status,
    foodTypeName = patient.foodType.typeName,
    breakfastName = breakfast.name,
    lunchName = lunch.name,
    afternoonSnackName = afternoonSnack.name,
    dinnerName = dinner.name,
    nightSnackName = nightSnack.name
)

/** Reconstructs a [DailyOrder] domain model from a cached [DailyOrderEntity]. */
@RequiresApi(Build.VERSION_CODES.O)
private fun DailyOrderEntity.toDomain(): DailyOrder {
    val date = LocalDate.parse(orderDate)
    val foodType = FoodType(foodTypeName)

    fun meal(name: String, category: String) = Meal(
        id = 0L,
        name = name,
        ingredients = "",
        category = category,
        foodType = foodType
    )

    val breakfast = meal(breakfastName, "Breakfast")
    val lunch = meal(lunchName, "Lunch")
    val afternoonSnack = meal(afternoonSnackName, "Afternoon snack")
    val dinner = meal(dinnerName, "Dinner")
    val nightSnack = meal(nightSnackName, "Night snack")

    // Minimal patient stub — cached orders only store the id and name
    val patient = Patient(
        patientId = patientId,
        name = patientName,
        bedNumber = 0,
        room = "",
        roomQrCode = null,
        foodType = foodType,
        restrictions = emptyList(),
        status = status
    )

    val menu = Menu(
        id = 0L,
        date = date,
        foodType = foodType,
        breakfast = breakfast,
        afternoonSnack = afternoonSnack,
        lunch = lunch,
        dinner = dinner,
        nightSnack = nightSnack
    )

    return DailyOrder(
        id = id,
        orderDate = date,
        status = status,
        patient = patient,
        menu = menu,
        breakfast = breakfast,
        lunch = lunch,
        afternoonSnack = afternoonSnack,
        dinner = dinner,
        nightSnack = nightSnack
    )
}
