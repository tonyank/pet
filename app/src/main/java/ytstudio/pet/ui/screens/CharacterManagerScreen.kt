package ytstudio.pet.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ytstudio.pet.data.CharacterRepository
import ytstudio.pet.model.PetCharacter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterManagerScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var characters by remember { mutableStateOf(CharacterRepository.getCharacters()) }
    var activeCharId by remember { mutableStateOf(CharacterRepository.getActiveCharacter(context).id) }
    var showAddDialog by remember { mutableStateOf(false) }

    var newCharName by remember { mutableStateOf("") }
    var newCharQuotes by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "创建角色")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "角色库与切换",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "选择桌面常驻宠物角色，或自定义全新的桌宠与动作气泡。",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )
            }

            items(characters) { char ->
                val isSelected = char.id == activeCharId

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFFFEBE8) else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            CharacterRepository.setActiveCharacter(context, char.id)
                            activeCharId = char.id
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = char.avatarResId),
                            contentDescription = char.name,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEEEEE))
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = char.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (char.id == "xiaoshazi") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "主角",
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "对话气泡 (${char.dialogues.size}条): ${char.dialogues.joinToString("、")}",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                maxLines = 2,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "已选择",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("创建新桌宠角色") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newCharName,
                            onValueChange = { newCharName = it },
                            label = { Text("角色名称") },
                            placeholder = { Text("例如：小可爱") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newCharQuotes,
                            onValueChange = { newCharQuotes = it },
                            label = { Text("随机对话气泡（用换行或逗号隔开）") },
                            placeholder = { Text("早安！\n吃饭了吗？\n今天想你啦") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newCharName.isNotBlank()) {
                                val quotes = newCharQuotes.split("\n", ",")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                val created = CharacterRepository.addCustomCharacter(newCharName, quotes)
                                characters = CharacterRepository.getCharacters()
                                CharacterRepository.setActiveCharacter(context, created.id)
                                activeCharId = created.id
                                showAddDialog = false
                                newCharName = ""
                                newCharQuotes = ""
                            }
                        }
                    ) {
                        Text("保存并应用")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}
