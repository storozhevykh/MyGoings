package com.storozhevykh.mygoings.dagger

import com.storozhevykh.mygoings.database.DataBase
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(RoomModule::class))
interface DaggerComponent {
    fun getRoomDatabase(): DataBase
}