package com.storozhevykh.mygoings.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.storozhevykh.mygoings.App
import com.storozhevykh.mygoings.adapters.ViewPagerAdapter
import com.storozhevykh.mygoings.database.DataBase
import com.storozhevykh.mygoings.database.GoingDao
import com.storozhevykh.mygoings.model.Going
import com.storozhevykh.mygoings.model.StaticStorage
import com.storozhevykh.mygoings.view.SyncDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList
import javax.inject.Inject

class SyncHelper {

    @Inject
    lateinit var dataBase: DataBase
    var goingDAO: GoingDao

    private var listener: ValueEventListener? = null

    private val goingsListFirebase = ArrayList<Going>()

    init {
        App.component.inject(this)
        goingDAO = dataBase.goingDao()
    }

    suspend fun compareLocalWithCloud(): Int {

        val goingsListLocal = goingDAO.getAll()
        val localIdSet = HashSet<Long>()

        for (going in goingsListLocal)
            localIdSet.add(going.timeCreated)

        val firebase = App.fireDataBase.child(StaticStorage.userEmail!!)
        firebase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {
                    val curGoing = item.getValue(Going::class.java)
                    //println("The going from firebase: ${curGoing?.title}")
                    goingsListFirebase.add(curGoing!!)
                    if (!localIdSet.contains(curGoing.timeCreated)) {
                        goingsAddToLocal.add(curGoing)
                    }
                }

                val firebaseIdSet = HashSet<Long>()
                for (going in goingsListFirebase)
                    firebaseIdSet.add(going.timeCreated)

                for (going in goingsListLocal) {
                    if (!firebaseIdSet.contains(going.timeCreated)) {
                        println("The going ${going.title} is absent")
                        goingsAddToCloud.add(going)
                    }
                }

                if (goingsAddToCloud.size > 0)
                    SyncDialog().show(StaticStorage.activityContext.supportFragmentManager, "TAG")
                else CoroutineScope(Dispatchers.IO).launch { synchronize() }

                registerListener()
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error when reading data from firedatabase")
            }

        })

        return goingsAddToCloud.size
    }

    suspend fun synchronize() {
        unregisterListener()
        for (going in goingsAddToCloud) {
            App.fireDataBase.child(StaticStorage.userEmail!!).child((going.timeCreated).toString()).setValue(going)
        }
        for (going in goingsAddToLocal) {
            println("Primary key: ${going.timeCreated}")
            //println("In database: ${goingDAO.getById(going.timeCreated)}")
            goingDAO.insert(going)
        }
        ViewPagerAdapter.updatePages(-1)
        println("Synchronization finished")
        registerListener()
    }

    suspend fun deleteFromLocal() {
        for (going in goingsAddToCloud) {
            goingDAO.delete(going)
        }
        ViewPagerAdapter.updatePages(-1)
    }

    fun registerListener() {
        val firebase = App.fireDataBase.child(StaticStorage.userEmail!!)
        listener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newGoingsList = mutableListOf<Going>()
                CoroutineScope(Dispatchers.IO).launch {
                    for (item in snapshot.children) {
                        val curGoing = item.getValue(Going::class.java)
                        val goingID = curGoing?.timeCreated
                        if (goingDAO.containsPrimaryKey(goingID!!)) {
                            val localGoing = goingDAO.getById(goingID)
                            if (localGoing != curGoing)
                                goingDAO.update(curGoing)
                        }
                        else
                            goingDAO.insert(curGoing)
                    }
                    StaticStorage.goingsList = newGoingsList
                    ViewPagerAdapter.updatePages(-1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        firebase.addValueEventListener(listener!!)
    }

    fun unregisterListener() {
        val firebase: DatabaseReference
        if (StaticStorage.userEmail != null && listener != null) {
            firebase = App.fireDataBase.child(StaticStorage.userEmail!!)
            firebase.removeEventListener(listener!!)
        }
    }

    companion object {
        val goingsAddToCloud = ArrayList<Going>()
        val goingsAddToLocal = ArrayList<Going>()
    }

}