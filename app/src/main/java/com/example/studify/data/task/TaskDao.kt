// app/src/main/java/com/example/studify/data/task/TaskDao.kt
package com.example.studify.data.task

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {


    @Query("SELECT * FROM tasks WHERE userId = :currentUid ORDER BY id DESC")
    fun getAllForUser(currentUid: String): Flow<List<TaskEntity>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long


    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)


    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)


    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TaskEntity?
}
