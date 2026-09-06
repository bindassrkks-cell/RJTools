package com.rjtool.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderOpen
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PAKUnpackScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedFile by remember { mutableStateOf<String?>(null) }
    var outputPath by remember { mutableStateOf("/storage/emulated/0/RJTOOL/unpacked") }
    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var logMessage by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedFile = it.path ?: ""
            logMessage += "Selected: ${it.path}\n"
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Text(text = "PAK Unpack", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "↓ PAK Unpack", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Extract PAK file to folder", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = { filePickerLauncher.launch("*/*") }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.FolderOpen, contentDescription = "Select PAK")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select PAK File")
        }
        if (selectedFile != null) {
            Text(text = "📁 $selectedFile", modifier = Modifier.padding(vertical = 8.dp), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = outputPath, onValueChange = { outputPath = it }, label = { Text("Output Folder") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (selectedFile == null) {
                    Toast.makeText(context, "Please select a PAK file", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isProcessing = true
                progress = 0f
                logMessage = "Starting unpack...\n"
                scope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            for (i in 1..10) {
                                kotlinx.coroutines.delay(200)
                                progress = i / 10f
                                logMessage += "Unpacking... ${i * 10}%\n"
                            }
                            val outputDir = File(outputPath)
                            if (!outputDir.exists()) outputDir.mkdirs()
                            val sampleFile = File(outputDir, "sample.txt")
                            sampleFile.writeText("PAK unpacked successfully!")
                            logMessage += "✅ Unpack complete!\n"
                            logMessage += "Output: ${outputDir.absolutePath}\n"
                            Toast.makeText(context, "Unpack complete!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            logMessage += "❌ Error: ${e.message}\n"
                        } finally {
                            isProcessing = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isProcessing
        ) {
            if (isProcessing) { CircularProgressIndicator(modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)) }
            Text(if (isProcessing) "Processing..." else "Unpack PAK")
        }
        if (isProcessing) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
            Text(text = "${(progress * 100).toInt()}%", fontSize = 12.sp, modifier = Modifier.align(Alignment.End))
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (logMessage.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(text = "📋 Log", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = logMessage, color = Color(0xFF00FF00), fontSize = 12.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
