package com.storozhevykh.mygoings

import android.app.Application
import com.facebook.FacebookSdk
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.storozhevykh.mygoings.dagger.DaggerComponent
import com.storozhevykh.mygoings.dagger.DaggerDaggerComponent
import com.storozhevykh.mygoings.model.StaticStorage


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        application = this
        App.component = buildDagger()
        fireDataBase = Firebase.database.reference
        StaticStorage.appContext = this
        //FacebookSdk.sdkInitialize(this)
    }

    fun buildDagger(): DaggerComponent {
        return DaggerDaggerComponent.builder().build()
    }

    companion object {
        lateinit var application: App
        lateinit var component: DaggerComponent
        lateinit var fireDataBase: DatabaseReference
    }
}