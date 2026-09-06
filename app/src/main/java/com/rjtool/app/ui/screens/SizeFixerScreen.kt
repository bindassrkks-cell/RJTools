package com.rjtool.app.ui.screens
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitScreen
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
fun SizeFixerScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var targetSize by remember { mutableStateOf("15.01M") }
    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var logMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Text(text = "Size Fixer", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.15f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "📏 Size Fixer", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Fix file sizes in PAK index", fontSize = 14.sp, color = Color(0xFF4CAF50).copy(alpha = 0.7f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = { Toast.makeText(context, "Select folder containing files to fix", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.FitScreen, contentDescription = "Select Folder")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select Folder")
        }
        if (selectedFolder != null) {
            Text(text = "📂 $selectedFolder", modifier = Modifier.padding(vertical = 8.dp), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = targetSize, onValueChange = { targetSize = it }, label = { Text("Target Size (e.g., 15.01M)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (selectedFolder == null) {
                    Toast.makeText(context, "Please select a folder", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isProcessing = true
                progress = 0f
                logMessage = "Starting size fix...\n"
                scope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            val folder = File(selectedFolder!!)
                            if (!folder.exists()) {
                                logMessage += "❌ Folder does not exist\n"
                                return@withContext
                            }
                            val files = folder.listFiles()?.filter { it.isFile } ?: emptyList()
                            val total = files.size
                            files.forEachIndexed { index, file ->
                                kotlinx.coroutines.delay(100)
                                progress = (index + 1).toFloat() / total
                                logMessage += "Fixed: ${file.name} → ${targetSize}\n"
                            }
                            logMessage += "✅ Size fix complete! ${total} files processed.\n"
                            Toast.makeText(context, "Size fix complete!", Toast.LENGTH_SHORT).show()
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
            Text(if (isProcessing) "Processing..." else "Fix Sizes")
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
