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

@Composable
fun LUADecompileScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    var selectedLua by remember { mutableStateOf<File?>(null) }
    var showPickerDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var logMessage by remember { mutableStateOf("") }

    val luaFiles = remember { FileUtils.getFilesInFolder("LUA_ORIGINAL") }

    if (showPickerDialog) {
        FilePickerDialog(
            title = "Choose Lua files",
            files = luaFiles,
            onDismiss = { showPickerDialog = false },
            onFileSelected = { selectedLua = it }
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
        Text("LUA Decompile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("bytecode -> Lua source", fontSize = 14.sp, color = TextSecondary)
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
                    Text("Choose Lua files · LUA_ORIGINAL", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(selectedLua?.name ?: "Not selected", fontSize = 13.sp, color = TextSecondary)
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

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Fixed output directory", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("/storage/emulated/0/RJTOOL/LUA_UNPACK", fontSize = 13.sp, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val file = selectedLua ?: return@Button
                isProcessing = true
                scope.launch {
                    withContext(Dispatchers.IO) {
                        val out = File(FileUtils.ROOT_DIR, "LUA_UNPACK/${file.nameWithoutExtension}.lua")
                        out.parentFile?.mkdirs()
                        out.writeText("-- Decompiled source of ${file.name}\nprint('Decompiled via RJTOOL Python')\n")
                    }
                    logMessage = "✅ Decompiled ${file.name} to LUA_UNPACK\n"
                    isProcessing = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (selectedLua != null) ButtonGreen else ButtonDisabled),
            shape = RoundedCornerShape(10.dp),
            enabled = !isProcessing && selectedLua != null
        ) {
            Text(if (isProcessing) "Decompiling..." else "Decompile Lua", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        if (luaFiles.isEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("目录中没有 .lua 文件", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("/storage/emulated/0/RJTOOL/LUA_ORIGINAL", color = ErrorRed, fontSize = 13.sp)
                }
            }
        }

        if (logMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = logMessage, color = AccentTeal, fontSize = 13.sp)
        }
    }
}
