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
import com.example.studify.ui.theme.Cream
import com.example.studify.ui.theme.Coffee
import com.example.studify.ui.theme.Stone
import com.example.studify.ui.theme.Banana
import com.example.studify.ui.theme.AccentRed
import com.example.studify.ui.theme.Paper

@Composable
fun AdminLoginScreen(
    onAdminLoginSuccess: () -> Unit,
    onBackToUserLogin: () -> Unit,
    onForgotPassword: () -> Unit = {},
) {
    var user by rememberSaveable { mutableStateOf("") }
    var pwd by rememberSaveable { mutableStateOf("") }
    var pwdVisible by rememberSaveable { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }

    val scroll = rememberScrollState()

    // Default admin credentials (for assignment/demo)
    val defaultAdminEmail = "admin@studify.com"
    val defaultAdminPassword = "admin123"

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
                        value = user,
                        onValueChange = {
                            user = it
                            error = null
                        },
                        placeholder = "admin@studify.com",
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
                        visualTransformation = if (pwdVisible) VisualTransformation.None
                        else PasswordVisualTransformation()
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

            // Login button
            Button(
                onClick = {
                    error = null

                    val email = user.trim()
                    val password = pwd

                    if (!looksLikeEmailOrName(email)) {
                        error = "Please enter a valid admin email."
                        return@Button
                    }
                    if (password.length < 6) {
                        error = "Password must be at least 6 characters."
                        return@Button
                    }

                    // Hard-coded admin check
                    if (email == defaultAdminEmail && password == defaultAdminPassword) {
                        onAdminLoginSuccess()
                    } else {
                        error = "Invalid admin email or password."
                    }
                },
                enabled = user.isNotBlank() && pwd.length >= 6,
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
                Text("Log In", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
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
            Text(
                backText,
                modifier = Modifier.clickable { onBackToUserLogin() }
            )
        }
    }
}

/* ---------- Reusable text field ---------- */

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
    if (input.length < 3) return false
    val emailRegex = "[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+".toRegex()
    return if (input.contains("@")) emailRegex.matches(input) else true
}

/* ---------- Preview ---------- */
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
