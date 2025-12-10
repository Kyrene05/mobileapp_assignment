// app/src/main/java/com/example/studify/data/task/TaskMappers.kt
package com.example.studify.data.task

fun TaskEntity.toDomain(): Task =
    Task(
        id = id,
        title = title,
        minutes = minutes,
        coins = coins,
        done = done
    )

fun Task.toEntity(): TaskEntity =
    TaskEntity(
        id = if (id == 0L) 0L else id,   // 0 让 Room 自增
        title = title,
        minutes = minutes,
        coins = coins,
        done = done
    )
