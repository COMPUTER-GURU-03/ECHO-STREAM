package com.example.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.GlassSurface
import com.example.core.ui.glassEffect
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.SecondaryText

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogoutClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is ProfileUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is ProfileUiState.Success -> {
                ProfileContent(
                    displayName = state.profile.displayName ?: "User",
                    email = state.email,
                    onUpdateName = { viewModel.updateDisplayName(it) },
                    onLogout = onLogoutClick
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    displayName: String,
    email: String,
    onUpdateName: (String) -> Unit,
    onLogout: () -> Unit
) {
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(displayName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        // Profile Picture Placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(ElectricIndigo, Color(0xFF8A84FF)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isEditingName) {
            TextField(
                value = editedName,
                onValueChange = { editedName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .glassEffect(12.dp),
                placeholder = { Text("Display Name", color = Color.White.copy(alpha = 0.5f)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                trailingIcon = {
                    TextButton(onClick = {
                        onUpdateName(editedName)
                        isEditingName = false
                    }) {
                        Text("Save", color = ElectricIndigo, fontWeight = FontWeight.Bold)
                    }
                }
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = { isEditingName = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Name",
                        tint = ElectricIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Text(
            text = email,
            style = MaterialTheme.typography.bodyLarge,
            color = SecondaryText,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Profile Menu Items (Placeholders)
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ProfileMenuItem(title = "App Settings")
            ProfileMenuItem(title = "Subscription")
            ProfileMenuItem(title = "Account Security")
            ProfileMenuItem(title = "Help & Support")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", style = MaterialTheme.typography.titleLarge)
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun ProfileMenuItem(title: String) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        cornerRadius = 12.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = title, color = Color.White, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
