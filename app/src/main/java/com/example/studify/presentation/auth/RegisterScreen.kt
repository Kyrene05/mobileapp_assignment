package com.example.studify.presentation.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import com.example.studify.ui.theme.*

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

    var generalError by remember { mutableStateOf<String?>(null) }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()
    var showSuccess by remember { mutableStateOf(false) }

    // Store UID temporarily for the dialog
    var registeredUid by remember { mutableStateOf("") }

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
                        onValueChange = { username = it; fieldErrors = fieldErrors - "username" },
                        error = fieldErrors["username"]
                    )

                    Spacer(Modifier.height(12.dp))

                    LabeledField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it; fieldErrors = fieldErrors - "email" },
                        error = fieldErrors["email"],
                        placeholder = "yourname@gmail.com"
                    )

                    Spacer(Modifier.height(12.dp))

                    LabeledField(
                        label = "Password",
                        value = pwd,
                        onValueChange = { pwd = it; fieldErrors = fieldErrors - "password" },
                        error = fieldErrors["password"],
                        placeholder = "At least 8 characters",
                        visualTransformation = if (pwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailing = {
                            IconButton(onClick = { pwVisible = !pwVisible }) {
                                Icon(if (pwVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null, tint = Stone)
                            }
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    LabeledField(
                        label = "Confirm password",
                        value = confirm,
                        onValueChange = { confirm = it; fieldErrors = fieldErrors - "confirm" },
                        error = fieldErrors["confirm"],
                        visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailing = {
                            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                Icon(if (confirmVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null, tint = Stone)
                            }
                        }
                    )

                    if (generalError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(generalError!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    generalError = null
                    val errors = validateAll(username, email, pwd, confirm)
                    fieldErrors = errors

                    if (errors.isEmpty()) {
                        loading = true
                        scope.launch {
                            val result = AuthRepository.register(
                                email = email.trim().lowercase(),
                                password = pwd,
                                username = username.trim()
                            )
                            result.onSuccess { payload ->
                                loading = false
                                registeredUid = payload.uid // Save UID for the OK button
                                showSuccess = true // Show dialog ONLY
                            }.onFailure { e ->
                                loading = false
                                generalError = mapFirebaseError(e)
                            }
                        }
                    }
                },
                enabled = !loading && username.isNotBlank() && email.isNotBlank() && pwd.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Banana, contentColor = Coffee),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Coffee)
                else Text("Sign Up", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(14.dp))

            val back = buildAnnotatedString {
                withStyle(SpanStyle(color = Stone)) { append("Already have an account? ") }
                withStyle(SpanStyle(color = AccentRed, fontWeight = FontWeight.SemiBold)) { append("Log in") }
            }
            Text(back, modifier = Modifier.align(Alignment.CenterHorizontally).clickable { onBackToLogin() })
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showSuccess) {
        Dialog(onDismissRequest = { }) {
            Surface(color = Cream, shape = RoundedCornerShape(16.dp), shadowElevation = 8.dp) {
                Column(Modifier.padding(20.dp)) {
                    Text("Sign up successful", color = Coffee, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text("Your account was created. Please log in to continue.", color = Coffee.copy(alpha = 0.85f))
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = {
                                // TRIGGER NAVIGATION ONLY HERE
                                FirebaseAuth.getInstance().signOut()
                                showSuccess = false
                                onRegisterSuccess(RegisterForm(registeredUid, username, email))
                                onBackToLogin()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Banana, contentColor = Coffee),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("OK") }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    placeholder: String = "",
    trailing: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = Coffee, fontSize = 15.sp, modifier = Modifier.padding(bottom = 6.dp, start = 2.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            isError = error != null,
            placeholder = { Text(placeholder, color = Stone) },
            trailingIcon = trailing,
            visualTransformation = visualTransformation,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Coffee,
                unfocusedBorderColor = Stone.copy(alpha = 0.6f),
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedTextColor = Coffee,
                unfocusedTextColor = Coffee
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
        }
    }
}

private fun validateAll(username: String, email: String, pwd: String, confirm: String): Map<String, String> {
    val errors = mutableMapOf<String, String>()
    val trimmedEmail = email.trim().lowercase()

    // STRICT DOMAIN CHECK
    val allowedDomains = listOf("gmail.com", "yahoo.com", "outlook.com", "hotmail.com")

    if (username.trim().length < 3) errors["username"] = "Username must be at least 3 characters."

    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
        errors["email"] = "Invalid email format."
    } else if (!allowedDomains.any { trimmedEmail.endsWith(it) }) {
        errors["email"] = "Only Gmail, Yahoo, or Outlook are allowed."
    }

    if (pwd.length < 8 || !pwd.any { it.isDigit() } || !pwd.any { it.isLetter() }) {
        errors["password"] = "Need at least 8 characters, must contain letters and numbers."
    }
    if (pwd != confirm) errors["confirm"] = "Passwords do not match."

    return errors
}

private fun mapFirebaseError(e: Throwable): String {
    return when (e) {
        is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "Email already registered."
        is com.google.firebase.FirebaseNetworkException -> "Network error."
        else -> e.localizedMessage ?: "Registration failed."
    }
}