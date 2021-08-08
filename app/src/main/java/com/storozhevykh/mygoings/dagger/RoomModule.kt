package com.storozhevykh.mygoings.dagger

import androidx.room.Room
import com.storozhevykh.mygoings.App
import com.storozhevykh.mygoings.database.DataBase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RoomModule {

    @Provides
    @Singleton
    fun provideDatabase(): DataBase {
        return Room.databaseBuilder(App.application, DataBase::class.java, "database").build()
    }

}