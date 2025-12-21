package com.example.studify.data.task

fun TaskEntity.toDomain(): Task =
    Task(
        id = id,
        userId = userId, // Map the owner's ID to the domain model
        title = title,
        minutes = minutes,
        coins = coins,
        done = done,
        totalFocusMinutes = totalFocusMinutes,
        lastFocusMinutes = lastFocusMinutes,
        focusSessions = focusSessions
    )

fun Task.toEntity(): TaskEntity =
    TaskEntity(
        id = if (id == 0L) 0L else id,
        userId = userId, // Ensure the UID is saved back to the database
        title = title,
        minutes = minutes,
        coins = coins,
        done = done,
        totalFocusMinutes = totalFocusMinutes,
        lastFocusMinutes = lastFocusMinutes,
        focusSessions = focusSessions
    )