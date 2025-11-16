package com.cotovicz.daseinandroid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "creations")
data class Creation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val likeCount: Int = 0,
    val commentCount: Int = 0
)
