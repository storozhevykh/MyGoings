package com.storozhevykh.mygoings.database

import androidx.room.*
import com.storozhevykh.mygoings.model.Going

@Dao
interface GoingDao {

    @Query("SELECT * FROM Going")
    suspend fun getAll(): List<Going>

    @Query("SELECT * FROM Going WHERE timeElapsed <= :timeLimit AND timeElapsed > 0")
    suspend fun getTillTime(timeLimit: Long): List<Going>

    @Query("SELECT * FROM Going WHERE state = 1")
    suspend fun getDone(): List<Going>

    @Query("SELECT * FROM Going WHERE state = 2")
    suspend fun getSkipped(): List<Going>

    @Query("SELECT * FROM Going WHERE state = 2 OR state = 1 OR timeElapsed <= :timeLimit")
    suspend fun getPast(timeLimit: Long): List<Going>

    @Query("SELECT * FROM Going WHERE priority = :priority")
    suspend fun getWithPriority(priority: Int): List<Going>

    @Query("SELECT * FROM Going WHERE priority >= :priority AND state = 0")
    suspend fun getActiveWithPriority(priority: Int): List<Going>

    @Query("SELECT * FROM Going WHERE timeCreated = :timeCreated")
    suspend fun getById(timeCreated: Long): Going

    @Query("SELECT count(*)!=0 FROM Going WHERE timeCreated = :timeCreated")
    suspend fun  containsPrimaryKey(timeCreated: Long): Boolean

    @Query("DELETE FROM Going")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(going: Going)

    @Update
    suspend fun update(going: Going)

    @Delete
    suspend fun delete(going: Going)
}