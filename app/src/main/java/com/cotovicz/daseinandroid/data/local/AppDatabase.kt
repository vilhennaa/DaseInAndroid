package com.cotovicz.daseinandroid.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cotovicz.daseinandroid.data.local.dao.CommentDao
import com.cotovicz.daseinandroid.data.local.dao.CreationDao
import com.cotovicz.daseinandroid.data.local.entity.Comment
import com.cotovicz.daseinandroid.data.local.entity.Creation

@Database(entities = [Creation::class, Comment::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun creationDao(): CreationDao
    abstract fun commentDao(): CommentDao
}