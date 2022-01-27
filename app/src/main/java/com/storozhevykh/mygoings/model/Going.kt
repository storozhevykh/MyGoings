package com.storozhevykh.mygoings.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Default parameters are needed to obtain an empty constructor for Firebase
@Entity
data class Going (val title: String = "", val text: String = "", val category: String = "", val priority: Int = 0,
                  @PrimaryKey val timeCreated: Long = 0, val timeElapsed: Long = 0, var state: Int = 0) {


}