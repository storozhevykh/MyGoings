package com.storozhevykh.mygoings.dagger

import com.storozhevykh.mygoings.firebase.SyncHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SyncModule {

    @Provides
    @Singleton
    fun provideSyncHelper(): SyncHelper {
        return SyncHelper()
    }

}