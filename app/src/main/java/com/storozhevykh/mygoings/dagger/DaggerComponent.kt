package com.storozhevykh.mygoings.dagger

import com.storozhevykh.mygoings.adapters.GoingsListAdapter
import com.storozhevykh.mygoings.firebase.SyncHelper
import com.storozhevykh.mygoings.model.FilterGoingsList
import com.storozhevykh.mygoings.services.NotificationService
import com.storozhevykh.mygoings.view.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(RoomModule::class, FilterModule::class, SyncModule::class))
interface DaggerComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(addingGoingDialog: AddingGoingDialog)
    fun inject(pageFragment: PageFragment)
    fun inject(settingsFragment: SettingsFragment)
    fun inject(filterGoingsList: FilterGoingsList)
    fun inject(goingsListAdapter: GoingsListAdapter)
    fun inject(notificationService: NotificationService)
    fun inject(syncHelper: SyncHelper)
    fun inject(syncDialog: SyncDialog)
    fun inject(loginDialog: LoginDialog)
}