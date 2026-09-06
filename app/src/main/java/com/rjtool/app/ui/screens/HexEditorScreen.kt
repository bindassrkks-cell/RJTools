package com.rjtool.app.ui.screens
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class HexByte(val offset: Int, val value: Byte, val ascii: Char)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HexEditorScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedFile by remember { mutableStateOf<String?>(null) }
    var hexData by remember { mutableStateOf<List<HexByte>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }
    var logMessage by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedOffset by remember { mutableStateOf<Int?>(null) }
    var editValue by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedFile = it.path ?: ""
            loadHexData(selectedFile!!)
        }
    }

    fun loadHexData(path: String) {
        scope.launch {
            isProcessing = true
            logMessage = "Loading file...\n"
            withContext(Dispatchers.IO) {
                try {
                    val file = File(path)
                    if (!file.exists()) {
                        logMessage += "❌ File does not exist\n"
                        isProcessing = false
                        return@withContext
                    }
                    val bytes = file.readBytes()
                    hexData = bytes.mapIndexed { index, byte ->
                        HexByte(offset = index, value = byte, ascii = if (byte in 32..126) byte.toChar() else '.')
                    }
                    logMessage += "✅ Loaded ${bytes.size} bytes\n"
                } catch (e: Exception) {
                    logMessage += "❌ Error: ${e.message}\n"
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Text(text = "Hex Editor", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { filePickerLauncher.launch("*/*") }) { Icon(Icons.Default.Edit, contentDescription = "Open File") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336).copy(alpha = 0.15f))) {
            Text(text = "🔢 Hex Editor - Edit .uexp files", modifier = Modifier.padding(12.dp), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, label = { Text("Search Hex (e.g., 01 02 03)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = editValue, onValueChange = { editValue = it }, label = { Text("Edit at offset (hex)") }, modifier = Modifier.weight(1f), singleLine = true)
            Button(
                onClick = {
                    if (selectedOffset != null && editValue.isNotEmpty()) {
                        try {
                            val offset = selectedOffset!!
                            val newValue = editValue.toInt(16).toByte()
                            val updatedList = hexData.toMutableList()
                            if (offset < updatedList.size) {
                                updatedList[offset] = HexByte(offset, newValue, newValue.toChar())
                                hexData = updatedList
                                val file = File(selectedFile!!)
                                val bytes = hexData.map { it.value }.toByteArray()
                                file.writeBytes(bytes)
                                logMessage += "✅ Updated byte at offset 0x${offset.toString(16)}\n"
                                Toast.makeText(context, "Byte updated!", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            logMessage += "❌ Error: ${e.message}\n"
                        }
                    }
                },
                modifier = Modifier.weight(0.5f)
            ) { Text("Apply") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (isProcessing) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
        if (hexData.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    items(hexData.chunked(16)) { chunk ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "0x${chunk.firstOrNull()?.offset?.toString(16)?.padStart(8, '0') ?: "--------"}", color = Color(0xFFFF9800), fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(80.dp))
                            Text(text = chunk.joinToString(" ") { it.value.toUByte().toString(16).padStart(2, '0').uppercase() }, color = Color(0xFF00FF00), fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                            Text(text = chunk.joinToString("") { it.ascii.toString() }, color = Color(0xFF00BFFF), fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(80.dp))
                        }
                    }
                }
            }
        } else if (selectedFile != null && !isProcessing) {
            Text(text = "No data loaded", modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (logMessage.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))) {
                Text(text = logMessage, color = Color(0xFF00FF00), fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.fillMaxWidth().padding(8.dp).heightIn(max = 80.dp).verticalScroll(rememberScrollState()))
            }
        }
    }
}
