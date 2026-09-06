package com.rjtool.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rjtool.app.ui.screens.*
import com.rjtool.app.ui.theme.RJTOOLTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RJTOOLTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "onboarding") {
                        composable("onboarding") {
                            OnboardingScreen(
                                onAgree = {
                                    navController.navigate("home") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
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
