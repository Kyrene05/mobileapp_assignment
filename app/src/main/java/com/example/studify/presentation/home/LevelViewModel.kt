package com.example.studify.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

//  Firestore data class
data class PlayerProgress(
    val level: Int = 1,
    val xp: Int = 0,
    val nextLevelXp: Int = 10000,
    val coins: Int = 0
)

class LevelViewModel(app: Application) : AndroidViewModel(app) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _progress = MutableStateFlow(PlayerProgress())
    val progress: StateFlow<PlayerProgress> = _progress

    private val userId: String?
        get() = auth.currentUser?.uid

    init {
        // Load from Firebase on start
        viewModelScope.launch { loadFromFirebase() }
    }
    /** Called *after login* to make sure we restore cloud progress */
    fun refreshFromFirebase() {
        viewModelScope.launch {
            loadFromFirebase()
        }
    }

    fun clearData() {
        _progress.value = PlayerProgress()
    }
    // -----------------------------------------------------
    //  1. pomodoro reward: gain XP & Coins
    // -----------------------------------------------------
    fun grantSessionReward(exp: Int, coinsGain: Int = exp) {
        if (exp <= 0 && coinsGain <= 0) return

        viewModelScope.launch {
            // Use a local copy to ensure thread safety during calculation
            val currentProgress = _progress.value
            var newXp = currentProgress.xp + exp
            var newLevel = currentProgress.level
            var newCoins = currentProgress.coins + coinsGain
            var newNext = currentProgress.nextLevelXp

            // Loop handles "multi-leveling" if user gets massive XP at once
            while (newXp >= newNext) {
                newXp -= newNext
                newLevel += 1
                newCoins += levelUpReward(newLevel)
                newNext = calcNextLevelXp(newLevel)
            }

            val updated = currentProgress.copy(
                level = newLevel,
                xp = newXp,
                nextLevelXp = newNext,
                coins = newCoins
            )

            // Update the StateFlow first so UI reacts immediately
            _progress.value = updated

            // Then persist to Firestore
            saveToFirebase(updated)
        }
    }

    // -----------------------------------------------------
    //  2. Shop
    // -----------------------------------------------------
    fun overrideCoins(newCoins: Int) {
        val p = _progress.value

        val updated = p.copy(coins = newCoins)
        _progress.value = updated

        saveToFirebase(updated)
    }

    // -----------------------------------------------------
    //  Firebase load & save
    // -----------------------------------------------------
    private suspend fun loadFromFirebase() {
        val uid = userId
        if (uid == null) {
            _progress.value = PlayerProgress()
            return
        }

        try {
            val doc = db.collection("users")
                .document(uid)
                .collection("progress")
                .document("level")
                .get()
                .await()

            if (doc.exists()) {
                val loaded = doc.toObject(PlayerProgress::class.java)
                if (loaded != null) {
                    _progress.value = loaded
                }
            } else {
                val freshStart = PlayerProgress()
                _progress.value = freshStart
                saveToFirebase(freshStart)
            }
        } catch (e: Exception) {
        }
    }

    private fun saveToFirebase(progress: PlayerProgress) {
        val uid = userId ?: return
        viewModelScope.launch {
            try {
                db.collection("users")
                    .document(uid)
                    .collection("progress")
                    .document("level")
                    .set(progress)
                    .await()
            } catch (_: Exception) {
            }
        }
    }

    // -----------------------------------------------------
    //  XP calculation
    // -----------------------------------------------------
    //  Updated XP calculation in LevelViewModel.kt
    private fun calcNextLevelXp(level: Int): Int {
        val base = 10000.0 // Keep as double for precise math
        val growth = 1.2

        // Level 1 = 10,000
        // Level 2 = 10,000 * 1.2 = 12,000
        // Level 3 = 10,000 * 1.2^2 = 14,400
        val raw = base * Math.pow(growth, (level - 1).toDouble())

        // Round to the nearest 100 for a "clean" UI look
        return (raw / 100).toInt() * 100
    }

    private fun levelUpReward(level: Int): Int {
        val base = 20
        val linear = base + (level - 1) * 2
        val milestoneBonus = if (level % 5 == 0) 30 else 0
        return linear + milestoneBonus
    }
}
