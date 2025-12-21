package com.example.studify.presentation.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.example.studify.data.AppDatabase
import com.example.studify.data.task.StudySessionEntity
import com.example.studify.data.task.Task
import com.example.studify.data.task.TaskEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    private val taskDao = db.taskDao()
    private val sessionDao = db.studySessionDao()
    private val currentUid: String
        get() = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // ---------- Task list (Room -> Domain) ----------

    val tasks: StateFlow<List<Task>> =
        taskDao.getAllForUser(currentUid)                       // Flow<List<TaskEntity>>
            .map { entities -> entities.map { it.toDomain() } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // ---------- Latest StudySession for the selected task ----------

    private val _currentSession = MutableStateFlow<StudySessionEntity?>(null)
    val currentSession: StateFlow<StudySessionEntity?> = _currentSession

    /** Called when entering TaskRecordScreen, to load the latest session for this task (may be null). */
    fun loadLatestSession(taskId: Long) {
        viewModelScope.launch {
            _currentSession.value = sessionDao.getLatestForTask(taskId)
        }
    }

    // ---------- Task CRUD ----------

    fun createTask(title: String, minutes: Int, coins: Int) {
        viewModelScope.launch {
            taskDao.insert(
                TaskEntity(
                    id = 0,
                    userId = currentUid,
                    title = title,
                    minutes = minutes,
                    coins = coins,
                    done = false
                )
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.update(task.toEntity())
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteById(task.id)
        }
    }

    fun toggleDone(task: Task) {
        updateTask(task.copy(done = !task.done))
    }

    // ---------- StudySession save / update ----------

    /**
     * Called when the user presses Save on the TaskRecordScreen.
     * - If no previous record exists → Insert
     * - If a latest record already exists → Update
     */
    fun saveSession(task: Task, notes: String, photoPath: String?) {
        viewModelScope.launch {
            val existing = _currentSession.value
            val now = System.currentTimeMillis()
            val minutes = task.minutes

            if (existing == null) {
                val newEntity = StudySessionEntity(
                    id = 0,
                    taskId = task.id,
                    timestamp = now,
                    minutes = minutes,
                    notes = notes,
                    photoPath = photoPath
                )
                sessionDao.insert(newEntity)
                _currentSession.value = sessionDao.getLatestForTask(task.id)
            } else {
                val updated = existing.copy(
                    timestamp = now,
                    minutes = minutes,
                    notes = notes,
                    photoPath = photoPath
                )
                sessionDao.update(updated)
                _currentSession.value = updated
            }
        }
    }

    // ---------- Apply focus result from TaskFocusScreen ----------

    /**
     * Called when a focus session is completed (either full or early finish).
     *
     * @param task          The original task.
     * @param actualMinutes The real focus minutes in this session.
     *
     * We add the minutes to:
     *  - totalFocusMinutes
     *  - lastFocusMinutes
     *  - focusSessions (count++)
     */
    fun applyFocusResult(task: Task, actualMinutes: Int) {
        val safeMinutes = actualMinutes.coerceAtLeast(0)

        val updated = task.copy(
            totalFocusMinutes = task.totalFocusMinutes + safeMinutes,
            lastFocusMinutes = safeMinutes,
            focusSessions = task.focusSessions + 1
        )

        updateTask(updated)
    }

    // ---------- ViewModel Factory ----------

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val app =
                    extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                return TaskViewModel(app) as T
            }
        }
    }
}

/* ---------- Entity ↔ Domain mappers ---------- */

private fun TaskEntity.toDomain(): Task =
    Task(
        id = id,
        userId=userId,
        title = title,
        minutes = minutes,
        coins = coins,
        done = done,
        totalFocusMinutes = totalFocusMinutes,
        lastFocusMinutes = lastFocusMinutes,
        focusSessions = focusSessions
    )

private fun Task.toEntity(): TaskEntity =
    TaskEntity(
        id = id,
        userId=userId,
        title = title,
        minutes = minutes,
        coins = coins,
        done = done,
        totalFocusMinutes = totalFocusMinutes,
        lastFocusMinutes = lastFocusMinutes,
        focusSessions = focusSessions
    )
