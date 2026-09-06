package com.rjtool.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rjtool.app.ui.components.FilePickerDialog
import com.rjtool.app.ui.components.TopHeader
import com.rjtool.app.ui.theme.*
import com.rjtool.app.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile

@Composable
fun SizeFixerScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var showPickerDialog by remember { mutableStateOf(false) }
    var targetSizeInput by remember { mutableStateOf("15.01M") }
    var logMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val resultPakFiles = remember { FileUtils.getFilesInFolder("RESULT_PAK") }

    if (showPickerDialog) {
        FilePickerDialog(
            title = "Choose File in RESULT_PAK",
            files = resultPakFiles,
            onDismiss = { showPickerDialog = false },
            onFileSelected = { selectedFile = it }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TopHeader()
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.clickable { navController.popBackStack() }.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBackIos, "Back", tint = AccentTeal, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back", color = AccentTeal, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Size Fixer", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Auto-detect from RESULT_PAK & pad to target", fontSize = 14.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Target file · RESULT_PAK", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(selectedFile?.name ?: "Not selected", fontSize = 13.sp, color = TextSecondary)
                }
                Button(
                    onClick = { showPickerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A34)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Choose", color = AccentTeal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = targetSizeInput,
            onValueChange = { targetSizeInput = it },
            label = { Text("Target Size (e.g., 15.01M, 15738880)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val file = selectedFile
                if (file == null || !file.exists()) {
                    logMessage += "❌ Please select a file from RESULT_PAK first!\n"
                    return@Button
                }
                val trimmed = targetSizeInput.trim().uppercase()
                val targetBytes = when {
                    trimmed.endsWith("MB") -> (trimmed.removeSuffix("MB").toDoubleOrNull() ?: 0.0) * 1024 * 1024
                    trimmed.endsWith("M") -> (trimmed.removeSuffix("M").toDoubleOrNull() ?: 0.0) * 1024 * 1024
                    trimmed.endsWith("KB") -> (trimmed.removeSuffix("KB").toDoubleOrNull() ?: 0.0) * 1024
                    trimmed.endsWith("K") -> (trimmed.removeSuffix("K").toDoubleOrNull() ?: 0.0) * 1024
                    else -> trimmed.toDoubleOrNull() ?: 0.0
                }.toLong()

                isProcessing = true
                scope.launch {
                    withContext(Dispatchers.IO) {
                        val oldLen = file.length()
                        RandomAccessFile(file, "rw").use { it.setLength(targetBytes) }
                        logMessage += "✅ Fixed ${file.name}\nOld: $oldLen B -> New: ${file.length()} B\n"
                    }
                    isProcessing = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (selectedFile != null) ButtonGreen else ButtonDisabled),
            shape = RoundedCornerShape(10.dp),
            enabled = !isProcessing && selectedFile != null
        ) {
            Text("Apply Exact Size", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        if (logMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = logMessage, color = AccentTeal, fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.padding(14.dp))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
