package com.example.studify.presentation.avatar

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firestore collection: shop_items
 *
 * Each doc contains:
 * - name: String
 * - price: Number (Long)
 * - available: Boolean
 * - imageKey: String (e.g. "acc_cap")
 */
object ShopRepository {

    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection("shop_items")

    data class ShopItemDoc(
        val id: String = "",
        val name: String = "",
        val price: Int = 0,
        val available: Boolean = true,
        val imageKey: String = ""
    )

    private fun QuerySnapshot.toItems(): List<ShopItemDoc> {
        return documents.map { doc ->
            ShopItemDoc(
                id = doc.id,
                name = doc.getString("name") ?: "",
                price = (doc.getLong("price") ?: 0L).toInt(),
                available = doc.getBoolean("available") ?: true,
                imageKey = doc.getString("imageKey") ?: ""
            )
        }.sortedBy { it.name.lowercase() }
    }

    /** Realtime: for Admin management screen */
    fun observeAllItems(): Flow<Result<List<ShopItemDoc>>> = callbackFlow {
        val reg: ListenerRegistration = col.addSnapshotListener { snap, e ->
            if (e != null) {
                trySend(Result.failure(e))
                return@addSnapshotListener
            }
            val list = snap?.toItems() ?: emptyList()
            trySend(Result.success(list))
        }
        awaitClose { reg.remove() }
    }

    /** Realtime: for ShopScreen (only available items) */
    fun observeAvailableItems(): Flow<Result<List<ShopItemDoc>>> = callbackFlow {
        val reg: ListenerRegistration =
            col.whereEqualTo("available", true)
                .addSnapshotListener { snap, e ->
                    if (e != null) {
                        trySend(Result.failure(e))
                        return@addSnapshotListener
                    }
                    val list = snap?.toItems() ?: emptyList()
                    trySend(Result.success(list))
                }
        awaitClose { reg.remove() }
    }

    /** One-shot load (if you ever need it) */
    suspend fun getAvailableItemsOnce(): Result<List<ShopItemDoc>> = runCatching {
        val snap = col.whereEqualTo("available", true).get().await()
        snap.toItems()
    }

    /** Update available true/false (used by your Edit screen later) */
    suspend fun setAvailable(itemId: String, available: Boolean): Result<Unit> = runCatching {
        col.document(itemId).update("available", available).await()
    }

    /** Update fields (optional, for full edit later) */
    suspend fun updateItem(
        itemId: String,
        name: String,
        price: Int,
        available: Boolean
    ): Result<Unit> = runCatching {
        col.document(itemId).update(
            mapOf(
                "name" to name,
                "price" to price,
                "available" to available
            )
        ).await()
    }
}
