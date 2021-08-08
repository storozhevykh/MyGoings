package com.storozhevykh.mygoings.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.storozhevykh.mygoings.model.Going

@Database(entities = arrayOf(Going::class), version = 1)
abstract class DataBase: RoomDatabase() {

    abstract fun goingDao(): GoingDao
}