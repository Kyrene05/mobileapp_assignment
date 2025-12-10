package com.example.studify.presentation.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class RegisterPayload(
    val uid: String,
    val email: String,
    val username: String
)

object AuthRepository {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    /** Sign in with email or username */
    suspend fun signIn(emailOrUsername: String, password: String): Result<Unit> = runCatching {
        val email = if (emailOrUsername.contains("@")) {
            emailOrUsername.trim()
        } else {
            val snap = db.collection("users")
                .whereEqualTo("username", emailOrUsername.trim())
                .limit(1)
                .get()
                .await()
            val doc = snap.documents.firstOrNull()
                ?: throw IllegalArgumentException("Username not found.")
            (doc.getString("email") ?: "").ifEmpty {
                throw IllegalStateException("Email not found for this username.")
            }
        }
        auth.signInWithEmailAndPassword(email, password).await()
    }

    /** Human-friendly error for sign-in */
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

    /** Create user; set displayName = username; save to Firestore users/{uid} */
    suspend fun register(
        email: String,
        password: String,
        username: String
    ): Result<RegisterPayload> = runCatching {
        val trimmedEmail = email.trim()
        val trimmedUser = username.trim()

        // (optional) ensure username unique
        val exists = db.collection("users")
            .whereEqualTo("username", trimmedUser)
            .limit(1)
            .get()
            .await()
            .isEmpty
            .not()
        if (exists) error("Username is already taken.")

        // 1) create Auth user
        val user = auth.createUserWithEmailAndPassword(trimmedEmail, password).await().user
            ?: error("User is null after createUser")

        // 2) set Firebase Auth profile displayName = username
        val req = UserProfileChangeRequest.Builder()
            .setDisplayName(trimmedUser)
            .build()
        user.updateProfile(req).await()

        // 3) write profile to Firestore
        val payload = RegisterPayload(uid = user.uid, email = trimmedEmail, username = trimmedUser)
        db.collection("users").document(user.uid).set(
            mapOf(
                "email" to trimmedEmail,
                "username" to trimmedUser,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
        ).await()

        payload
    }
    /*Forget password*/
    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

}
