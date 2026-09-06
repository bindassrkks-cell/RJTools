package com.rjtool.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rjtool.app.ui.screens.*
import com.rjtool.app.ui.theme.DarkBackground
import com.rjtool.app.ui.theme.RJTOOLTheme
import com.rjtool.app.utils.FileUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (_: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            }
        }

        FileUtils.initRJToolWorkspace(this)

        setContent {
            RJTOOLTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onPAKUnpack = { navController.navigate("pak_unpack") },
                                onPAKRepack = { navController.navigate("pak_repack") },
                                onLUADecompile = { navController.navigate("lua_decompile") },
                                onLUACompile = { navController.navigate("lua_compile") },
                                onSizeFixer = { navController.navigate("size_fixer") },
                                onHexEditor = { navController.navigate("hex_editor") }
                            )
                        }
                        composable("pak_unpack") { PAKUnpackScreen(navController) }
                        composable("pak_repack") { PAKRepackScreen(navController) }
                        composable("lua_decompile") { LUADecompileScreen(navController) }
                        composable("lua_compile") { LUACompileScreen(navController) }
                        composable("size_fixer") { SizeFixerScreen(navController) }
                        composable("hex_editor") { HexEditorScreen(navController) }
                    }
                }
            }
        }
    }
}
