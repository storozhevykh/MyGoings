package com.storozhevykh.mygoings.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Going (val title: String, val text: String, val category: String, val importance: Int,
                  val timeCreated: Long, val timeElapsed: Long, val done: Boolean) {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

}