package com.rjtool.app.utils

import android.content.Context
import android.os.Environment
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
        FOLDERS.forEach { folderName ->
            val dir = File(ROOT_DIR, folderName)
            if (!dir.exists()) dir.mkdirs()
        }
        copyAssetIfNotExists(context, "index.csv")
        copyAssetIfNotExists(context, "index.csx")
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
