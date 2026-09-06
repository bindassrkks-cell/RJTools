package com.rjtool.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rjtool.app.ui.theme.*
import java.io.File

@Composable
fun FilePickerDialog(
    title: String,
    files: List<File>,
    onDismiss: () -> Unit,
    onFileSelected: (File) -> Unit
) {
    var tempSelected by remember { mutableStateOf<File?>(files.firstOrNull()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkDialogBg)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))

                if (files.isEmpty()) {
                    Text("No files detected in folder.", fontSize = 14.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)) {
                        items(files) { file ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { tempSelected = file }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = tempSelected == file,
                                    onCheckedChange = { if (it) tempSelected = file },
                                    colors = CheckboxDefaults.colors(checkedColor = AccentTeal, uncheckedColor = TextSecondary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(file.name, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = AccentTeal, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(
                        onClick = {
                            tempSelected?.let { onFileSelected(it) }
                            onDismiss()
                        },
                        enabled = tempSelected != null
                    ) {
                        Text("确定", color = if (tempSelected != null) AccentTeal else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
