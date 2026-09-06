package com.rjtool.app.utils

import java.io.File

object FileUtils {
    fun ensureDirectory(path: String): Boolean {
        val dir = File(path)
        if (!dir.exists()) return dir.mkdirs()
        return true
    }

    fun getFileSize(path: String): Long {
        val file = File(path)
        return if (file.exists()) file.length() else 0
    }

    fun getFileSizeFormatted(path: String): String {
        val size = getFileSize(path)
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }

    fun listFiles(path: String): List<File> {
        val dir = File(path)
        return if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.toList() ?: emptyList()
        } else emptyList()
    }
}
