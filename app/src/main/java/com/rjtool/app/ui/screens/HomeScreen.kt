package com.rjtool.app.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rjtool.app.engine.PakEngine
import com.rjtool.app.ui.components.TopHeader
import com.rjtool.app.ui.theme.*
import com.rjtool.app.utils.FileUtils
import kotlinx.coroutines.launch
import java.io.File

data class HomeTool(val title: String, val subtitle: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
fun HomeScreen(
    onPAKUnpack: () -> Unit,
    onPAKRepack: () -> Unit,
    onLUADecompile: () -> Unit,
    onLUACompile: () -> Unit,
    onSizeFixer: () -> Unit,
    onHexEditor: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var scriptStatus by remember { mutableStateOf("") }

    val tools = listOf(
        HomeTool("PAK Unpack", "PAK -> files (Python)", Icons.Default.ArrowDownward, onPAKUnpack),
        HomeTool("PAK Repack", "EDITTED -> PAK (Python)", Icons.Default.ArrowUpward, onPAKRepack),
        HomeTool("LUA Decompile", "bytecode -> Lua source", Icons.Default.DataObject, onLUADecompile),
        HomeTool("LUA Compile", "Lua source -> bytecode", Icons.Default.Code, onLUACompile),
        HomeTool("Size Fixer", "Auto detect & pad RESULT_PAK", Icons.Default.FitScreen, onSizeFixer),
        HomeTool("Hex Editor", "Edit .uexp / .uasset & Headshot", Icons.Default.Edit, onHexEditor)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TopHeader()
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "${Build.MODEL} · Android ${Build.VERSION.RELEASE}",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(14.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Fixed workspace", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("/storage/emulated/0/RJTOOL", fontSize = 13.5.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Storage access granted · Python 3.11 Active", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = AccentTeal)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dynamic Python Script Runner Button (e.g. fix.mainactivity.py)
        Button(
            onClick = {
                val scriptCandidates = listOf(
                    File(FileUtils.ROOT_DIR, "fix.mainactivity.py"),
                    File(FileUtils.ROOT_DIR, "fix_mainactivity.py")
                )
                val script = scriptCandidates.firstOrNull { it.exists() }
                if (script == null) {
                    scriptStatus = "Drop fix.mainactivity.py into /storage/emulated/0/RJTOOL/ to run!"
                } else {
                    scope.launch {
                        PakEngine.runFixMainActivityScript(context, script) { msg ->
                            scriptStatus = msg
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A34)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Terminal, contentDescription = null, tint = AccentTeal)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Execute fix.mainactivity.py", color = AccentTeal, fontWeight = FontWeight.Bold)
        }

        if (scriptStatus.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = scriptStatus, color = AccentTeal, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        tools.forEach { tool ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                shape = RoundedCornerShape(16.dp),
                onClick = tool.onClick
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E3A34)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(tool.icon, tool.title, tint = AccentTeal, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tool.title, fontSize = 16.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(tool.subtitle, fontSize = 13.sp, color = TextSecondary)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, "Go", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
