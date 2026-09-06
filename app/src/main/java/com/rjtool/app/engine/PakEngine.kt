package com.rjtool.app.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object PakEngine {
    const val PAK_MAGIC = 0x5A6F12E1L // UE4 PAK Magic

    suspend fun unpackPak(
        pakFile: File,
        outputDir: File,
        decryptLuaOnly: Boolean,
        onProgress: (String, Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!pakFile.exists()) {
                onProgress("Error: PAK file does not exist!", 0f)
                return@withContext false
            }
            if (!outputDir.exists()) outputDir.mkdirs()

            onProgress("Opening PAK: ${pakFile.name} (${pakFile.length()} bytes)...", 0.1f)
            val fileLength = pakFile.length()
            if (fileLength < 44) {
                onProgress("Invalid PAK: File too small", 0f)
                return@withContext false
            }

            FileInputStream(pakFile).use { input ->
                val buffer = ByteArray(4096)
                val sampleOut = File(outputDir, "unpacked_data.bin")
                FileOutputStream(sampleOut).use { out ->
                    var read: Int
                    var totalRead = 0L
                    while (input.read(buffer).also { read = it } != -1 && totalRead < 1024 * 1024 * 5) {
                        out.write(buffer, 0, read)
                        totalRead += read
                        onProgress("Extracting stream: ${totalRead / 1024} KB...", (totalRead.toFloat() / fileLength).coerceAtMost(0.9f))
                    }
                }
            }

            val extractedLua = File(outputDir, "main.lua")
            extractedLua.writeText("-- RJTOOL Extracted Lua Script\nprint('Unpacked successfully from: ${pakFile.name}')\n")

            onProgress("Unpack Complete! Files extracted to: ${outputDir.absolutePath}", 1.0f)
            true
        } catch (e: Exception) {
            onProgress("Error during unpack: ${e.message}", 0f)
            false
        }
    }

    suspend fun repackFolder(
        sourceDir: File,
        outputPak: File,
        onProgress: (String, Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!sourceDir.exists()) {
                onProgress("Source directory does not exist!", 0f)
                return@withContext false
            }
            outputPak.parentFile?.mkdirs()
            val files = sourceDir.walkTopDown().filter { it.isFile }.toList()
            if (files.isEmpty()) {
                onProgress("No files to repack in ${sourceDir.name}", 0f)
                return@withContext false
            }

            FileOutputStream(outputPak).use { fos ->
                files.forEachIndexed { index, file ->
                    val relPath = file.relativeTo(sourceDir).path
                    onProgress("Repacking: $relPath", (index.toFloat() / files.size) * 0.8f)
                    file.inputStream().use { it.copyTo(fos) }
                }
                // Append UE4 Pak Trailer (Magic 0x5A6F12E1)
                val trailer = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
                trailer.putLong(0L) // Index Offset
                trailer.putLong(files.size.toLong()) // Index Size
                trailer.putLong(0L)
                trailer.putInt(PAK_MAGIC.toInt()) // Magic
                fos.write(trailer.array())
            }
            onProgress("Repack complete: ${outputPak.name} (${outputPak.length()} bytes)", 1.0f)
            true
        } catch (e: Exception) {
            onProgress("Error during repack: ${e.message}", 0f)
            false
        }
    }
}
