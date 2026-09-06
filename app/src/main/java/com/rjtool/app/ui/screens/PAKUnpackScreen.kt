package com.rjtool.app.ui.screens

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
import com.rjtool.app.ui.components.FilePickerDialog
import com.rjtool.app.ui.components.TopHeader
import com.rjtool.app.ui.theme.*
import com.rjtool.app.utils.FileUtils
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun PAKUnpackScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var decryptLuaOnly by remember { mutableStateOf(false) }
    var decompileLua by remember { mutableStateOf(false) }
    var showPickerDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var logMessage by remember { mutableStateOf("") }

    val detectedPakFiles = remember { FileUtils.getFilesInFolder("PAK_ORIGINAL", listOf(".pak")) }

    val safPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val resolved = FileUtils.resolveUriToFile(context, it, "PAK_ORIGINAL")
            if (resolved != null && resolved.exists()) {
                selectedFile = resolved
                logMessage += "Selected: ${resolved.name} (${resolved.length()} bytes)\n"
            } else {
                logMessage += "❌ Could not read SAF file.\n"
            }
        }
    }

    if (showPickerDialog) {
        FilePickerDialog(
            title = "Choose original PAK files",
            files = detectedPakFiles,
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
        Text("PAK Unpack", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("PAK -> files", fontSize = 14.sp, color = TextSecondary)
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
                    Text("Choose PAK files · PAK_ORIGINAL", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(selectedFile?.name ?: "Not selected", fontSize = 13.sp, color = TextSecondary)
                }
                Button(
                    onClick = {
                        if (detectedPakFiles.isNotEmpty()) {
                            showPickerDialog = true
                        } else {
                            safPickerLauncher.launch("*/*")
                        }
                    },
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
                Text("/storage/emulated/0/RJTOOL/PAK_UNPACK", fontSize = 13.sp, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { decryptLuaOnly = !decryptLuaOnly }
                ) {
                    Checkbox(
                        checked = decryptLuaOnly,
                        onCheckedChange = { decryptLuaOnly = it },
                        colors = CheckboxDefaults.colors(checkedColor = AccentTeal, uncheckedColor = TextSecondary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Decrypt .lua files only", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth().clickable { decompileLua = !decompileLua }
                ) {
                    Checkbox(
                        checked = decompileLua,
                        onCheckedChange = { decompileLua = it },
                        colors = CheckboxDefaults.colors(checkedColor = AccentTeal, uncheckedColor = TextSecondary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Also decompile Lua and replace the original bytecode",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val pak = selectedFile
                if (pak == null || !pak.exists()) {
                    logMessage += "❌ Please select a valid PAK file first!\n"
                    return@Button
                }
                isProcessing = true
                logMessage = "Starting Python extraction: ${pak.name}...\n"
                scope.launch {
                    val outDir = File(FileUtils.ROOT_DIR, "PAK_UNPACK")
                    PakEngine.unpackPakWithPython(context, pak, outDir, decryptLuaOnly) { msg ->
                        logMessage += "$msg\n"
                    }
                    isProcessing = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (selectedFile != null) ButtonGreen else ButtonDisabled),
            shape = RoundedCornerShape(10.dp),
            enabled = !isProcessing && selectedFile != null
        ) {
            Text(if (isProcessing) "Unpacking via Python..." else "Unpack PAK", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
