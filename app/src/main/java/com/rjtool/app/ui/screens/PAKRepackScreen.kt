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
import com.rjtool.app.engine.PakEngine
import com.rjtool.app.ui.components.FilePickerDialog
import com.rjtool.app.ui.components.TopHeader
import com.rjtool.app.ui.theme.*
import com.rjtool.app.utils.FileUtils
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun PAKRepackScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    var selectedPak by remember { mutableStateOf<File?>(null) }
    var showPickerDialog by remember { mutableStateOf(false) }
    var enableCustomRepack by remember { mutableStateOf(true) }
    var selectedMode by remember { mutableStateOf(2) }
    var isProcessing by remember { mutableStateOf(false) }
    var logMessage by remember { mutableStateOf("") }

    val pakFiles = remember { FileUtils.getFilesInFolder("PAK_ORIGINAL", listOf(".pak")) }

    if (showPickerDialog) {
        FilePickerDialog(
            title = "Choose original PAK files",
            files = pakFiles,
            onDismiss = { showPickerDialog = false },
            onFileSelected = { selectedPak = it }
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
        Text("PAK Repack", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("EDITTED -> PAK", fontSize = 14.sp, color = TextSecondary)
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
                    Text("Choose original PAK files · PAK_ORIGINAL", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(selectedPak?.name ?: "Not selected", fontSize = 13.sp, color = TextSecondary)
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

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { enableCustomRepack = !enableCustomRepack }
                ) {
                    Checkbox(
                        checked = enableCustomRepack,
                        onCheckedChange = { enableCustomRepack = it },
                        colors = CheckboxDefaults.colors(checkedColor = AccentTeal, uncheckedColor = TextSecondary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enable custom PAK repacking", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { selectedMode = 1 }.padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = selectedMode == 1,
                        onClick = { selectedMode = 1 },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentTeal, unselectedColor = TextSecondary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("1. Match paths using index.csv", fontSize = 14.sp, color = TextPrimary)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { selectedMode = 2 }.padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = selectedMode == 2,
                        onClick = { selectedMode = 2 },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentTeal, unselectedColor = TextSecondary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("2. Use the EDITTED directory structure", fontSize = 14.sp, color = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val outName = selectedPak?.name ?: "repacked.pak"
                val targetPak = File(File(FileUtils.ROOT_DIR, "RESULT_PAK"), outName)
                val sourceDir = File(FileUtils.ROOT_DIR, "EDITTED")

                isProcessing = true
                logMessage = "Starting repack into ${targetPak.name}...\n"
                scope.launch {
                    PakEngine.repackPak(sourceDir, targetPak, selectedMode == 1) { msg ->
                        logMessage += "$msg\n"
                    }
                    isProcessing = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
            shape = RoundedCornerShape(10.dp),
            enabled = !isProcessing
        ) {
            Text(if (isProcessing) "Repacking..." else "Repack PAK", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
    }
}
