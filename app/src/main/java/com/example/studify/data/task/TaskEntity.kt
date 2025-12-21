package com.example.studify.data.task

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for tasks table.
 *
 * IMPORTANT:
 *  - New columns were added for focus statistics.
 *  - Easiest way during development is uninstalling the app
 *    so Room can recreate the database with the new schema.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId:String,
    val title: String,
    val minutes: Int,
    val coins: Int,
    val done: Boolean,

    // --- new statistics columns ---
    val totalFocusMinutes: Int = 0,
    val lastFocusMinutes: Int = 0,
    val focusSessions: Int = 0
)
