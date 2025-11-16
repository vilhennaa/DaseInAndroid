package com.cotovicz.daseinandroid.data.local.dao

import androidx.room.*
import com.cotovicz.daseinandroid.data.local.entity.Creation
import kotlinx.coroutines.flow.Flow

@Dao
interface CreationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreation(creation: Creation)

    @Query("SELECT * FROM creations ORDER BY id DESC")
    fun getAllCreations(): Flow<List<Creation>>

    @Query("SELECT * FROM creations WHERE id = :id")
    fun getCreationById(id: Int): Flow<Creation?>

    @Update
    suspend fun updateCreation(creation: Creation)

    @Delete
    suspend fun deleteCreation(creation: Creation)

    @Query("UPDATE creations SET commentCount = commentCount + 1 WHERE id = :creationId")
    suspend fun incrementCommentCount(creationId: Int)
}
