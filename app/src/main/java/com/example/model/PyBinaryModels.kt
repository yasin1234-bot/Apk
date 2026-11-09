package com.example.model

data class ServerConfig(
    val apiUrl: String = "http://127.0.0.1:5000",
    val isConnected: Boolean = false,
    val isTestingConnection: Boolean = false,
    val lastCheckTime: Long? = null,
    val errorMessage: String? = null,
    val serverStatusMessage: String? = null,
    val isStandaloneSimulator: Boolean = false
)

data class PythonFileItem(
    val filename: String,
    val content: String,
    val sizeBytes: Long,
    val uriString: String? = null,
    val isPreset: Boolean = false
)

enum class LogType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

data class LogEntry(
    val timestamp: String,
    val message: String,
    val type: LogType = LogType.INFO
)

data class ConversionResult(
    val binaryFileName: String,
    val binaryData: ByteArray,
    val originalSize: Long,
    val binarySize: Long,
    val obfuscationEngine: String = "PyArmor 8.5 + PyInstaller 6.0",
    val serverMessage: String = "Binary generated successfully.",
    val compilationTimeSeconds: Double = 6.8
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ConversionResult
        return binaryFileName == other.binaryFileName && binaryData.contentEquals(other.binaryData)
    }

    override fun hashCode(): Int {
        var result = binaryFileName.hashCode()
        result = 31 * result + binaryData.contentHashCode()
        return result
    }
}

sealed class CompilationState {
    object Idle : CompilationState()
    data class InProgress(
        val progressPercentage: Int,
        val currentStepText: String,
        val logs: List<LogEntry>
    ) : CompilationState()
    data class Success(
        val result: ConversionResult
    ) : CompilationState()
    data class Error(
        val errorMessage: String,
        val logs: List<LogEntry>
    ) : CompilationState()
}

data class SavedBinaryRecord(
    val id: String,
    val originalFileName: String,
    val binaryFileName: String,
    val timestampFormatted: String,
    val sizeBytes: Long,
    val obfuscationLevel: String,
    val binaryData: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SavedBinaryRecord
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
