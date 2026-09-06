package com.rjtool.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFD6E2E6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, "Avatar", tint = Color(0xFF5F6368), modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("RJTOOL v1.0.59", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))
            Text("TG @byrj6", fontSize = 14.sp, color = Color(0xFF757575))
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8F5E9))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Settings, "Settings", tint = Color(0xFF2E7D32), modifier = Modifier.size(22.dp))
        }
    }
}
