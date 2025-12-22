package com.example.studify.presentation.auth

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

data class RegisterPayload(
    val uid: String,
    val email: String,
    val username: String
)

object AuthRepository {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    fun currentUid(): String? = auth.currentUser?.uid

    fun signOut() {
        auth.signOut()
    }

    /**
    * UPDATED: Sign in with email OR username.
    */
    suspend fun signIn(emailOrUsername: String, password: String): Result<Unit> = runCatching {
        val login = emailOrUsername.trim()

        // 1. If it's an email, sign in immediately to get 'auth' context
        if (login.contains("@")) {
            auth.signInWithEmailAndPassword(login, password).await()
        } else {
            // 2. If it's a username, we must use a 'Service Account' logic or
            // temporary permissive rules to find the email first
            val snap = db.collection("users")
                .whereEqualTo("username", login)
                .limit(1)
                .get(Source.SERVER)
                .await()

            val doc = snap.documents.firstOrNull()
                ?: throw IllegalArgumentException("Username not found.")

            val email = doc.getString("email") ?: throw IllegalStateException("Email missing.")
            auth.signInWithEmailAndPassword(email, password).await()
        }
    }

    /**
     * Human-friendly error for sign-in.
     */
    fun signInErrorMessage(t: Throwable): String {
        val msg = t.message ?: "Sign in failed."
        val code = (t as? FirebaseAuthException)?.errorCode ?: ""
        return when {
            code.equals("ERROR_USER_NOT_FOUND", true) -> "Account does not exist."
            code.equals("ERROR_WRONG_PASSWORD", true) -> "Incorrect password."
            code.equals("ERROR_USER_DISABLED", true) -> "This account has been disabled."
            code.equals("ERROR_TOO_MANY_REQUESTS", true) -> "Too many attempts. Try again later."
            msg.contains("password is invalid", true) -> "Incorrect password."
            msg.contains("no user record", true) -> "Account does not exist."
            msg.contains("Username not found", true) -> "Username not found."
            else -> msg
        }
    }

    /**
     * UPDATED: Create user and profile.
     */
    suspend fun register(
        email: String,
        password: String,
        username: String
    ): Result<RegisterPayload> = runCatching {
        val trimmedEmail = email.trim()
        val trimmedUser = username.trim()

        // 1. Create Auth User FIRST. This makes request.auth != null
        val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
        val user = authResult.user ?: error("User creation failed.")

        // 2. Now that we are signed in, check if username is unique
        val exists = db.collection("users")
            .whereEqualTo("username", trimmedUser)
            .limit(1)
            .get()
            .await()
            .isEmpty
            .not()

        if (exists) {
            // If username exists, delete the auth user we just made and throw error
            user.delete().await()
            error("Username is already taken.")
        }

        // 3. Write profile to Firestore users/{uid}
        val payload = RegisterPayload(uid = user.uid, email = trimmedEmail, username = trimmedUser)

        db.collection("users").document(user.uid).set(
            mapOf(
                "email" to trimmedEmail,
                "username" to trimmedUser,
                "role" to "user",
                "createdAt" to Timestamp.now()
            )
        ).await()

        payload
    }

    /**
     * Send password reset email.
     */
    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    /**
     * Admin check using Firestore users/{uid}.role == "admin"
     * Returns true only if role is exactly "admin".
     */
    suspend fun isCurrentUserAdmin(source: Source = Source.SERVER): Result<Boolean> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching false

        val snap = db.collection("users")
            .document(uid)
            .get(source)
            .await()

        val role = snap.getString("role")
        role == "admin"
    }

    /**
     * Optional helper: set role for a user (use this only for dev/admin setup tools)
     * You usually set role from Firebase console, not from the app UI.
     */
    suspend fun setUserRole(uid: String, role: String): Result<Unit> = runCatching {
        db.collection("users")
            .document(uid)
            .update("role", role)
            .await()
    }
}
