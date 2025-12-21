package com.example.studify.presentation.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studify.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    onGoRegister: () -> Unit,
    onForgotPassword: () -> Unit = {},
    onGoAdminLogin: () -> Unit = {},
) {
    var user by rememberSaveable { mutableStateOf("") }
    var pwd by rememberSaveable { mutableStateOf("") }
    var pwdVisible by rememberSaveable { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()

    Surface(color = Cream, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(50.dp))

            Text("Login", color = Coffee, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Paper),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    LabeledTextField(
                        label = "Username or Email",
                        value = user,
                        onValueChange = { user = it; error = null },
                        placeholder = "yourname@gmail.com",
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Email
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    LabeledTextField(
                        label = "Password",
                        value = pwd,
                        onValueChange = { pwd = it; error = null },
                        placeholder = "At least 8 characters",
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Password
                        ),
                        trailing = {
                            IconButton(onClick = { pwdVisible = !pwdVisible }) {
                                Icon(
                                    imageVector = if (pwdVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Toggle password",
                                    tint = Stone
                                )
                            }
                        },
                        visualTransformation = if (pwdVisible) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Admin? Click here",
                            color = AccentRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onGoAdminLogin() }
                        )

                        Text(
                            "Forgot password?",
                            color = Stone,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { onForgotPassword() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    error = null

                    // Inside Button onClick
                    val input = user.trim()
                    val password = pwd

                    if (!looksLikeEmailOrName(input)) {
                        error = "Please enter a valid email or username."
                        return@Button
                    }

// Check for "Proper" password rules (at least 1 letter and 1 number)
                    if (password.length < 8 || !password.any { it.isDigit() } || !password.any { it.isLetter() }) {
                        error = "Password must be at least 8 characters and contain letters and numbers."
                        return@Button
                    }

                    loading = true
                    val launch = scope.launch {
                        try {
                            // 1) Sign in the user
                            val result = AuthRepository.signIn(input, password)
                            result.onFailure { throw it }

                            // 2) Get the current User ID immediately after sign-in
                            val uid = FirebaseAuth.getInstance().currentUser?.uid

                            if (uid == null) {
                                loading = false
                                error = "Authentication failed."
                                return@launch
                            }

                            // 3) GET THE DOCUMENT DIRECTLY BY UID (Instead of searching by email)
                            // This avoids the "Permission Denied" query error
                            val doc = FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .get(Source.SERVER)
                                .await()

                            if (!doc.exists()) {
                                loading = false
                                error = "User profile not found."
                                return@launch
                            }

                            val role = doc.getString("role")

                            loading = false

                            // 4) Check role
                            if (role == "admin") {
                                FirebaseAuth.getInstance().signOut()
                                error = "This is an admin account. Please use Admin Login."
                            } else {
                                onLoginSuccess()
                            }
                        } catch (e: Exception) {
                            loading = false
                            error = when (e) {
                                is FirebaseAuthInvalidUserException ->
                                    "No account found with this email."

                                is FirebaseAuthInvalidCredentialsException ->
                                    "Wrong password. Please try again."

                                is FirebaseNetworkException ->
                                    "No internet connection. Please check your network."

                                else -> "Login failed. Please check your credentials."
                            }
                        }
                    }
                },
                enabled = user.isNotBlank() && pwd.length >= 6 && !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Banana,
                    contentColor = Coffee,
                    disabledContainerColor = Banana.copy(alpha = 0.5f),
                    disabledContentColor = Coffee.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Coffee
                    )
                } else {
                    Text("Log In", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(14.dp))

            val mixed = buildAnnotatedString {
                withStyle(SpanStyle(color = Stone)) { append("New here? ") }
                withStyle(SpanStyle(color = AccentRed, fontWeight = FontWeight.SemiBold)) {
                    append("Create an account")
                }
            }
            Text(mixed, modifier = Modifier.clickable { onGoRegister() })
        }
    }
}

/* ---------- Helpers ---------- */

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    trailing: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Text(
        text = label,
        color = Coffee,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
    )
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = { Text(placeholder, color = Stone) },
        trailingIcon = trailing,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Coffee,
            unfocusedBorderColor = Stone.copy(alpha = 0.6f),
            cursorColor = Coffee,
            focusedTextColor = Coffee,
            unfocusedTextColor = Coffee
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun looksLikeEmailOrName(input: String): Boolean {
    val trimmed = input.trim()
    if (trimmed.length < 3) return false

    return if (trimmed.contains("@")) {
        // Use standard Android email pattern
        android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()
    } else {
        // Basic username check: alphanumeric and at least 3 chars
        trimmed.all { it.isLetterOrDigit() }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8E9D2)
@Composable
private fun PreviewAuth() {
    MaterialTheme {
        AuthScreen(
            onLoginSuccess = {},
            onGoRegister = {},
            onGoAdminLogin = {}
        )
    }
}
