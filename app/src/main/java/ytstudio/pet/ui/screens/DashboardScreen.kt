package ytstudio.pet.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ytstudio.pet.R
import ytstudio.pet.data.CharacterRepository
import ytstudio.pet.model.PetCharacter
import ytstudio.pet.model.SpecialAction
import ytstudio.pet.service.PetFloatingService
import kotlin.random.Random

@Composable
fun DashboardScreen(
    hasOverlayPermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isServiceRunning by remember { mutableStateOf(PetFloatingService.isRunning) }
    var activeCharacter by remember { mutableStateOf(CharacterRepository.getActiveCharacter(context)) }

    // Preview state
    var previewBubbleText by remember { mutableStateOf<String?>(null) }
    var isPreviewKissing by remember { mutableStateOf(false) }

    LaunchedEffect(previewBubbleText) {
        if (previewBubbleText != null) {
            delay(3200)
            previewBubbleText = null
        }
    }

    LaunchedEffect(isPreviewKissing) {
        if (isPreviewKissing) {
            delay(4800)
            isPreviewKissing = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Overlay Permission Banner if missing
        if (!hasOverlayPermission) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "未授予悬浮窗权限",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100),
                            fontSize = 15.sp
                        )
                        Text(
                            text = "需要在其他应用上层显示，才能让桌宠浮现在屏幕上。",
                            color = Color(0xFF5D4037),
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("去授权", fontSize = 12.sp)
                    }
                }
            }
        }

        // Pet Interactive Preview & Stage
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Interactive Pet Stage
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Speech Bubble
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.BottomCenter
                        if (previewBubbleText != null) {
                            Surface(
                                color = Color(0xFFFFFDE7),
                                shape = RoundedCornerShape(16.dp),
                                shadowElevation = 4.dp,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE082)),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = previewBubbleText ?: "",
                                    color = Color(0xFF37474F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Pet Avatar with breathing effect
                    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
                    val breatheScale by infiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(160.dp, 200.dp)
                            .scale(if (isPreviewKissing) 1.0f else breatheScale)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFEEEEEE))
                            .clickable {
                                // Trigger interaction preview
                                if (isPreviewKissing) return@clickable
                                val total = activeCharacter.dialogues.size +
                                        if (activeCharacter.specialActionType == SpecialAction.KISS_AUDIO) 1 else 0
                                val pick = Random.nextInt(total)
                                if (activeCharacter.specialActionType == SpecialAction.KISS_AUDIO && pick == activeCharacter.dialogues.size) {
                                    isPreviewKissing = true
                                    previewBubbleText = "啾咪~ ❤️"
                                } else {
                                    previewBubbleText = activeCharacter.dialogues.getOrElse(pick) { "呼呼大睡中…" }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPreviewKissing) {
                            Image(
                                painter = painterResource(id = R.drawable.kiss_anim),
                                contentDescription = "亲吻动画",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = activeCharacter.avatarResId),
                                contentDescription = activeCharacter.name,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Sleeping zZZ badge
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                Text(
                                    text = "zZZ…",
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "当前角色：${activeCharacter.name}（呼呼大睡中）",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "点击上方宠物试听气泡或亲吻动作",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Service Start / Stop Control Button
        Button(
            onClick = {
                if (!hasOverlayPermission) {
                    onRequestPermission()
                    return@Button
                }
                if (isServiceRunning) {
                    val stopIntent = Intent(context, PetFloatingService::class.java).apply {
                        action = PetFloatingService.ACTION_STOP
                    }
                    context.startService(stopIntent)
                    isServiceRunning = false
                } else {
                    val startIntent = Intent(context, PetFloatingService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(startIntent)
                    } else {
                        context.startService(startIntent)
                    }
                    isServiceRunning = true
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isServiceRunning) Color(0xFFE53935) else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                imageVector = if (isServiceRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isServiceRunning) "收回桌宠" else "召唤“小傻子”到桌面",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
