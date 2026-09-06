package com.rjtool.app.engine

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class FloatMatch(val offset: Long, val originalValue: Float, val label: String)

object HexEngine {
    fun scanHeadshotAndFloats(file: File): List<FloatMatch> {
        val matches = mutableListOf<FloatMatch>()
        if (!file.exists()) return matches

        RandomAccessFile(file, "r").use { raf ->
            val buffer = ByteArray(minOf(file.length().toInt(), 1024 * 512))
            val read = raf.read(buffer)
            if (read > 4) {
                val byteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
                for (i in 0 until read - 4 step 4) {
                    val f = byteBuffer.getFloat(i)
                    if (f in 0.9f..5.1f && !f.isNaN() && !f.isInfinite()) {
                        val tag = when {
                            f in 1.9f..2.1f -> "Headshot Multiplier (2.0x)"
                            f in 0.95f..1.05f -> "Base Damage (1.0x)"
                            f in 1.45f..1.55f -> "Hit Multiplier (1.5x)"
                            else -> "Float Property"
                        }
                        matches.add(FloatMatch(i.toLong(), f, tag))
                        if (matches.size >= 8) break
                    }
                }
            }
        }
        return matches
    }

    fun writeFloatAtOffset(file: File, offset: Long, newValue: Float): Boolean {
        return try {
            RandomAccessFile(file, "rw").use { raf ->
                raf.seek(offset)
                val bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(newValue).array()
                raf.write(bytes)
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
