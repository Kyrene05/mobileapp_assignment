package com.example.studify.presentation.welcome

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.ClickableText
import com.example.studify.ui.theme.AccentRed
import com.example.studify.ui.theme.Cream
import com.example.studify.ui.theme.Coffee
import com.example.studify.ui.theme.Banana
import com.example.studify.ui.theme.Stone

@Composable
fun WelcomeScreen(
    onGoLogin: () -> Unit,          // normal user flow
    onGoAdminLogin: () -> Unit,     // NEW: admin login flow
    onViewTerms: () -> Unit = {},
    showTosInitially: Boolean = false
) {
    var showTos by rememberSaveable { mutableStateOf(showTosInitially) }

    Surface(color = Cream, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(230.dp))

            Text(
                "StudyMEOW😽",
                color = Coffee,
                fontSize = 35.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(100.dp))

            Button(
                onClick = { showTos = true },
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Banana,
                    contentColor = Coffee
                ),
                modifier = Modifier
                    .width(180.dp)
                    .height(45.dp)
            ) {
                Text("Start !", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(20.dp))

          /*  // --- Admin link under Start button ---
            val adminText: AnnotatedString = buildAnnotatedString {
                withStyle(SpanStyle(color = Stone, fontSize = 13.sp)) {
                    append("Admin? ")
                }
                pushStringAnnotation(
                    tag = "ADMIN_LOGIN",
                    annotation = "admin_login"
                )
                withStyle(
                    SpanStyle(
                        color = AccentRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                ) {
                    append("Click here to login")
                }
                pop()
            }*/

            /*ClickableText(
                text = adminText,
                onClick = { offset ->
                    // Only react when the red text is clicked
                    adminText.getStringAnnotations(
                        tag = "ADMIN_LOGIN",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let {
                        onGoAdminLogin()
                    }
                }
            )*/
        }
    }

    if (showTos) {
        Dialog(onDismissRequest = { showTos = false }) {
            Surface(
                color = Cream,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "Terms of Service",
                        color = Coffee,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Please confirm that you agree to our Terms of Service.",
                        color = Coffee.copy(0.85f),
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showTos = false },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Coffee
                            )
                        ) { Text("Cancel") }

                        Spacer(Modifier.width(10.dp))

                        Button(
                            onClick = {
                                showTos = false
                                onGoLogin()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Banana,
                                contentColor = Coffee
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Agree") }
                    }
                }
            }
        }
    }
}

/* ---------- Previews ---------- */

@Preview(
    name = "Welcome – Normal",
    showBackground = true,
    backgroundColor = 0xFFF8E9D2
)
@Composable
private fun PreviewWelcome() {
    MaterialTheme {
        WelcomeScreen(
            onGoLogin = {},
            onGoAdminLogin = {},   // preview stub
            onViewTerms = {}
        )
    }
}

@Preview(
    name = "Welcome – With TOS Dialog",
    showBackground = true,
    backgroundColor = 0xFFF8E9D2
)
@Composable
private fun PreviewWelcomeWithDialog() {
    MaterialTheme {
        WelcomeScreen(
            onGoLogin = {},
            onGoAdminLogin = {},
            onViewTerms = {},
            showTosInitially = true
        )
    }
}
