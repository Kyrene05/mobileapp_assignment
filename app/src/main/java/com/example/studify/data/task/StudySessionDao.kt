package com.example.studify.data.task

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    @Insert
    suspend fun insert(entity: StudySessionEntity)

    @Update
    suspend fun update(entity: StudySessionEntity)

    @Query(
        "SELECT * FROM study_sessions " +
                "WHERE taskId = :taskId " +
                "ORDER BY timestamp DESC"
    )
    fun sessionsForTask(taskId: Long): Flow<List<StudySessionEntity>>


    @Query(
        "SELECT * FROM study_sessions " +
                "WHERE taskId = :taskId " +
                "ORDER BY timestamp DESC " +
                "LIMIT 1"
    )
    suspend fun getLatestForTask(taskId: Long): StudySessionEntity?
}
