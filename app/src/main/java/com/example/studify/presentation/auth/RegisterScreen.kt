package com.example.studify.presentation.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import com.example.studify.ui.theme.Cream
import com.example.studify.ui.theme.Coffee
import com.example.studify.ui.theme.Stone
import com.example.studify.ui.theme.Banana
import com.example.studify.ui.theme.AccentRed
import com.example.studify.ui.theme.Paper

data class RegisterForm(
    val uid: String,
    val username: String,
    val email: String
)

@Composable
fun RegisterScreen(
    onRegisterSuccess: (RegisterForm) -> Unit,
    onBackToLogin: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var pwd by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    var pwVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()
    var showSuccess by remember { mutableStateOf(false) }

    val canSubmit = username.length >= 3 &&
            looksLikeEmail(email) &&
            pwd.length >= 6 &&
            confirm == pwd &&
            !loading

    Surface(color = Cream, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scroll)
        ) {
            Spacer(Modifier.height(28.dp))
            Text("Create Account", color = Coffee, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Paper),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {

                    LabeledField(
                        label = "Username",
                        value = username,
                        onValueChange = { username = it; error = null },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(Modifier.height(12.dp))

                    LabeledField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it; error = null },
                        placeholder = "yourname@example.com",
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Email
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    LabeledField(
                        label = "Password",
                        value = pwd,
                        onValueChange = { pwd = it; error = null },
                        placeholder = "At least 6 characters",
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Password
                        ),
                        trailing = {
                            IconButton(onClick = { pwVisible = !pwVisible }) {
                                Icon(
                                    imageVector = if (pwVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = if (pwVisible) "Hide password" else "Show password",
                                    tint = Stone
                                )
                            }
                        },
                        visualTransformation = if (pwVisible) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    Spacer(Modifier.height(12.dp))

                    LabeledField(
                        label = "Confirm password",
                        value = confirm,
                        onValueChange = { confirm = it; error = null },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Password
                        ),
                        trailing = {
                            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                Icon(
                                    imageVector = if (confirmVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = if (confirmVisible) "Hide password" else "Show password",
                                    tint = Stone
                                )
                            }
                        },
                        visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    error = validate(username, email, pwd, confirm)
                    if (error != null) return@Button


                    loading = true

                    scope.launch {
                        val result = AuthRepository.register(
                            email = email,
                            password = pwd,
                            username = username
                        )
                        result
                            .onSuccess { payload ->
                                loading = false
                                onRegisterSuccess(
                                    RegisterForm(
                                        uid = payload.uid,
                                        username = payload.username,
                                        email = payload.email
                                    )
                                )
                                showSuccess = true
                            }
                            .onFailure { e ->
                                loading = false
                                showSuccess = false
                                error = mapFirebaseError(e)
                            }
                    }
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Banana,
                    contentColor = Coffee
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    if (loading) "Signing up..." else "Sign Up",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(14.dp))

            val back = buildAnnotatedString {
                withStyle(SpanStyle(color = Stone)) { append("Already have an account? ") }
                withStyle(SpanStyle(color = AccentRed, fontWeight = FontWeight.SemiBold)) { append("Log in") }
            }
            Text(
                back,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onBackToLogin() }
            )
        }
    }

    if (showSuccess) {
        Dialog(onDismissRequest = { /* Not allow logout from outside */ }) {
            Surface(
                color = Cream,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Sign up successful", color = Coffee, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Your account was created. Please log in to continue.",
                        color = Coffee.copy(alpha = 0.85f),
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = {
                                FirebaseAuth.getInstance().signOut()
                                showSuccess = false
                                onBackToLogin()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Banana,
                                contentColor = Coffee
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("OK") }
                    }
                }
            }
        }
    }
}

/* ---- small components & helpers ---- */

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    trailing: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
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

private fun validate(
    username: String,
    email: String,
    pwd: String,
    confirm: String
): String? {
    if (username.length < 3) return "Username must be at least 3 characters."
    if (!looksLikeEmail(email)) return "Enter a valid email."
    if (pwd.length < 6) return "Password must be at least 6 characters."
    if (pwd != confirm) return "Passwords do not match."
    return null
}

private fun looksLikeEmail(input: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
    return emailRegex.matches(input)
}

@Preview(
    name = "Register – Light",
    showBackground = true,
    backgroundColor = 0xFFF8E9D2
)
@Composable
private fun PreviewRegisterLight() {
    MaterialTheme {
        RegisterScreen(
            onRegisterSuccess = { },
            onBackToLogin = { }
        )
    }
}

private fun mapFirebaseError(e: Throwable): String {
    val msg = e.message ?: return "Registration failed. Try again."
    return when {
        msg.contains("already taken", true) -> "Username is already taken."
        msg.contains("email address is already in use", true) -> "Email is already in use."
        msg.contains("WEAK_PASSWORD", true) || msg.contains("Password should be at least", true) -> "Password is too weak."
        msg.contains("badly formatted", true) -> "Email is badly formatted."
        else -> msg
    }
}
