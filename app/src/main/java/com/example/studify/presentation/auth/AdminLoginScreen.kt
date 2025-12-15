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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AdminLoginScreen(
    onAdminLoginSuccess: () -> Unit,
    onBackToUserLogin: () -> Unit,
    onForgotPassword: () -> Unit = {},
) {
    var email by rememberSaveable { mutableStateOf("") }
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

            Text(
                text = "Admin Login",
                color = Coffee,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "For admin accounts only.",
                color = Stone,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Paper),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    LabeledTextField(
                        label = "Admin Email",
                        value = email,
                        onValueChange = {
                            email = it
                            error = null
                        },
                        placeholder = "admin@gmail.com",
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Email
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    LabeledTextField(
                        label = "Password",
                        value = pwd,
                        onValueChange = {
                            pwd = it
                            error = null
                        },
                        placeholder = "At least 6 characters",
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
                        horizontalArrangement = Arrangement.End
                    ) {
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
                    val e = email.trim()
                    val p = pwd

                    if (!looksLikeEmail(e)) {
                        error = "Please enter a valid admin email."
                        return@Button
                    }
                    if (p.length < 6) {
                        error = "Password must be at least 6 characters."
                        return@Button
                    }

                    loading = true
                    scope.launch {
                        try {
                            // 1) Sign in (Firebase Auth)
                            val result = AuthRepository.signIn(e, p)
                            result.onFailure { throw it }

                            // 2) Check Firestore role by EMAIL (more reliable than uid doc id)
                            val snap = FirebaseFirestore.getInstance()
                                .collection("users")
                                .whereEqualTo("email", e)
                                .limit(1)
                                .get(Source.SERVER)
                                .await()

                            val doc = snap.documents.firstOrNull()
                            val role = doc?.getString("role")

                            loading = false

                            if (role == "admin") {
                                onAdminLoginSuccess()
                            } else {
                                FirebaseAuth.getInstance().signOut()
                                error = "This account is not an admin."
                            }
                        } catch (ex: Exception) {
                            loading = false
                            error = try {
                                AuthRepository.signInErrorMessage(ex)
                            } catch (_: Exception) {
                                ex.message ?: "Login failed."
                            }
                        }
                    }
                },
                enabled = email.isNotBlank() && pwd.length >= 6 && !loading,
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

            val backText = buildAnnotatedString {
                withStyle(SpanStyle(color = Stone)) { append("Not an admin? ") }
                withStyle(
                    SpanStyle(
                        color = AccentRed,
                        fontWeight = FontWeight.SemiBold
                    )
                ) { append("Back to user login") }
            }
            Text(backText, modifier = Modifier.clickable { onBackToUserLogin() })
        }
    }
}

/* ---------- Reusable field ---------- */

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

private fun looksLikeEmail(input: String): Boolean {
    if (input.length < 5) return false
    val emailRegex = "[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+".toRegex()
    return emailRegex.matches(input)
}

@Preview(showBackground = true, backgroundColor = 0xFFF8E9D2)
@Composable
private fun PreviewAdminLogin() {
    MaterialTheme {
        AdminLoginScreen(
            onAdminLoginSuccess = {},
            onBackToUserLogin = {}
        )
    }
}
