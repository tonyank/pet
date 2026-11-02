package ytstudio.pet

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import ytstudio.pet.ui.screens.CharacterManagerScreen
import ytstudio.pet.ui.screens.DashboardScreen
import ytstudio.pet.ui.screens.SettingsScreen
import ytstudio.pet.ui.theme.PetTheme

class MainActivity : ComponentActivity() {

    private var hasOverlayPermissionState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkOverlayPermission()

        setContent {
            PetTheme {
                MainScreen(
                    hasOverlayPermission = hasOverlayPermissionState.value,
                    onRequestPermission = { requestOverlayPermission() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkOverlayPermission()
    }

    private fun checkOverlayPermission() {
        hasOverlayPermissionState.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }
}

sealed class Screen(val title: String, val icon: ImageVector) {
    object Dashboard : Screen("桌宠", Icons.Default.Home)
    object Characters : Screen("角色", Icons.Default.Face)
    object Settings : Screen("设置", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    hasOverlayPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "U的桌宠", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                listOf(Screen.Dashboard, Screen.Characters, Screen.Settings).forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen }
                    )
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (selectedScreen) {
            Screen.Dashboard -> DashboardScreen(
                hasOverlayPermission = hasOverlayPermission,
                onRequestPermission = onRequestPermission,
                modifier = modifier
            )
            Screen.Characters -> CharacterManagerScreen(
                modifier = modifier
            )
            Screen.Settings -> SettingsScreen(
                modifier = modifier
            )
        }
    }
}
