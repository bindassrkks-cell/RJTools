package com.rjtool.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.rjtool.app.ui.components.TopHeader

@Composable
fun LUACompileScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FA)).padding(20.dp)) {
        TopHeader()
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.clickable { navController.popBackStack() }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.ArrowBackIos, "Back", tint = Color(0xFF00796B), modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back", color = Color(0xFF00796B), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("LUA Compile", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Lua source -> bytecode", fontSize = 14.sp, color = Color(0xFF757575))
    }
}
