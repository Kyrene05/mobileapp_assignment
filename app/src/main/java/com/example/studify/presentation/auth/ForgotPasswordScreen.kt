package com.example.studify.presentation.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studify.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit
) {

    var email by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Surface(color = Cream, modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(80.dp))

            Text("Reset Password", color = Coffee, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(10.dp))

            Text(
                "Enter your email and we’ll send you a link to reset your password.",
                color = Stone,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Paper),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            error = null
                            info = null
                        },
                        singleLine = true,
                        placeholder = { Text("yourname@example.com", color = Stone) },
                        label = { Text("Email") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Coffee,
                            unfocusedBorderColor = Stone.copy(alpha = 0.6f),
                            cursorColor = Coffee
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                    if (info != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(info!!, color = Coffee, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    error = null
                    info = null

                    if (!looksLikeEmail(email)) {
                        error = "Please enter a valid email."
                        return@Button
                    }

                    loading = true

                    scope.launch {
                        val result = AuthRepository.sendPasswordReset(email.trim())
                        loading = false
                        result.onSuccess {
                            info = "Reset email sent! Please check your inbox or your spam folder."
                        }.onFailure { e ->
                            error = e.message ?: "Failed to send email. Try again later."
                        }
                    }
                },
                enabled = email.isNotBlank() && !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Banana,
                    contentColor = Coffee
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(
                    if (loading) "Sending..." else "Send Reset Email",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "Back to Login",
                color = AccentRed,
                fontSize = 15.sp,
                modifier = Modifier.clickable { onBack() }
            )
        }
    }
}

private fun looksLikeEmail(input: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
    return emailRegex.matches(input)
}

@Preview(showBackground = true, backgroundColor = 0xFFF8E9D2)
@Composable
private fun PreviewForgotPassword() {
    MaterialTheme {
        ForgotPasswordScreen(onBack = {})
    }
}
