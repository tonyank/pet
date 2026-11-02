package ytstudio.pet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ytstudio.pet.data.CharacterRepository
import ytstudio.pet.model.PetSettings

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(CharacterRepository.getSettings(context)) }

    fun updateSettings(newSettings: PetSettings) {
        settings = newSettings
        CharacterRepository.saveSettings(context, newSettings)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "桌宠行为偏好",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Sound Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("亲吻互动音效", fontWeight = FontWeight.SemiBold)
                        Text("触发亲吻时播放原视频伴奏与声音", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = settings.soundEnabled,
                        onCheckedChange = { updateSettings(settings.copy(soundEnabled = it)) }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Snap to Edge Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("自动贴边停靠", fontWeight = FontWeight.SemiBold)
                        Text("拖拽松手后平滑磁吸停靠在屏幕边缘", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = settings.snapToEdge,
                        onCheckedChange = { updateSettings(settings.copy(snapToEdge = it)) }
                    )
                }
            }
        }

        // App & Build Specifications
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "项目配置规范",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                InfoItem("包名 (Package)", "ytstudio.pet")
                InfoItem("应用名称", "U的桌宠")
                InfoItem("UI 架构", "Single Activity + Jetpack Compose Material 3")
                InfoItem("最低支持 (Min SDK)", "API 24 (Android 7.0)")
                InfoItem("目标版本 (Target SDK)", "API 34 (Android 14)")
                InfoItem("构建语言", "Kotlin DSL (.gradle.kts)")
                InfoItem("CI/CD 工作流", "GitHub Actions workflow_dispatch (debug/release)")
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
