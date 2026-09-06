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
import com.rjtool.app.engine.FloatMatch
import com.rjtool.app.engine.HexEngine
import com.rjtool.app.ui.components.FilePickerDialog
import com.rjtool.app.ui.components.TopHeader
import com.rjtool.app.ui.theme.*
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
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var showPickerDialog by remember { mutableStateOf(false) }

    var currentOffset by remember { mutableStateOf(0L) }
    var rows by remember { mutableStateOf<List<HexRow>>(emptyList()) }
    var editOffsetStr by remember { mutableStateOf("") }
    var editByteHexStr by remember { mutableStateOf("") }

    var isHeadshotMode by remember { mutableStateOf(false) }
    var floatMatches by remember { mutableStateOf<List<FloatMatch>>(emptyList()) }
    var newFloatInput by remember { mutableStateOf("2.5") }

    val uexpFiles = remember {
        FileUtils.getFilesInFolder("EDITTED", listOf(".uexp", ".uasset")) +
        FileUtils.getFilesInFolder("PAK_UNPACK", listOf(".uexp", ".uasset")) +
        FileUtils.getFilesInFolder("RESULT_PAK", listOf(".uexp", ".uasset"))
    }

    fun loadHexPage(offset: Long) {
        val file = selectedFile ?: return
        scope.launch {
            withContext(Dispatchers.IO) {
                if (!file.exists()) return@withContext
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
                }
            }
        }
    }

    if (showPickerDialog) {
        FilePickerDialog(
            title = "Select .uexp / .uasset File",
            files = uexpFiles,
            onDismiss = { showPickerDialog = false },
            onFileSelected = {
                selectedFile = it
                loadHexPage(0L)
                floatMatches = HexEngine.scanHeadshotAndFloats(it)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        TopHeader()
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.clickable { navController.popBackStack() }.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBackIos, "Back", tint = AccentTeal, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back", color = AccentTeal, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("Hex & Asset Editor", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("File: ${selectedFile?.name ?: "Tap Choose to select .uexp"}", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Folder: ${selectedFile?.parentFile?.name ?: "None"}", fontSize = 12.sp, color = TextSecondary)
                }
                Button(
                    onClick = { showPickerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A34)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Choose", color = AccentTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { isHeadshotMode = !isHeadshotMode }
                ) {
                    Checkbox(
                        checked = isHeadshotMode,
                        onCheckedChange = { isHeadshotMode = it },
                        colors = CheckboxDefaults.colors(checkedColor = AccentTeal, uncheckedColor = TextSecondary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Headshot / Damage Multiplier Scan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                if (isHeadshotMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (floatMatches.isEmpty()) {
                        Text("No float patterns detected in current file.", fontSize = 12.sp, color = TextSecondary)
                    } else {
                        floatMatches.forEach { match ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(match.label, fontSize = 13.sp, color = AccentTeal, fontWeight = FontWeight.Bold)
                                    Text("Offset: 0x${match.offset.toString(16).uppercase()} · Val: ${match.originalValue}", fontSize = 11.sp, color = TextSecondary)
                                }
                                Button(
                                    onClick = {
                                        val f = newFloatInput.toFloatOrNull() ?: 2.5f
                                        selectedFile?.let { file ->
                                            HexEngine.writeFloatAtOffset(file, match.offset, f)
                                            Toast.makeText(context, "Patched to $f!", Toast.LENGTH_SHORT).show()
                                            loadHexPage(currentOffset)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Patch $newFloatInput", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                label = { Text("Byte (Hex)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val off = editOffsetStr.toLong(16)
                            val byteVal = editByteHexStr.toInt(16).toByte()
                            selectedFile?.let { file ->
                                withContext(Dispatchers.IO) {
                                    RandomAccessFile(file, "rw").use { raf ->
                                        raf.seek(off)
                                        raf.write(byteArrayOf(byteVal))
                                    }
                                }
                                Toast.makeText(context, "Byte saved to disk!", Toast.LENGTH_SHORT).show()
                                loadHexPage(currentOffset)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Edit failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
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
                            modifier = Modifier.width(75.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = { if (currentOffset >= 256) loadHexPage(currentOffset - 256) }) {
                Text("<< Prev")
            }
            Text(
                text = "Offset: 0x${currentOffset.toString(16).uppercase()}",
                fontSize = 12.sp,
                color = TextSecondary
            )
            OutlinedButton(onClick = { loadHexPage(currentOffset + 256) }) {
                Text("Next >>")
            }
        }
    }
}
