package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PythonFileItem
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealLightContainer
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TextMutedSlate
import com.example.ui.theme.TextPrimarySlate
import com.example.ui.theme.TextSecondarySlate

@Composable
fun FileUploadCard(
    selectedFile: PythonFileItem?,
    onPickFileClick: () -> Unit,
    onLoadSampleClick: (String) -> Unit,
    onOpenEditorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCodePreview by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("file_upload_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // Label: "FILE UPLOAD" (gray, uppercase)
            Text(
                text = "FILE UPLOAD",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextMutedSlate,
                modifier = Modifier.testTag("file_upload_title")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Central Upload Drag/Drop Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(
                        width = 1.5.dp,
                        color = if (selectedFile != null) TealPrimary else Color(0xFFCBD5E1),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onPickFileClick() }
                    .padding(vertical = 24.dp, horizontal = 16.dp)
                    .testTag("upload_drop_zone"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Icon: File folder and cloud upload icons in teal
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TealLightContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "Folder",
                                    tint = TealPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = TealLightContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Cloud Upload",
                                    tint = TealPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Button/Label: "CHOOSE PYTHON (.py) FILE"
                    Button(
                        onClick = onPickFileClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("choose_file_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CHOOSE PYTHON (.py) FILE",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Label: "SELECTED: [filename]"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedFile != null) Color(0xFFF0FDF4) else Color(0xFFF1F5F9),
                        border = if (selectedFile != null) BorderStroke(1.dp, Color(0xFF86EFAC)) else null,
                        modifier = Modifier.testTag("selected_file_label")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = if (selectedFile != null) Color(0xFF16A34A) else TextMutedSlate,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedFile != null) "SELECTED: ${selectedFile.filename}" else "SELECTED: [No file chosen]",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedFile != null) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (selectedFile != null) Color(0xFF15803D) else TextSecondarySlate,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Samples & Editor Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Load Samples:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedSlate
                )

                TextButton(
                    onClick = onOpenEditorClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Edit Code",
                        tint = TealPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Edit Code",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TealPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("Auth Vault", "default"),
                    Pair("AES Cipher", "cipher"),
                    Pair("Prime Math", "math")
                ).forEach { (label, type) ->
                    OutlinedButton(
                        onClick = {
                            if (type == "default") onPickFileClick() // Or reload default
                            else onLoadSampleClick(type)
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondarySlate,
                            maxLines = 1
                        )
                    }
                }
            }

            // Code Preview Collapsible Toggle
            if (selectedFile != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCodePreview = !showCodePreview },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Source Code Preview (${selectedFile.sizeBytes} bytes)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextSecondarySlate
                    )
                    Text(
                        text = if (showCodePreview) "Hide" else "View Code",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TealPrimary
                    )
                }

                AnimatedVisibility(visible = showCodePreview) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = selectedFile.content,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                ),
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }
        }
    }
}
