package com.lalema.app.ui.ai

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lalema.app.ai.ChatMessage
import com.lalema.app.ui.theme.LocalIsDarkTheme
import com.lalema.app.ui.theme.LocalGlassBackdrop
import com.lalema.app.ui.theme.LiquidGlassSurface
import com.lalema.app.ui.theme.glassContentColor
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.backdrop.shadow.InnerShadow
import androidx.compose.ui.unit.DpOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    navController: NavController,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val isDark = LocalIsDarkTheme.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "AI健康顾问",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (isLoading) "思考中..." else "在线",
                                fontSize = 12.sp,
                                color = if (isLoading) MaterialTheme.colorScheme.primary else Color(0xFF4CAF50)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.height(44.dp)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            // 消息列表
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 60.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "我是您的AI健康顾问",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "可以问我关于肠道健康、饮食建议等问题",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(messages) { message ->
                    ChatBubble(message = message)
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                LiquidGlassSurface(
                    modifier = Modifier,
                    cornerRadius = 16.dp
                ) {
                    SimpleLoadingDot()
                }
                        }
                    }
                }
            }

            // 输入框
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { if (it.length <= 500) inputText = it },
                    placeholder = { Text("输入您的问题...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.35f),
                        unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.25f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.40f)
                    ),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .drawBackdrop(
                            backdrop = LocalGlassBackdrop.current,
                            shape = { CircleShape },
                            effects = {
                                vibrancy()
                                blur(14f.dp.toPx())
                                lens(8f.dp.toPx(), 12f.dp.toPx())
                            },
                            highlight = { Highlight.Default },
                            shadow = {
                                Shadow(
                                    radius = 10.dp,
                                    offset = DpOffset(0.dp, 3.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                                )
                            },
                            innerShadow = {
                                InnerShadow(
                                    radius = 10.dp,
                                    color = Color.White.copy(alpha = if (isDark) 0.10f else 0.40f)
                                )
                            },
                            onDrawSurface = {
                                drawRect(
                                    if (inputText.isBlank()) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                )
                            }
                        )
                        .clickable(enabled = inputText.isNotBlank() && !isLoading) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "发送",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleLoadingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "dot")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )
    Box(
        modifier = Modifier
            .size(20.dp)
            .scale(scale)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
    )
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (isUser) {
            LiquidGlassSurface(
                cornerRadius = 16.dp,
                tint = MaterialTheme.colorScheme.primary,
                contentPadding = 0.dp
            ) {
                Text(
                    text = message.content,
                    color = glassContentColor(MaterialTheme.colorScheme.primary),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(
                        top = 10.dp,
                        bottom = 10.dp,
                        start = 14.dp,
                        end = 14.dp
                    )
                )
            }
        } else {
            LiquidGlassSurface(cornerRadius = 16.dp) {
                Text(
                    text = message.content,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
