package com.example.matarpontun.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.matarpontun.data.local.entity.DailyOrderEntity

@Dao
interface DailyOrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: DailyOrderEntity)

    @Query("SELECT * FROM daily_orders WHERE patientId = :patientId AND orderDate = :date LIMIT 1")
    suspend fun getOrderForPatient(patientId: Long, date: String): DailyOrderEntity?

    @Query("SELECT * FROM daily_orders WHERE wardId = :wardId AND orderDate = :date")
    suspend fun getOrdersForWard(wardId: Long, date: String): List<DailyOrderEntity>

    @Query("DELETE FROM daily_orders WHERE patientId = :patientId AND orderDate = :date")
    suspend fun deleteOrderForPatient(patientId: Long, date: String)
}
