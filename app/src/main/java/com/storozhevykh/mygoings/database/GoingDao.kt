package com.storozhevykh.mygoings.database

import androidx.room.*
import com.storozhevykh.mygoings.model.Going

@Dao
interface GoingDao {

    @Query("SELECT * FROM Going")
    fun getAll(): List<Going>

    @Query("SELECT * FROM Going WHERE timeElapsed <= :timeLimit")
    fun getTillTime(timeLimit: Long): List<Going>

    @Insert
    fun insert(going: Going)

    @Update
    fun update(going: Going)

    @Delete
    fun delete(going: Going)
}