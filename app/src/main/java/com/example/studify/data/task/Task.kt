package com.example.studify.data.task

/**
 * Domain model used on the UI side.
 *
 * New fields:
 *  - totalFocusMinutes: accumulated focus time for this task
 *  - lastFocusMinutes : minutes in the latest focus session
 *  - focusSessions    : how many focus sessions have been completed
 */
data class Task(
    val id: Long,
    val userId: String,
    val title: String,
    val minutes: Int,
    val coins: Int,
    val done: Boolean,

    // --- new statistics fields ---
    val totalFocusMinutes: Int = 0,
    val lastFocusMinutes: Int = 0,
    val focusSessions: Int = 0
)
