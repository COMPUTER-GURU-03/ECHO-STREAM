package com.example.features.authentication.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.glassEffect
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.SecondaryText

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String? = null,
    onDismissError: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "MOVIE DB",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = ElectricIndigo
        )
        Text(
            text = "Welcome back to the cinema",
            style = MaterialTheme.typography.bodyLarge,
            color = SecondaryText,
            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
        )

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            icon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            icon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible }
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = onForgotPasswordClick) {
                Text("Forgot Password?", color = ElectricIndigo, style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onLoginClick(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "Login", style = MaterialTheme.typography.titleLarge)
            }
        }

        Row(
            modifier = Modifier.padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account?", color = SecondaryText)
            TextButton(onClick = onRegisterClick) {
                Text("Register", color = ElectricIndigo, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = onDismissError,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Authentication Error", color = Color.White) },
            text = { Text(errorMessage, color = SecondaryText) },
            confirmButton = {
                TextButton(onClick = onDismissError) {
                    Text("OK", color = ElectricIndigo)
                }
            }
        )
    }
}

@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String? = null,
    onDismissError: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val passwordsMatch = password == confirmPassword && password.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Join MOVIE DB for the ultimate experience",
            style = MaterialTheme.typography.bodyLarge,
            color = SecondaryText,
            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
        )

        AuthTextField(
            value = name,
            onValueChange = { name = it },
            label = "Display Name",
            icon = Icons.Default.Person
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            icon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            icon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            icon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible }
        )

        if (confirmPassword.isNotEmpty() && !passwordsMatch) {
            Text(
                text = "Passwords do not match",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onRegisterClick(email, password, name) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && name.isNotBlank() && passwordsMatch,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "Sign Up", style = MaterialTheme.typography.titleLarge)
            }
        }

        Row(
            modifier = Modifier.padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account?", color = SecondaryText)
            TextButton(onClick = onLoginClick) {
                Text("Login", color = ElectricIndigo, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = onDismissError,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Registration Error", color = Color.White) },
            text = { Text(errorMessage, color = SecondaryText) },
            confirmButton = {
                TextButton(onClick = onDismissError) {
                    Text("OK", color = ElectricIndigo)
                }
            }
        )
    }
}

@Composable
fun ForgotPasswordScreen(
    onResetClick: (String) -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String? = null,
    onDismissError: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var resetSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Reset Password",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Enter your email to receive a reset link",
            style = MaterialTheme.typography.bodyLarge,
            color = SecondaryText,
            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
        )

        if (!resetSent) {
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { 
                    onResetClick(email)
                    resetSent = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && email.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Send Reset Link", style = MaterialTheme.typography.titleLarge)
                }
            }
        } else {
            Text(
                text = "Check your email for instructions to reset your password.",
                style = MaterialTheme.typography.bodyLarge,
                color = ElectricIndigo,
                modifier = Modifier.padding(bottom = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        TextButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "Back to Login",
                color = ElectricIndigo,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { 
                onDismissError()
                resetSent = false
            },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Reset Error", color = Color.White) },
            text = { Text(errorMessage, color = SecondaryText) },
            confirmButton = {
                TextButton(onClick = { 
                    onDismissError()
                    resetSent = false
                }) {
                    Text("OK", color = ElectricIndigo)
                }
            }
        )
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {}
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .glassEffect(12.dp),
        placeholder = { Text(label, color = Color.White.copy(alpha = 0.5f)) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = ElectricIndigo) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = onPasswordToggle) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = SecondaryText
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = ElectricIndigo,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}
