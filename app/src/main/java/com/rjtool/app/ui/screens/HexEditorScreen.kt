package com.rjtool.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rjtool.app.ui.components.TopHeader
import com.rjtool.app.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile

data class HexRow(val offset: Long, val bytes: List<Byte>, val ascii: String)

@Composable
fun HexEditorScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var filenameInput by remember { mutableStateOf("index.csv") }
    var currentOffset by remember { mutableStateOf(0L) }
    var rows by remember { mutableStateOf<List<HexRow>>(emptyList()) }
    var editOffsetStr by remember { mutableStateOf("") }
    var editByteHexStr by remember { mutableStateOf("") }
    var logMessage by remember { mutableStateOf("") }

    fun loadPage(offset: Long) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val file = File(FileUtils.ROOT_DIR, filenameInput)
                if (!file.exists()) {
                    logMessage = "File not found: ${file.name}\n"
                    return@withContext
                }
                RandomAccessFile(file, "r").use { raf ->
                    raf.seek(offset)
                    val buffer = ByteArray(256)
                    val read = raf.read(buffer)
                    val newRows = mutableListOf<HexRow>()
                    if (read > 0) {
                        for (i in 0 until read step 16) {
                            val chunk = buffer.slice(i until minOf(i + 16, read))
                            val ascii = chunk.map { b -> if (b in 32..126) b.toInt().toChar() else '.' }.joinToString("")
                            newRows.add(HexRow(offset + i, chunk, ascii))
                        }
                    }
                    rows = newRows
                    currentOffset = offset
                    logMessage = "Loaded offset 0x${offset.toString(16).uppercase()} (${file.length()} bytes total)\n"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        TopHeader()
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.clickable { navController.popBackStack() }.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBackIos, "Back", tint = Color(0xFF00796B), modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back", color = Color(0xFF00796B), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("Hex Editor", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = filenameInput,
                onValueChange = { filenameInput = it },
                label = { Text("File in RJTOOL") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = { loadPage(0L) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
            ) {
                Text("Load")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Byte Edit Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = editOffsetStr,
                onValueChange = { editOffsetStr = it },
                label = { Text("Offset (Hex)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = editByteHexStr,
                onValueChange = { editByteHexStr = it },
                label = { Text("Byte (e.g. FF)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val off = editOffsetStr.toLong(16)
                            val byteVal = editByteHexStr.toInt(16).toByte()
                            val file = File(FileUtils.ROOT_DIR, filenameInput)
                            withContext(Dispatchers.IO) {
                                RandomAccessFile(file, "rw").use { raf ->
                                    raf.seek(off)
                                    raf.write(byteArrayOf(byteVal))
                                }
                            }
                            Toast.makeText(context, "Byte saved to disk!", Toast.LENGTH_SHORT).show()
                            loadPage(currentOffset)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Edit failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hex Table Container
        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(10.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                items(rows) { row ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text(
                            text = "0x" + row.offset.toString(16).padStart(8, '0').uppercase(),
                            color = Color(0xFFFFB300),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.width(85.dp)
                        )
                        Text(
                            text = row.bytes.joinToString(" ") { it.toUByte().toString(16).padStart(2, '0').uppercase() },
                            color = Color(0xFF00FF66),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = row.ascii,
                            color = Color(0xFF00E5FF),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.width(80.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = { if (currentOffset >= 256) loadPage(currentOffset - 256) }) {
                Text("<< Prev")
            }
            Text(
                text = "Offset: 0x${currentOffset.toString(16).uppercase()}",
                modifier = Modifier.align(Alignment.CenterVertically),
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
            OutlinedButton(onClick = { loadPage(currentOffset + 256) }) {
                Text("Next >>")
            }
        }
    }
}
