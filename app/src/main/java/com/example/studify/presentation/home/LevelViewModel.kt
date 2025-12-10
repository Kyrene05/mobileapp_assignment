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

// 🔹 Firestore data class
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

    // -----------------------------------------------------
    // ⭐ 1. pomodoro reward: gain XP & Coins
    // -----------------------------------------------------
    fun grantSessionReward(exp: Int, coinsGain: Int = exp) {
        if (exp <= 0 && coinsGain <= 0) return

        viewModelScope.launch {
            var p = _progress.value

            var newXp = p.xp + exp
            var newLevel = p.level
            var newCoins = p.coins + coinsGain
            var newNext = p.nextLevelXp

            // Level up calculation
            while (newXp >= newNext) {
                newXp -= newNext
                newLevel += 1
                newCoins += levelUpReward(newLevel)
                newNext = calcNextLevelXp(newLevel)
            }

            val updated = PlayerProgress(
                level = newLevel,
                xp = newXp,
                nextLevelXp = newNext,
                coins = newCoins
            )

            _progress.value = updated
            saveToFirebase(updated)
        }
    }
    fun updateCoins(newCoins: Int) {
        val current = _progress.value
        val updated = current.copy(coins = newCoins)

        _progress.value = updated

        saveToFirebase(updated)
    }

    // -----------------------------------------------------
    // ⭐ 2. Shop
    // -----------------------------------------------------
    fun overrideCoins(newCoins: Int) {
        val p = _progress.value

        val updated = p.copy(coins = newCoins)
        _progress.value = updated

        saveToFirebase(updated)
    }

    // -----------------------------------------------------
    // 🔻 Firebase load & save
    // -----------------------------------------------------
    private suspend fun loadFromFirebase() {
        val uid = userId ?: return
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
                saveToFirebase(_progress.value)
            }
        } catch (_: Exception) {
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
    // 🔻 XP calculation
    // -----------------------------------------------------
    private fun calcNextLevelXp(level: Int): Int {
        val base = 10000
        val growth = 1.2
        val raw = base * Math.pow(growth, (level - 1).toDouble())
        return (raw / 10).toInt() * 10
    }

    private fun levelUpReward(level: Int): Int {
        val base = 20
        val linear = base + (level - 1) * 2
        val milestoneBonus = if (level % 5 == 0) 30 else 0
        return linear + milestoneBonus
    }
}
