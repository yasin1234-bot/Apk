package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConversionResult
import com.example.model.SavedBinaryRecord
import com.example.ui.theme.StatusConnectedGreen
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TextMutedSlate
import com.example.ui.theme.TextPrimarySlate
import com.example.ui.theme.TextSecondarySlate

@Composable
fun BinaryHistoryCard(
    history: List<SavedBinaryRecord>,
    onReDownload: (SavedBinaryRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("binary_history_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    tint = TealPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "GENERATED BINARY HISTORY",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextMutedSlate
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                history.take(5).forEach { record ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE0F2F1),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = record.binaryFileName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimarySlate
                                )
                                Text(
                                    text = "${record.timestampFormatted} • ${record.sizeBytes} B",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMutedSlate
                                )
                            }
                        }

                        IconButton(
                            onClick = { onReDownload(record) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Binary",
                                tint = StatusConnectedGreen
                            )
                        }
                    }
                }
            }
        }
    }
}
