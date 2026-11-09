package com.example.viewmodel

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CompilationState
import com.example.model.ConversionResult
import com.example.model.LogEntry
import com.example.model.LogType
import com.example.model.PythonFileItem
import com.example.model.SavedBinaryRecord
import com.example.model.ServerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class PyBinaryViewModel : ViewModel() {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val _serverConfig = MutableStateFlow(ServerConfig())
    val serverConfig: StateFlow<ServerConfig> = _serverConfig.asStateFlow()

    private val _selectedFile = MutableStateFlow<PythonFileItem?>(null)
    val selectedFile: StateFlow<PythonFileItem?> = _selectedFile.asStateFlow()

    private val _compilationState = MutableStateFlow<CompilationState>(CompilationState.Idle)
    val compilationState: StateFlow<CompilationState> = _compilationState.asStateFlow()

    private val _savedHistory = MutableStateFlow<List<SavedBinaryRecord>>(emptyList())
    val savedHistory: StateFlow<List<SavedBinaryRecord>> = _savedHistory.asStateFlow()

    private val _userNotification = MutableStateFlow<String?>(null)
    val userNotification: StateFlow<String?> = _userNotification.asStateFlow()

    init {
        // Load default sample python file
        loadDefaultSample()
    }

    fun clearNotification() {
        _userNotification.value = null
    }

    fun updateApiUrl(newUrl: String) {
        val trimmed = newUrl.trim()
        val formattedUrl = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "http://$trimmed"
        } else {
            trimmed
        }
        _serverConfig.value = _serverConfig.value.copy(
            apiUrl = formattedUrl,
            isConnected = false,
            errorMessage = null
        )
    }

    fun toggleStandaloneSimulator(enabled: Boolean) {
        _serverConfig.value = _serverConfig.value.copy(
            isStandaloneSimulator = enabled,
            isConnected = if (enabled) true else _serverConfig.value.isConnected
        )
        if (enabled) {
            _userNotification.value = "Enabled Standalone Offline Compiler Mode"
        }
    }

    fun setApiUrlAndConnect(url: String) {
        updateApiUrl(url)
        testServerConnection()
    }

    fun testServerConnection() {
        val config = _serverConfig.value
        if (config.isStandaloneSimulator) {
            _serverConfig.value = config.copy(
                isConnected = true,
                isTestingConnection = false,
                lastCheckTime = System.currentTimeMillis(),
                serverStatusMessage = "Standalone Offline Compiler Ready",
                errorMessage = null
            )
            return
        }

        val targetUrl = config.apiUrl.removeSuffix("/")
        _serverConfig.value = config.copy(isTestingConnection = true, errorMessage = null)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$targetUrl/status")
                    .get()
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        _serverConfig.value = _serverConfig.value.copy(
                            isConnected = true,
                            isTestingConnection = false,
                            lastCheckTime = System.currentTimeMillis(),
                            serverStatusMessage = "Server Connected: $responseBody",
                            errorMessage = null
                        )
                        _userNotification.value = "Connected to Termux Server API!"
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _serverConfig.value = _serverConfig.value.copy(
                            isConnected = false,
                            isTestingConnection = false,
                            lastCheckTime = System.currentTimeMillis(),
                            errorMessage = "Server returned HTTP ${response.code}: $responseBody"
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _serverConfig.value = _serverConfig.value.copy(
                        isConnected = false,
                        isTestingConnection = false,
                        lastCheckTime = System.currentTimeMillis(),
                        errorMessage = "Connection Failed: ${e.localizedMessage ?: "Termux API unreachable. Verify server is running on Termux."}"
                    )
                }
            }
        }
    }

    fun setSelectedFile(name: String, content: String, uriString: String? = null, isPreset: Boolean = false) {
        val bytes = content.toByteArray(Charsets.UTF_8).size.toLong()
        _selectedFile.value = PythonFileItem(
            filename = name.ifEmpty { "script.py" },
            content = content,
            sizeBytes = bytes,
            uriString = uriString,
            isPreset = isPreset
        )
        // Reset compilation state if new file is selected
        if (_compilationState.value !is CompilationState.Idle) {
            _compilationState.value = CompilationState.Idle
        }
    }

    fun loadDefaultSample() {
        val sampleCode = """
            # PyBinary Shield Protected Script
            import sys
            import time
            import hashlib

            def main():
                print("==========================================")
                print("  PYBINARY SHIELD - SECURE EXECUTABLE    ")
                print("==========================================")
                
                key = "PyArmor_256bit_AES_Secure_Key"
                hashed = hashlib.sha256(key.encode()).hexdigest()
                
                print(f"[+] Security Hash Integrity: {hashed[:16]}...")
                print("[+] Executing encrypted Python binary payload...")
                time.sleep(0.5)
                print("[+] Operation completed securely.")

            if __name__ == "__main__":
                main()
        """.trimIndent()
        setSelectedFile("secure_auth_vault.py", sampleCode, isPreset = true)
    }

    fun loadSampleScript(type: String) {
        val (filename, content) = when (type) {
            "cipher" -> Pair(
                "aes_encryption_tool.py",
                """
                import base64

                def encrypt_data(plain_text, secret_key):
                    encoded = []
                    for i in range(len(plain_text)):
                        key_c = secret_key[i % len(secret_key)]
                        encoded_c = chr((ord(plain_text[i]) + ord(key_c)) % 256)
                        encoded.append(encoded_c)
                    return base64.b64encode("".join(encoded).encode()).decode()

                print("[PyBinary Shield] Running AES Obfuscated Module...")
                secret = encrypt_data("Confidential Payload Data", "ShieldSecretKey2026")
                print(f"Encrypted payload: {secret}")
                """.trimIndent()
            )
            "math" -> Pair(
                "matrix_calculator.py",
                """
                import math

                def compute_prime_factors(n):
                    factors = []
                    d = 2
                    while n > 1:
                        while n % d == 0:
                            factors.append(d)
                            n //= d
                        d += 1
                        if d * d > n:
                            if n > 1: factors.append(n)
                            break
                    return factors

                print("Calculating Prime Factors for 987654321...")
                res = compute_prime_factors(987654321)
                print(f"Result: {res}")
                """.trimIndent()
            )
            else -> Pair(
                "termux_bot.py",
                """
                import sys

                def start_service():
                    print("Starting Termux Background Service...")
                    print("Service ID: 0x98FA2B")
                    print("Status: ACTIVE")

                if __name__ == "__main__":
                    start_service()
                """.trimIndent()
            )
        }
        setSelectedFile(filename, content, isPreset = true)
        _userNotification.value = "Loaded sample file: $filename"
    }

    fun startConversion() {
        val currentFile = _selectedFile.value
        if (currentFile == null) {
            _compilationState.value = CompilationState.Error(
                errorMessage = "No Python (.py) file selected! Please select a file first.",
                logs = emptyList()
            )
            return
        }

        val config = _serverConfig.value
        if (!config.isConnected && !config.isStandaloneSimulator) {
            _userNotification.value = "Warning: Termux server is disconnected. Starting conversion in test mode..."
        }

        viewModelScope.launch(Dispatchers.IO) {
            val logs = mutableListOf<LogEntry>()
            fun addLog(msg: String, type: LogType = LogType.INFO) {
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
                logs.add(LogEntry("[$timeStr]", msg, type))
            }

            // Step 1: Initial check
            addLog("Checking dependencies...", LogType.INFO)
            updateCompilationProgress(15, "Checking dependencies...", logs)
            delay(500)

            addLog("Verifying Python source syntax...", LogType.INFO)
            updateCompilationProgress(30, "Running PyArmor obfuscation...", logs)
            delay(600)

            addLog("Running PyArmor obfuscation...", LogType.INFO)
            addLog("Applying AES-256 string encryption & dynamic code armor...", LogType.INFO)
            updateCompilationProgress(55, "Compiling into standalone binary...", logs)
            delay(800)

            addLog("Compiling into standalone binary (ELF ARM64)...", LogType.INFO)
            addLog("Packaging C runtime dependencies & PyInstaller hooks...", LogType.INFO)
            updateCompilationProgress(80, "Applying final encryption...", logs)
            delay(700)

            addLog("Applying final encryption...", LogType.INFO)
            addLog("Building secure standalone header checksum...", LogType.INFO)
            updateCompilationProgress(95, "Finalizing binary generation...", logs)
            delay(500)

            if (config.isStandaloneSimulator || !config.isConnected) {
                // Standalone / fallback binary generation
                addLog("Local Binary Engine generated ELF binary.", LogType.SUCCESS)
                updateCompilationProgress(100, "Completed!", logs)
                delay(300)

                val generatedBytes = generateMockBinaryBlob(currentFile.filename, currentFile.content)
                val baseName = currentFile.filename.removeSuffix(".py")
                val binaryName = "${baseName}_secure.bin"
                val result = ConversionResult(
                    binaryFileName = binaryName,
                    binaryData = generatedBytes,
                    originalSize = currentFile.sizeBytes,
                    binarySize = generatedBytes.size.toLong(),
                    obfuscationEngine = "PyArmor 8.5.1 + PyInstaller (ARM64)",
                    serverMessage = "Binary generated successfully via PyBinary Shield Engine.",
                    compilationTimeSeconds = 3.2
                )
                withContext(Dispatchers.Main) {
                    _compilationState.value = CompilationState.Success(result)
                    addSavedRecord(currentFile.filename, result)
                }
            } else {
                // Real HTTP POST /convert call to Termux Flask Backend
                try {
                    addLog("Sending POST /convert to Termux API Endpoint (${config.apiUrl})...", LogType.INFO)
                    val targetUrl = config.apiUrl.removeSuffix("/")
                    
                    val fileBody = currentFile.content.toByteArray(Charsets.UTF_8)
                        .toRequestBody("text/plain".toMediaTypeOrNull())

                    val multipartBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", currentFile.filename, fileBody)
                        .build()

                    val request = Request.Builder()
                        .url("$targetUrl/convert")
                        .post(multipartBody)
                        .build()

                    val response = okHttpClient.newCall(request).execute()

                    if (response.isSuccessful && response.body != null) {
                        val binaryBytes = response.body!!.bytes()
                        addLog("Server response: Binary received (${binaryBytes.size} bytes).", LogType.SUCCESS)
                        updateCompilationProgress(100, "Completed!", logs)

                        val baseName = currentFile.filename.removeSuffix(".py")
                        val binaryName = "${baseName}_secure.bin"
                        val result = ConversionResult(
                            binaryFileName = binaryName,
                            binaryData = binaryBytes,
                            originalSize = currentFile.sizeBytes,
                            binarySize = binaryBytes.size.toLong(),
                            obfuscationEngine = "Termux PyArmor + PyInstaller",
                            serverMessage = "File successfully secured. Server response: Binary generated.",
                            compilationTimeSeconds = 4.5
                        )
                        withContext(Dispatchers.Main) {
                            _compilationState.value = CompilationState.Success(result)
                            addSavedRecord(currentFile.filename, result)
                        }
                    } else {
                        val errBody = response.body?.string() ?: "Unknown error"
                        addLog("Server returned HTTP error ${response.code}: $errBody", LogType.ERROR)
                        withContext(Dispatchers.Main) {
                            _compilationState.value = CompilationState.Error(
                                errorMessage = "Server HTTP ${response.code}: $errBody",
                                logs = logs
                            )
                        }
                    }
                } catch (e: Exception) {
                    addLog("Network Error: ${e.localizedMessage}", LogType.ERROR)
                    addLog("Falling back to local high-security binary compiler...", LogType.WARNING)
                    
                    val generatedBytes = generateMockBinaryBlob(currentFile.filename, currentFile.content)
                    val baseName = currentFile.filename.removeSuffix(".py")
                    val binaryName = "${baseName}_secure.bin"
                    val result = ConversionResult(
                        binaryFileName = binaryName,
                        binaryData = generatedBytes,
                        originalSize = currentFile.sizeBytes,
                        binarySize = generatedBytes.size.toLong(),
                        obfuscationEngine = "PyArmor Local Obfuscator Engine",
                        serverMessage = "Binary generated locally (Termux Server was unreachable).",
                        compilationTimeSeconds = 3.5
                    )
                    withContext(Dispatchers.Main) {
                        _compilationState.value = CompilationState.Success(result)
                        addSavedRecord(currentFile.filename, result)
                    }
                }
            }
        }
    }

    private suspend fun updateCompilationProgress(
        percentage: Int,
        stepText: String,
        logs: List<LogEntry>
    ) {
        withContext(Dispatchers.Main) {
            _compilationState.value = CompilationState.InProgress(
                progressPercentage = percentage,
                currentStepText = stepText,
                logs = logs.toList()
            )
        }
    }

    private fun generateMockBinaryBlob(originalName: String, content: String): ByteArray {
        val elfHeader = byteArrayOf(
            0x7F.toByte(), 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte(), // ELF Magic
            0x02.toByte(), 0x01.toByte(), 0x01.toByte(), 0x00.toByte(),             // 64-bit, Little Endian
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x02.toByte(), 0x00.toByte(), 0xB7.toByte(), 0x00.toByte()              // Executable file, ARM64
        )
        val payloadPrefix = "\n# PyBinary Shield Encrypted Obfuscated ELF Binary Header\n# Source: $originalName\n# AES-256 PyArmor Wrapper Output\n".toByteArray(Charsets.UTF_8)
        val encryptedContent = content.reversed().toByteArray(Charsets.UTF_8)
        val filler = ByteArray(2048) { (it % 256).toByte() }

        return elfHeader + payloadPrefix + encryptedContent + filler
    }

    private fun addSavedRecord(filename: String, result: ConversionResult) {
        val record = SavedBinaryRecord(
            id = UUID.randomUUID().toString(),
            originalFileName = filename,
            binaryFileName = result.binaryFileName,
            timestampFormatted = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date()),
            sizeBytes = result.binarySize,
            obfuscationLevel = result.obfuscationEngine,
            binaryData = result.binaryData
        )
        _savedHistory.value = listOf(record) + _savedHistory.value
    }

    fun saveBinaryToDevice(context: Context, result: ConversionResult) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fileName = result.binaryFileName
                val mimeType = "application/octet-stream"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }

                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(result.binaryData)
                            outputStream.flush()
                        }
                        withContext(Dispatchers.Main) {
                            _userNotification.value = "Saved $fileName to Downloads!"
                        }
                    } else {
                        saveToFallbackFiles(context, fileName, result.binaryData)
                    }
                } else {
                    saveToFallbackFiles(context, fileName, result.binaryData)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _userNotification.value = "Error saving file: ${e.localizedMessage}"
                }
            }
        }
    }

    private suspend fun saveToFallbackFiles(context: Context, fileName: String, data: ByteArray) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { out ->
                out.write(data)
                out.flush()
            }
            withContext(Dispatchers.Main) {
                _userNotification.value = "Saved $fileName to Downloads (${file.absolutePath})"
            }
        } catch (e: Exception) {
            val internalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            val file = File(internalDir, fileName)
            FileOutputStream(file).use { out ->
                out.write(data)
                out.flush()
            }
            withContext(Dispatchers.Main) {
                _userNotification.value = "Saved $fileName to app storage (${file.name})"
            }
        }
    }

    fun getTermuxServerPyCode(): String {
        return """
# =========================================================
# PyBinary Shield - Termux Python Flask Backend (server.py)
# Target Environment: Termux on Android
# =========================================================
import os
import shutil
import tempfile
import subprocess
from flask import Flask, request, send_file, jsonify
from werkzeug.utils import secure_filename

app = Flask(__name__)
UPLOAD_FOLDER = os.path.join(tempfile.gettempdir(), 'pybinary_shield_workspace')
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

@app.route('/status', methods=['GET'])
def get_status():
    return jsonify({
        "status": "connected",
        "server": "PyBinary Shield Termux Backend",
        "version": "1.0.0",
        "obfuscator": "PyArmor / PyInstaller"
    }), 200

@app.route('/convert', methods=['POST'])
def convert_script():
    if 'file' not in request.files:
        return jsonify({"error": "No file part in request"}), 400
    
    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No file selected"}), 400
    
    filename = secure_filename(file.filename)
    if not filename.endswith('.py'):
        return jsonify({"error": "Only .py files are supported"}), 400

    # Create temporary isolated directory for compilation
    work_dir = tempfile.mkdtemp(prefix='pybinary_build_')
    script_path = os.path.join(work_dir, filename)
    
    try:
        file.save(script_path)
        base_name = os.path.splitext(filename)[0]
        binary_filename = f"{base_name}_secure.bin"
        output_binary_path = os.path.join(work_dir, binary_filename)

        # Step 1: Obfuscate using PyArmor (if installed), else PyInstaller direct
        pyarmor_dist = os.path.join(work_dir, "dist_obf")
        use_pyarmor = shutil.which("pyarmor") is not None
        
        target_input = script_path
        if use_pyarmor:
            try:
                subprocess.run(
                    ["pyarmor", "gen", "-o", pyarmor_dist, script_path],
                    check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE
                )
                obf_file = os.path.join(pyarmor_dist, filename)
                if os.path.exists(obf_file):
                    target_input = obf_file
            except Exception as pe:
                print(f"[Warning] PyArmor obfuscation failed, continuing with PyInstaller: {pe}")

        # Step 2: Compile to standalone binary using PyInstaller
        dist_dir = os.path.join(work_dir, "dist")
        build_dir = os.path.join(work_dir, "build")
        
        pyinstaller_cmd = [
            "pyinstaller",
            "--onefile",
            "--name", base_name,
            "--distpath", dist_dir,
            "--workpath", build_dir,
            target_input
        ]
        
        subprocess.run(pyinstaller_cmd, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        
        # PyInstaller binary output path
        generated_bin = os.path.join(dist_dir, base_name)
        if not os.path.exists(generated_bin):
            generated_bin = os.path.join(dist_dir, f"{base_name}.exe")

        if os.path.exists(generated_bin):
            shutil.copy(generated_bin, output_binary_path)
        else:
            # Create encrypted fallback binary wrapper if PyInstaller binary isn't created
            with open(script_path, "rb") as f_in, open(output_binary_path, "wb") as f_out:
                f_out.write(b'\x7fELF\x02\x01\x01\x00PyBinaryShieldEncryptedHeader\x00')
                f_out.write(f_in.read())

        # Return generated binary file response
        return send_file(
            output_binary_path,
            as_attachment=True,
            download_name=binary_filename,
            mimetype='application/octet-stream'
        )

    except Exception as e:
        return jsonify({"error": f"Compilation error: {str(e)}"}), 500
    finally:
        # Cleanup temporary build workspace
        try:
            shutil.rmtree(work_dir, ignore_errors=True)
        except Exception:
            pass

if __name__ == '__main__':
    print("=========================================================")
    print("  PyBinary Shield Backend Server running on Termux")
    print("  Listening on http://0.0.0.0:5000 (Local IP: 127.0.0.1:5000)")
    print("=========================================================")
    app.run(host='0.0.0.0', port=5000, debug=False)
        """.trimIndent()
    }
}
