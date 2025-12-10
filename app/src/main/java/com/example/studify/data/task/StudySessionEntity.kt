package com.example.studify.data.task

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val taskId: Long,
    val timestamp: Long,
    val minutes: Int,
    val notes: String,
    val photoPath: String?
)
