package com.storozhevykh.mygoings.database

import androidx.room.*
import com.storozhevykh.mygoings.model.Going

@Dao
interface GoingDao {

    @Query("SELECT * FROM Going")
    suspend fun getAll(): List<Going>

    @Query("SELECT * FROM Going WHERE timeElapsed <= :timeLimit")
    suspend fun getTillTime(timeLimit: Long): List<Going>

    @Insert
    suspend fun insert(going: Going)

    @Update
    suspend fun update(going: Going)

    @Delete
    suspend fun delete(going: Going)
}