package com.example.matarpontun.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.matarpontun.data.local.entity.DailyOrderEntity

/**
 * Room DAO for [DailyOrderEntity].
 * REPLACE conflict strategy means re-ordering overwrites the existing cached row.
 */
@Dao
interface DailyOrderDao {

    /** Inserts or replaces a daily order in the cache. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: DailyOrderEntity)

    /** Returns the cached order for [patientId] on [date] (ISO string), or null if absent. */
    @Query("SELECT * FROM daily_orders WHERE patientId = :patientId AND orderDate = :date LIMIT 1")
    suspend fun getOrderForPatient(patientId: Long, date: String): DailyOrderEntity?

    /** Returns all cached orders for [wardId] on [date] (ISO string). */
    @Query("SELECT * FROM daily_orders WHERE wardId = :wardId AND orderDate = :date")
    suspend fun getOrdersForWard(wardId: Long, date: String): List<DailyOrderEntity>

    /** Deletes the cached order for [patientId] on [date] (ISO string). */
    @Query("DELETE FROM daily_orders WHERE patientId = :patientId AND orderDate = :date")
    suspend fun deleteOrderForPatient(patientId: Long, date: String)
}
