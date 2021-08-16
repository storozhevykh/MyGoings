package com.storozhevykh.mygoings.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Going (val title: String, val text: String, val category: String, val priority: Int,
                  @PrimaryKey val timeCreated: Long, val timeElapsed: Long, val done: Boolean) {


}