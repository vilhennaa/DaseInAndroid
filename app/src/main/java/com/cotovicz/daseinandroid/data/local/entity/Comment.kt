package com.cotovicz.daseinandroid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = Creation::class,
            parentColumns = ["id"],
            childColumns = ["creationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Comment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val creationId: Int,
    val parentId: Int?,
    val text: String,
    val author: String = "autor"
)