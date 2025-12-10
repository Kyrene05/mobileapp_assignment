package com.example.studify.presentation.avatar

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object AvatarRepository {
    private const val TAG = "AvatarRepo"
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private fun userDoc(uid: String) = db.collection("users").document(uid)
    private fun avatarDoc(uid: String) = userDoc(uid).collection("avatar").document("profile")


    suspend fun save(profile: AvatarProfile): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not signed in")
        val data = mapOf(
            "baseColor"   to profile.baseColor,
            "accessories" to profile.accessories.toList(),
            "owned"       to profile.owned.toList(),
            "updatedAt"   to FieldValue.serverTimestamp()
        )
        avatarDoc(uid).set(data, SetOptions.merge()).await()
        Log.d(TAG, "✅ Avatar saved successfully")
    }


    suspend fun load(uid: String): Result<AvatarProfile> = runCatching {
        val snap = avatarDoc(uid).get().await()
        val color = snap.getString("baseColor") ?: "grey"
        val accessories = (snap.get("accessories") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        val owned = ((snap.get("owned") as? List<*>)?.filterIsInstance<String>() ?: accessories).toSet()
        AvatarProfile(baseColor = color, accessories = accessories, owned = owned)
    }

    suspend fun getUserData(): Result<Pair<AvatarProfile, Int>> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not signed in")
        val avatarSnap = avatarDoc(uid).get().await()
        val userSnap   = userDoc(uid).get().await()

        val color = avatarSnap.getString("baseColor") ?: "grey"
        val accessories =
            (avatarSnap.get("accessories") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

        val owned = ((avatarSnap.get("owned") as? List<*>)?.filterIsInstance<String>()
            ?: DEFAULT_OWNED.toList()).toSet()

        val coins = (userSnap.getLong("coins") ?: 0L).toInt()

        AvatarProfile(baseColor = color, accessories = accessories, owned = owned) to coins
    }

    suspend fun updateAvatarAndCoins(profile: AvatarProfile, coins: Int): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not signed in")

        userDoc(uid).set(mapOf("coins" to coins), SetOptions.merge()).await()
        avatarDoc(uid).set(
            mapOf(
                "baseColor"   to profile.baseColor,
                "accessories" to profile.accessories.toList(),
                "owned"       to profile.owned.toList(),
                "updatedAt"   to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
    }
}
