package com.rjtool.app.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    val ROOT_DIR = File(Environment.getExternalStorageDirectory(), "RJTOOL")
    val FOLDERS = listOf(
        "EDITTED",
        "LUA_ORIGINAL",
        "LUA_UNPACK",
        "PAK_ORIGINAL",
        "PAK_UNPACK",
        "RESULT_PAK"
    )

    fun initRJToolWorkspace(context: Context) {
        if (!ROOT_DIR.exists()) ROOT_DIR.mkdirs()
        FOLDERS.forEach { name ->
            val dir = File(ROOT_DIR, name)
            if (!dir.exists()) dir.mkdirs()
        }
        copyAssetIfNotExists(context, "index.csv")
        copyAssetIfNotExists(context, "index.csx")
    }

    fun getFilesInFolder(subFolderName: String, extensions: List<String>? = null): List<File> {
        val folder = File(ROOT_DIR, subFolderName)
        if (!folder.exists() || !folder.isDirectory) return emptyList()
        return folder.listFiles()?.filter { file ->
            file.isFile && (extensions == null || extensions.any { ext -> file.name.endsWith(ext, ignoreCase = true) })
        }?.sortedBy { it.name } ?: emptyList()
    }

    fun resolveUriToFile(context: Context, uri: Uri, targetSubFolder: String): File? {
        try {
            val path = uri.path ?: return null
            if (path.contains("/document/primary:")) {
                val relative = path.substringAfter("/document/primary:")
                val actualFile = File(Environment.getExternalStorageDirectory(), relative)
                if (actualFile.exists()) return actualFile
            }
            var name = "selected_file"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    name = cursor.getString(nameIndex)
                }
            }
            val destFile = File(File(ROOT_DIR, targetSubFolder), name)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            return destFile
        } catch (e: Exception) {
            return null
        }
    }

    private fun copyAssetIfNotExists(context: Context, fileName: String) {
        val target = File(ROOT_DIR, fileName)
        if (!target.exists()) {
            try {
                context.assets.open(fileName).use { input ->
                    FileOutputStream(target).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (_: Exception) {}
        }
    }
}
