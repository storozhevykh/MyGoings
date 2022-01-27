package com.storozhevykh.mygoings.dagger

import com.storozhevykh.mygoings.model.FilterGoingsList
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FilterModule {

    @Provides
    @Singleton
    fun provideFilter(): FilterGoingsList {
        return FilterGoingsList()
    }
}