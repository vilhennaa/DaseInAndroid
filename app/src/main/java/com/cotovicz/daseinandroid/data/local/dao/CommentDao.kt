package com.cotovicz.daseinandroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cotovicz.daseinandroid.data.local.entity.Comment
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    @Query("SELECT * FROM comments WHERE creationId = :creationId ORDER BY id ASC")
    fun getCommentsForCreation(creationId: Int): Flow<List<Comment>>
}