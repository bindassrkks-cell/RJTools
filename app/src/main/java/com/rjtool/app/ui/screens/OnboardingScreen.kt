package com.rjtool.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "settings")

@Composable
fun OnboardingScreen(onAgree: () -> Unit) {
    var agreed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = "RJTOOL v1.0.59", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "TG @byrj6", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(32.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = "📋 Terms of Service & Consent", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = """
                        By using RJTOOL, you agree to the following:
                        
                        1. This tool is for educational and legitimate modding purposes only.
                        2. You are solely responsible for any modifications made to game files.
                        3. Do not use this tool to cheat in online multiplayer games.
                        4. Respect the intellectual property rights of game developers.
                        5. The developers of RJTOOL are not responsible for any damage or bans.
                        6. All operations are performed locally on your device.
                        7. Your data is not collected or shared with any third party.
                        8. This tool does not modify system files or compromise device security.
                        
                        Please use this tool responsibly and ethically.
                    """.trimIndent(),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = agreed, onCheckedChange = { agreed = it })
            Text(text = "I have read and agree to the terms", fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (agreed) {
                    scope.launch {
                        context.dataStore.edit { prefs ->
                            prefs[booleanPreferencesKey("consent_given")] = true
                        }
                        onAgree()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = agreed
        ) {
            Text("Proceed to App", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (!agreed) {
            Text(text = "Please agree to the terms to continue", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}
