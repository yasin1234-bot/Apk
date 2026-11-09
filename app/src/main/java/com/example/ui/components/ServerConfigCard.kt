package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ServerConfig
import com.example.ui.theme.StatusConnectedGreen
import com.example.ui.theme.StatusConnectedGreenBg
import com.example.ui.theme.StatusDisconnectedRed
import com.example.ui.theme.StatusDisconnectedRedBg
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TextMutedSlate
import com.example.ui.theme.TextPrimarySlate
import com.example.ui.theme.TextSecondarySlate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerConfigCard(
    serverConfig: ServerConfig,
    onSetApiUrl: (String) -> Unit,
    onTestConnection: () -> Unit,
    onToggleSimulator: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputUrl by remember(serverConfig.apiUrl) { mutableStateOf(serverConfig.apiUrl) }
    val focusManager = LocalFocusManager.current

    val statusDotColor by animateColorAsState(
        targetValue = if (serverConfig.isConnected) StatusConnectedGreen else StatusDisconnectedRed,
        label = "statusDotColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("server_config_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // Label: "SERVER CONFIGURATION" (gray, uppercase)
            Text(
                text = "SERVER CONFIGURATION",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextMutedSlate,
                modifier = Modifier.testTag("server_config_title")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Label: "Termux Server API Endpoint (URL)" (gray)
            Text(
                text = "Termux Server API Endpoint (URL)",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = TextSecondarySlate
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Input: A text input field with placeholder "Enter Termux API Endpoint (URL)"
            OutlinedTextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_url_input"),
                placeholder = {
                    Text(
                        text = "Enter Termux API Endpoint (URL)",
                        color = TextMutedSlate
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "API Link",
                        tint = TealPrimary
                    )
                },
                trailingIcon = {
                    if (serverConfig.isTestingConnection) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = TealPrimary
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onSetApiUrl(inputUrl)
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    focusedTextColor = TextPrimarySlate,
                    unfocusedTextColor = TextPrimarySlate
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Preset Chips for convenience
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("http://127.0.0.1:5000", "http://192.168.1.100:5000", "http://localhost:5000").forEach { preset ->
                    FilterChip(
                        selected = inputUrl == preset,
                        onClick = {
                            inputUrl = preset
                            onSetApiUrl(preset)
                        },
                        label = {
                            Text(
                                text = preset.removePrefix("http://"),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE0F2F1),
                            selectedLabelColor = TealPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button & Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Button: A teal "SET API URL" button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onSetApiUrl(inputUrl)
                    },
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("set_api_url_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SET API URL",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                // Status Indicator: DISCONNECTED (red dot) / CONNECTED (green dot)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (serverConfig.isConnected) StatusConnectedGreenBg else StatusDisconnectedRedBg,
                    modifier = Modifier.testTag("status_indicator")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Glowing status dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(statusDotColor)
                        )
                        Text(
                            text = if (serverConfig.isConnected) "CONNECTED" else "DISCONNECTED",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = if (serverConfig.isConnected) StatusConnectedGreen else StatusDisconnectedRed
                        )
                    }
                }
            }

            // Error message if present
            AnimatedVisibility(visible = serverConfig.errorMessage != null) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = StatusDisconnectedRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = serverConfig.errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF991B1B)
                        )
                    }
                }
            }

            // Offline Standalone compiler option
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Standalone Compiler Mode",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimarySlate
                    )
                    Text(
                        text = "Use internal obfuscator when Termux server is offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedSlate
                    )
                }
                Switch(
                    checked = serverConfig.isStandaloneSimulator,
                    onCheckedChange = onToggleSimulator,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = TealPrimary
                    )
                )
            }
        }
    }
}
