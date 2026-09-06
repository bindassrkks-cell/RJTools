package com.rjtool.app.engine

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object PakEngine {
    fun ensurePythonStarted(context: Context) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context.applicationContext))
        }
    }

    suspend fun unpackPakWithPython(
        context: Context,
        pakFile: File,
        outputDir: File,
        decryptLuaOnly: Boolean,
        onProgress: (String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            ensurePythonStarted(context)
            val py = Python.getInstance()
            val pakModule = py.getModule("pak_core")
            onProgress("Executing Python Unpacker Engine...")
            val res = pakModule.callAttr("unpack_pak", pakFile.absolutePath, outputDir.absolutePath, decryptLuaOnly)
            onProgress(res.toString())
            true
        } catch (e: Exception) {
            onProgress("❌ Python execution error: ${e.message}")
            false
        }
    }

    suspend fun repackPakWithPython(
        context: Context,
        sourceDir: File,
        outputPak: File,
        matchIndex: Boolean,
        onProgress: (String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            ensurePythonStarted(context)
            val py = Python.getInstance()
            val pakModule = py.getModule("pak_core")
            onProgress("Executing Python Repack Engine...")
            val res = pakModule.callAttr("repack_pak", sourceDir.absolutePath, outputPak.absolutePath, matchIndex)
            onProgress(res.toString())
            true
        } catch (e: Exception) {
            onProgress("❌ Python execution error: ${e.message}")
            false
        }
    }

    suspend fun runFixMainActivityScript(
        context: Context,
        scriptFile: File,
        onProgress: (String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            ensurePythonStarted(context)
            val py = Python.getInstance()
            val pakModule = py.getModule("pak_core")
            onProgress("Injecting script: ${scriptFile.name}...")
            val res = pakModule.callAttr("run_external_fix", scriptFile.absolutePath)
            onProgress(res.toString())
            true
        } catch (e: Exception) {
            onProgress("❌ Script injection error: ${e.message}")
            false
        }
    }
}
