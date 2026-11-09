package com.example

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ConversionResult
import com.example.model.SavedBinaryRecord
import com.example.ui.components.*
import com.example.ui.theme.PyBinaryShieldTheme
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealLightContainer
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TextMutedSlate
import com.example.ui.theme.TextPrimarySlate
import com.example.ui.theme.TextSecondarySlate
import com.example.viewmodel.PyBinaryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PyBinaryShieldTheme {
                PyBinaryShieldApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PyBinaryShieldApp(viewModel: PyBinaryViewModel = viewModel()) {
    val context = LocalContext.current
    val serverConfig by viewModel.serverConfig.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val compilationState by viewModel.compilationState.collectAsState()
    val savedHistory by viewModel.savedHistory.collectAsState()
    val userNotification by viewModel.userNotification.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showCodeEditor by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Launcher for selecting .py files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                var fileName = "script.py"
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex) ?: "script.py"
                    }
                }
                val inputStream = context.contentResolver.openInputStream(it)
                val content = inputStream?.bufferedReader()?.use { reader -> reader.readText() } ?: ""
                viewModel.setSelectedFile(fileName, content, uri.toString())
            } catch (e: Exception) {
                // Fallback
                viewModel.setSelectedFile("custom_script.py", "# Custom Python Script\nprint('Loaded file')", uri.toString())
            }
        }
    }

    LaunchedEffect(userNotification) {
        userNotification?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Header: At the top, a clean white bar with "PyBinary Shield" text in teal/dark gray and a matching shield icon.
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_header_bar"),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = TealLightContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "Shield Icon",
                                        tint = TealPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "PyBinary Shield",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = TealDark,
                                    modifier = Modifier.testTag("header_title_text")
                                )
                                Text(
                                    text = "Python to Obfuscated Binary Shield",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMutedSlate
                                )
                            }
                        }

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (serverConfig.isConnected) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                        ) {
                            Text(
                                text = if (serverConfig.isConnected) "ONLINE" else "OFFLINE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (serverConfig.isConnected) Color(0xFF059669) else Color(0xFFDC2626),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Navigation Tabs
                    PrimaryTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = TealPrimary,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Shield Compiler", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                            icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Termux Server", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                            icon = { Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Binary Vault", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                            icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 1: MAIN CONVERTER SHIELD
                    item {
                        // Card 1: SERVER CONFIGURATION
                        ServerConfigCard(
                            serverConfig = serverConfig,
                            onSetApiUrl = { url -> viewModel.setApiUrlAndConnect(url) },
                            onTestConnection = { viewModel.testServerConnection() },
                            onToggleSimulator = { enabled -> viewModel.toggleStandaloneSimulator(enabled) }
                        )
                    }

                    item {
                        // Card 2: FILE UPLOAD
                        FileUploadCard(
                            selectedFile = selectedFile,
                            onPickFileClick = { filePickerLauncher.launch("*/*") },
                            onLoadSampleClick = { type -> viewModel.loadSampleScript(type) },
                            onOpenEditorClick = { showCodeEditor = true }
                        )
                    }

                    item {
                        // Main Action Button: "CONVERT TO SECURE BINARY"
                        Button(
                            onClick = { viewModel.startConversion() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("convert_to_secure_binary_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TealPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Convert Shield",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "CONVERT TO SECURE BINARY",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp
                                )
                            )
                        }
                    }

                    item {
                        // Part 3: PROCESSING & DOWNLOAD
                        ProcessingDownloadSection(
                            compilationState = compilationState,
                            onSaveBinaryClick = { result ->
                                viewModel.saveBinaryToDevice(context, result)
                            }
                        )
                    }

                    if (savedHistory.isNotEmpty()) {
                        item {
                            BinaryHistoryCard(
                                history = savedHistory,
                                onReDownload = { record ->
                                    val result = ConversionResult(
                                        binaryFileName = record.binaryFileName,
                                        binaryData = record.binaryData,
                                        originalSize = record.sizeBytes / 2,
                                        binarySize = record.sizeBytes,
                                        obfuscationEngine = record.obfuscationLevel
                                    )
                                    viewModel.saveBinaryToDevice(context, result)
                                }
                            )
                        }
                    }
                }

                1 -> {
                    // TAB 2: TERMUX BACKEND SERVER SETUP
                    item {
                        TermuxSetupCard(
                            serverPyCode = viewModel.getTermuxServerPyCode(),
                            onTestConnectionClick = { viewModel.testServerConnection() },
                            onCopySuccess = { msg ->
                                viewModel.clearNotification()
                                viewModel.saveBinaryToDevice(context, ConversionResult("test", byteArrayOf(), 0, 0)) // optional
                            }
                        )
                    }
                }

                2 -> {
                    // TAB 3: BINARY HISTORY VAULT
                    item {
                        if (savedHistory.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(32.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = TextMutedSlate,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No Converted Binaries Yet",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimarySlate
                                    )
                                    Text(
                                        text = "Upload a Python file and click 'CONVERT TO SECURE BINARY' to generate obfuscated binaries.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondarySlate
                                    )
                                }
                            }
                        } else {
                            BinaryHistoryCard(
                                history = savedHistory,
                                onReDownload = { record ->
                                    val result = ConversionResult(
                                        binaryFileName = record.binaryFileName,
                                        binaryData = record.binaryData,
                                        originalSize = record.sizeBytes / 2,
                                        binarySize = record.sizeBytes,
                                        obfuscationEngine = record.obfuscationLevel
                                    )
                                    viewModel.saveBinaryToDevice(context, result)
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Code Editor Modal
    if (showCodeEditor) {
        PythonEditorDialog(
            initialFilename = selectedFile?.filename ?: "my_script.py",
            initialContent = selectedFile?.content ?: "# Type or paste your Python source code here\nprint('Hello from PyBinary Shield!')",
            onDismiss = { showCodeEditor = false },
            onSave = { name, content ->
                viewModel.setSelectedFile(name, content)
                showCodeEditor = false
            }
        )
    }
}
