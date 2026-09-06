package com.rjtool.app.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile

object SizeFixerEngine {
    suspend fun fixFileSize(
        targetFile: File,
        targetSizeBytes: Long,
        onProgress: (String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!targetFile.exists()) {
                onProgress("File does not exist: ${targetFile.absolutePath}")
                return@withContext false
            }
            val oldSize = targetFile.length()
            RandomAccessFile(targetFile, "rw").use { raf ->
                raf.setLength(targetSizeBytes)
            }
            onProgress("Fixed: ${targetFile.name}\nOld: $oldSize B -> New: ${targetFile.length()} B")
            true
        } catch (e: Exception) {
            onProgress("Size fix failed: ${e.message}")
            false
        }
    }

    fun parseSizeToBytes(input: String): Long? {
        val trimmed = input.trim().uppercase()
        return try {
            when {
                trimmed.endsWith("MB") -> (trimmed.removeSuffix("MB").toDouble() * 1024 * 1024).toLong()
                trimmed.endsWith("M") -> (trimmed.removeSuffix("M").toDouble() * 1024 * 1024).toLong()
                trimmed.endsWith("KB") -> (trimmed.removeSuffix("KB").toDouble() * 1024).toLong()
                trimmed.endsWith("K") -> (trimmed.removeSuffix("K").toDouble() * 1024).toLong()
                trimmed.endsWith("B") -> trimmed.removeSuffix("B").toLong()
                else -> trimmed.toLong()
            }
        } catch (_: Exception) {
            null
        }
    }
}
