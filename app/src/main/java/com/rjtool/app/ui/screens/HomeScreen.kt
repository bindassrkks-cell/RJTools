package com.rjtool.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ToolItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val description: String,
    val color: Color
)

@Composable
fun HomeScreen(
    onPAKUnpack: () -> Unit,
    onPAKRepack: () -> Unit,
    onLUADecompile: () -> Unit,
    onLUACompile: () -> Unit,
    onSizeFixer: () -> Unit,
    onHexEditor: () -> Unit
) {
    val tools = listOf(
        ToolItem("pak_unpack", "PAK Unpack", Icons.Default.FolderOpen, "Extract PAK files", MaterialTheme.colorScheme.primary),
        ToolItem("pak_repack", "PAK Repack", Icons.Default.Archive, "Repack to PAK", MaterialTheme.colorScheme.secondary),
        ToolItem("lua_decompile", "LUA Decompile", Icons.Default.Code, "Bytecode → Lua source", MaterialTheme.colorScheme.tertiary),
        ToolItem("lua_compile", "LUA Compile", Icons.Default.Build, "Lua source → bytecode", Color(0xFFFF9800)),
        ToolItem("size_fixer", "Size Fixer", Icons.Default.FitScreen, "Fix file sizes", Color(0xFF4CAF50)),
        ToolItem("hex_editor", "Hex Editor", Icons.Default.Edit, "Edit .uexp files", Color(0xFFF44336))
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "🔧 RJTOOL v1.0.59", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(text = "Storage: /storage/emulated/0/RJTOOL", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                Text(text = "Storage access granted ✅", fontSize = 14.sp, color = Color(0xFF4CAF50))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Select Tool", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(tools) { tool ->
                Card(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1.1f),
                    onClick = when (tool.id) {
                        "pak_unpack" -> onPAKUnpack
                        "pak_repack" -> onPAKRepack
                        "lua_decompile" -> onLUADecompile
                        "lua_compile" -> onLUACompile
                        "size_fixer" -> onSizeFixer
                        "hex_editor" -> onHexEditor
                        else -> {}
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(imageVector = tool.icon, contentDescription = tool.title, tint = tool.color, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = tool.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = tool.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }
    }
}
