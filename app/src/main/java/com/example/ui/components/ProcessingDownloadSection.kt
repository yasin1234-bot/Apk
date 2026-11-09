package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CompilationState
import com.example.model.ConversionResult
import com.example.model.LogEntry
import com.example.model.LogType
import com.example.ui.theme.StatusConnectedGreen
import com.example.ui.theme.StatusConnectedGreenBg
import com.example.ui.theme.StatusDisconnectedRed
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TerminalDarkBg
import com.example.ui.theme.TerminalGreenText
import com.example.ui.theme.TextMutedSlate
import com.example.ui.theme.TextPrimarySlate
import com.example.ui.theme.TextSecondarySlate

@Composable
fun ProcessingDownloadSection(
    compilationState: CompilationState,
    onSaveBinaryClick: (ConversionResult) -> Unit,
    modifier: Modifier = Modifier
) {
    if (compilationState is CompilationState.Idle) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("processing_download_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            when (compilationState) {
                is CompilationState.InProgress -> {
                    InProgressView(
                        percentage = compilationState.progressPercentage,
                        currentStepText = compilationState.currentStepText,
                        logs = compilationState.logs
                    )
                }
                is CompilationState.Success -> {
                    SuccessView(
                        result = compilationState.result,
                        onSaveBinaryClick = onSaveBinaryClick
                    )
                }
                is CompilationState.Error -> {
                    ErrorView(
                        errorMessage = compilationState.errorMessage,
                        logs = compilationState.logs
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun InProgressView(
    percentage: Int,
    currentStepText: String,
    logs: List<LogEntry>
) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "progressAnimation"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Label: "COMPILATION IN PROGRESS ([X]%)" (green)
        Text(
            text = "COMPILATION IN PROGRESS (${percentage}%)",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = StatusConnectedGreen,
            modifier = Modifier.testTag("compilation_progress_label")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Bar: An animated teal bar showing progress
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .testTag("compilation_progress_bar"),
            color = TealPrimary,
            trackColor = Color(0xFFE2E8F0)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = currentStepText,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondarySlate
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Log Area: A text panel with gray English log entries
        LogTerminalPanel(logs = logs)
    }
}

@Composable
private fun SuccessView(
    result: ConversionResult,
    onSaveBinaryClick: (ConversionResult) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success Icon Graphic
        Surface(
            shape = CircleShape,
            color = StatusConnectedGreenBg,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = StatusConnectedGreen,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Success Text: "SUCCESS!" (green, large)
        Text(
            text = "SUCCESS!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            ),
            color = StatusConnectedGreen,
            modifier = Modifier.testTag("success_header_text")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Message: "File successfully secured.", "Server response: Binary generated."
        Text(
            text = "File successfully secured.",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimarySlate,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Server response: Binary generated.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondarySlate,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Details Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Binary File:", style = MaterialTheme.typography.labelMedium, color = TextMutedSlate)
                Text(result.binaryFileName, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimarySlate)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Original / Binary Size:", style = MaterialTheme.typography.labelMedium, color = TextMutedSlate)
                Text("${result.originalSize} B → ${result.binarySize} B", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimarySlate)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Obfuscation Engine:", style = MaterialTheme.typography.labelMedium, color = TextMutedSlate)
                Text(result.obfuscationEngine, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TealDark)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Download Button: A prominent green "SAVE SECURE BINARY" button with a download icon
        Button(
            onClick = { onSaveBinaryClick(result) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_secure_binary_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = StatusConnectedGreen,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Save Binary",
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "SAVE SECURE BINARY",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@Composable
private fun ErrorView(
    errorMessage: String,
    logs: List<LogEntry>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = StatusDisconnectedRed,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "COMPILATION FAILED",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = StatusDisconnectedRed
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimarySlate
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (logs.isNotEmpty()) {
            LogTerminalPanel(logs = logs)
        }
    }
}

@Composable
private fun LogTerminalPanel(logs: List<LogEntry>) {
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TerminalDarkBg)
            .padding(14.dp)
            .testTag("log_terminal_panel")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = TerminalGreenText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Build Log Output",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            Surface(
                shape = CircleShape,
                color = Color(0xFF334155),
                modifier = Modifier.size(8.dp)
            ) {}
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(logs) { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "${entry.timestamp} ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = TextMutedSlate
                    )
                    Text(
                        text = entry.message,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        ),
                        color = when (entry.type) {
                            LogType.SUCCESS -> TerminalGreenText
                            LogType.ERROR -> StatusDisconnectedRed
                            LogType.WARNING -> Color(0xFFFBBF24)
                            LogType.INFO -> Color(0xFFE2E8F0)
                        }
                    )
                }
            }
        }
    }
}
