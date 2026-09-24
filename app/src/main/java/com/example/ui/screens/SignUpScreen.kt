package com.example.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WorkoraTheme

enum class SignUpRole {
    CUSTOMER,
    WORKER
}

@Composable
fun SignUpScreen(
    onSignUp: (name: String, email: String, password: String, role: SignUpRole) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    val colors = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var selectedRole by rememberSaveable { mutableStateOf(SignUpRole.CUSTOMER) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    val shownError: String? = localError ?: errorMessage

    val submit: () -> Unit = {
        focusManager.clearFocus()
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        localError = when {
            cleanName.isEmpty() -> "Enter your full name."
            cleanEmail.isEmpty() -> "Enter your email address."
            !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches() -> "Enter a valid email address."
            password.length < 6 -> "Password must be at least 6 characters."
            password != confirmPassword -> "Passwords do not match."
            else -> null
        }
        if (localError == null) {
            onSignUp(cleanName, cleanEmail, password, selectedRole)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create your account",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Join Workora to hire skilled people or find work.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "I am here to",
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurface,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SignUpRoleCard(
                title = "Hire",
                subtitle = "Find workers for your jobs",
                selected = selectedRole == SignUpRole.CUSTOMER,
                enabled = !isLoading,
                onClick = { selectedRole = SignUpRole.CUSTOMER },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            SignUpRoleCard(
                title = "Work",
                subtitle = "Find jobs near you",
                selected = selectedRole == SignUpRole.WORKER,
                enabled = !isLoading,
                onClick = { selectedRole = SignUpRole.WORKER },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SignUpField(
            value = name,
            onValueChange = {
                name = it
                localError = null
            },
            label = "Full name",
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            enabled = !isLoading,
            capitalization = KeyboardCapitalization.Words
        )

        Spacer(modifier = Modifier.height(14.dp))

        SignUpField(
            value = email,
            onValueChange = {
                email = it
                localError = null
            },
            label = "Email",
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(14.dp))

        SignUpField(
            value = password,
            onValueChange = {
                password = it
                localError = null
            },
            label = "Password",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            enabled = !isLoading,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailing = {
                TextButton(
                    onClick = { passwordVisible = !passwordVisible },
                    enabled = !isLoading
                ) {
                    Text(text = if (passwordVisible) "Hide" else "Show")
                }
            },
            supportingText = "At least 6 characters"
        )

        Spacer(modifier = Modifier.height(14.dp))

        SignUpField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                localError = null
            },
            label = "Confirm password",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            enabled = !isLoading,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            onDone = submit
        )

        if (shownError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = shownError,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = submit,
            enabled = !isLoading,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = colors.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Create account",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Already have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            TextButton(
                onClick = onNavigateToLogin,
                enabled = !isLoading
            ) {
                Text(text = "Log in")
            }
        }
    }
}

@Composable
private fun SignUpRoleCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .background(if (selected) colors.primaryContainer else colors.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colors.primary else colors.outline,
                shape = shape
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) colors.onPrimaryContainer else colors.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
        )
    }
}

@Composable
private fun SignUpField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
    supportingText: String? = null,
    onDone: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        label = { Text(text = label) },
        supportingText = if (supportingText != null) {
            { Text(text = supportingText) }
        } else {
            null
        },
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone?.invoke() }
        ),
        trailingIcon = trailing,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.onBackground,
            unfocusedTextColor = colors.onBackground,
            errorTextColor = colors.onBackground,
            disabledTextColor = colors.onBackground.copy(alpha = 0.6f),
            cursorColor = colors.primary
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    WorkoraTheme {
        SignUpScreen(
            onSignUp = { _, _, _, _ -> },
            onNavigateToLogin = {}
        )
    }
}
