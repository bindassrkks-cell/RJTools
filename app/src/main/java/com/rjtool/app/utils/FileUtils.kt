package com.rjtool.app.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    val ROOT_DIR = File(Environment.getExternalStorageDirectory(), "RJTOOL")

    fun initRJToolWorkspace(context: Context) {
        if (!ROOT_DIR.exists()) ROOT_DIR.mkdirs()
        val folders = listOf(
            "EDITTED",
            "LUA_ORIGINAL",
            "LUA_UNPACK",
            "PAK_ORIGINAL",
            "PAK_UNPACK",
            "RESULT_PAK"
        )
        folders.forEach { folderName ->
            val dir = File(ROOT_DIR, folderName)
            if (!dir.exists()) dir.mkdirs()
        }

        val targetCsv = File(ROOT_DIR, "index.csv")
        if (!targetCsv.exists()) {
            try {
                context.assets.open("index.csv").use { input ->
                    FileOutputStream(targetCsv).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (_: Exception) {}
        }
    }
}
