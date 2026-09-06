package com.rjtool.app.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rjtool.app.engine.SizeFixerEngine
import com.rjtool.app.ui.components.TopHeader
import com.rjtool.app.utils.FileUtils
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SizeFixerScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedFileName by remember { mutableStateOf("") }
    var targetSizeInput by remember { mutableStateOf("15.01M") }
    var logMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TopHeader()
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.clickable { navController.popBackStack() }.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBackIos, "Back", tint = Color(0xFF00796B), modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back", color = Color(0xFF00796B), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Size Fixer", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))
        Text("Pad file to exact required size", fontSize = 14.sp, color = Color(0xFF757575))
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Workspace files in RESULT_PAK or EDITTED", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = selectedFileName,
                    onValueChange = { selectedFileName = it },
                    label = { Text("Filename (e.g. repacked.pak)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = targetSizeInput,
                    onValueChange = { targetSizeInput = it },
                    label = { Text("Target Size (e.g. 15.01M or 15738880)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val bytes = SizeFixerEngine.parseSizeToBytes(targetSizeInput)
                if (bytes == null || bytes <= 0) {
                    Toast.makeText(context, "Invalid target size format", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val possibleFiles = listOf(
                    File(FileUtils.ROOT_DIR, "RESULT_PAK/$selectedFileName"),
                    File(FileUtils.ROOT_DIR, "EDITTED/$selectedFileName"),
                    File(FileUtils.ROOT_DIR, selectedFileName)
                )
                val target = possibleFiles.firstOrNull { it.exists() && it.isFile }
                if (target == null) {
                    logMessage += "❌ File $selectedFileName not found in RJTOOL folders!\n"
                    return@Button
                }

                isProcessing = true
                scope.launch {
                    SizeFixerEngine.fixFileSize(target, bytes) {
                        logMessage += "$it\n"
                    }
                    isProcessing = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
            shape = RoundedCornerShape(10.dp),
            enabled = !isProcessing
        ) {
            Text("Apply Exact Size", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        if (logMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = logMessage, color = Color(0xFF00FF66), fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.padding(14.dp))
            }
        }
    }
}
