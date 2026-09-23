// AuthScreen.kt
// NOTE: change the package line below if your project uses a different package name.
package com.workora.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Roles are plain Strings everywhere (no enum).
const val ROLE_WORKER: String = "WORKER"
const val ROLE_EMPLOYER: String = "EMPLOYER"

private val BrandColor = Color(0xFF1565C0)
private val DarkText = Color(0xFF111111)
private val MutedText = Color(0xFF616161)
private val BorderGray = Color(0xFF9E9E9E)
private val ErrorRed = Color(0xFFB00020)

/**
 * Login / Register screen.
 *
 * Parameter names match the User model fields: name, email, phoneNumber, role.
 * The password is only a form input passed to the callback; it is not a User field.
 *
 * @param onLogin    (email, password, role)
 * @param onRegister (name, email, phoneNumber, password, role)
 */
@Composable
fun AuthScreen(
    onLogin: (email: String, password: String, role: String) -> Unit,
    onRegister: (name: String, email: String, phoneNumber: String, password: String, role: String) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var isLoginMode by rememberSaveable { mutableStateOf(true) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(ROLE_WORKER) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    val shownError: String? = localError ?: errorMessage

    val submit: () -> Unit = {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        val cleanPhone = phoneNumber.trim()

        val validationError: String? = validateAuthInput(
            isLoginMode = isLoginMode,
            name = cleanName,
            email = cleanEmail,
            phoneNumber = cleanPhone,
            password = password
        )

        if (validationError != null) {
            localError = validationError
        } else {
            localError = null
            if (isLoginMode) {
                onLogin(cleanEmail, password, role)
            } else {
                onRegister(cleanName, cleanEmail, cleanPhone, password, role)
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Workora",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = BrandColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isLoginMode) "Welcome back! Please log in." else "Create your account",
                fontSize = 16.sp,
                color = MutedText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "I am a",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = DarkText,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RoleOption(
                    text = "Worker",
                    selected = role == ROLE_WORKER,
                    onClick = { role = ROLE_WORKER },
                    modifier = Modifier.weight(1f)
                )
                RoleOption(
                    text = "Employer",
                    selected = role == ROLE_EMPLOYER,
                    onClick = { role = ROLE_EMPLOYER },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isLoginMode) {
                AuthTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full name",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!isLoginMode) {
                AuthTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = "Phone number",
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                enabled = !isLoading,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingContent = {
                    TextButton(
                        onClick = { passwordVisible = !passwordVisible },
                        enabled = !isLoading
                    ) {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            color = BrandColor
                        )
                    }
                },
                onDone = submit
            )

            if (shownError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = shownError,
                    color = ErrorRed,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = submit,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColor,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isLoginMode) "Log in" else "Register",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    isLoginMode = !isLoginMode
                    localError = null
                },
                enabled = !isLoading
            ) {
                Text(
                    text = if (isLoginMode) {
                        "Don't have an account? Register"
                    } else {
                        "Already have an account? Log in"
                    },
                    color = BrandColor
                )
            }
        }
    }
}

@Composable
private fun RoleOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandColor,
                contentColor = Color.White
            )
        ) {
            Text(text = text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandColor)
        ) {
            Text(text = text)
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null,
    onDone: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        label = { Text(text = label) },
        textStyle = LocalTextStyle.current.copy(color = DarkText),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone?.invoke() }
        ),
        trailingIcon = trailingContent,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrandColor,
            unfocusedBorderColor = BorderGray,
            focusedLabelColor = BrandColor,
            unfocusedLabelColor = MutedText,
            cursorColor = BrandColor
        )
    )
}

private fun validateAuthInput(
    isLoginMode: Boolean,
    name: String,
    email: String,
    phoneNumber: String,
    password: String
): String? {
    if (!isLoginMode && name.isEmpty()) {
        return "Please enter your full name."
    }
    if (email.isEmpty()) {
        return "Please enter your email."
    }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        return "Please enter a valid email address."
    }
    if (!isLoginMode) {
        if (phoneNumber.isEmpty()) {
            return "Please enter your phone number."
        }
        if (phoneNumber.count { it.isDigit() } < 10) {
            return "Please enter a valid phone number."
        }
    }
    if (password.isEmpty()) {
        return "Please enter your password."
    }
    if (!isLoginMode && password.length < 6) {
        return "Password must be at least 6 characters."
    }
    return null
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    AuthScreen(
        onLogin = { _, _, _ -> },
        onRegister = { _, _, _, _, _ -> }
    )
}
