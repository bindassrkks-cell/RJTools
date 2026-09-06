package com.rjtool.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.rjtool.app.engine.PakEngine
import com.rjtool.app.ui.components.TopHeader
import com.rjtool.app.utils.FileUtils
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun PAKUnpackScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedFilePath by remember { mutableStateOf<String?>(null) }
    var decryptLuaOnly by remember { mutableStateOf(false) }
    var decompileLua by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var logMessage by remember { mutableStateOf("") }
    var progressVal by remember { mutableStateOf(0f) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedFilePath = it.path ?: ""
            logMessage += "Selected file: ${it.path}\n"
        }
    }

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
            modifier = Modifier
                .clickable { navController.popBackStack() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBackIos, "Back", tint = Color(0xFF00796B), modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back", color = Color(0xFF00796B), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("PAK Unpack", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))
        Text("PAK -> files", fontSize = 14.sp, color = Color(0xFF757575))
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Choose PAK files · PAK_ORIGINAL", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(selectedFilePath ?: "Not selected", fontSize = 13.sp, color = Color(0xFF757575))
                }
                Button(
                    onClick = { filePicker.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2F1)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Choose", color = Color(0xFF00796B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Fixed output directory", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))
                Spacer(modifier = Modifier.height(4.dp))
                Text("/storage/emulated/0/RJTOOL/PAK_UNPACK", fontSize = 13.sp, color = Color(0xFF757575))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { decryptLuaOnly = !decryptLuaOnly }
                ) {
                    Checkbox(
                        checked = decryptLuaOnly,
                        onCheckedChange = { decryptLuaOnly = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00796B))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Decrypt .lua files only", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth().clickable { decompileLua = !decompileLua }
                ) {
                    Checkbox(
                        checked = decompileLua,
                        onCheckedChange = { decompileLua = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00796B))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Also decompile Lua and replace the original bytecode",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val pakDir = File(FileUtils.ROOT_DIR, "PAK_ORIGINAL")
                val defaultPak = pakDir.listFiles()?.firstOrNull { it.name.endsWith(".pak", true) }
                val target = if (selectedFilePath != null) File(selectedFilePath!!) else defaultPak

                if (target == null || !target.exists()) {
                    Toast.makeText(context, "Please put a .pak file in RJTOOL/PAK_ORIGINAL", Toast.LENGTH_LONG).show()
                    logMessage += "❌ No .pak file found in ${pakDir.absolutePath}\n"
                    return@Button
                }

                isProcessing = true
                logMessage += "Starting extraction of ${target.name}...\n"
                scope.launch {
                    val outDir = File(FileUtils.ROOT_DIR, "PAK_UNPACK")
                    PakEngine.unpackPak(target, outDir, decryptLuaOnly) { msg, prog ->
                        logMessage += "$msg\n"
                        progressVal = prog
                    }
                    isProcessing = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
            shape = RoundedCornerShape(10.dp),
            enabled = !isProcessing
        ) {
            Text(if (isProcessing) "Unpacking..." else "Unpack PAK", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        if (logMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = logMessage,
                    color = Color(0xFF00FF66),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
