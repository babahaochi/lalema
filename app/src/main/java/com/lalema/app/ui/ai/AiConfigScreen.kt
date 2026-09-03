package com.lalema.app.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lalema.app.ai.AiProvider
import com.lalema.app.ui.theme.GlassMotion
import com.lalema.app.ui.theme.LiquidGlassButton
import com.lalema.app.ui.theme.LiquidGlassCard
import com.lalema.app.ui.theme.LiquidGlassIconButton
import com.lalema.app.ui.theme.LiquidGlassSurface
import com.lalema.app.ui.theme.LiquidGlassSwitch
import com.lalema.app.ui.theme.LiquidGlassTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiConfigScreen(
    navController: NavController,
    viewModel: AiConfigViewModel = hiltViewModel()
) {
    val config by viewModel.config.collectAsState()
    var expandedProvider by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf(config.apiKey) }
    var model by remember { mutableStateOf(config.model) }
    var customUrl by remember { mutableStateOf(config.customBaseUrl) }
    var useLocalOnly by remember { mutableStateOf(config.useLocalAiOnly) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI配置",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    LiquidGlassIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        onClick = { navController.popBackStack() }
                    )
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "AI服务提供商",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LiquidGlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedProvider = !expandedProvider },
                        cornerRadius = 12.dp,
                        contentPadding = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = config.provider.displayName,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = if (expandedProvider) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = expandedProvider,
                        enter = expandVertically(animationSpec = tween(GlassMotion.DURATION_MEDIUM)) + fadeIn(),
                        exit = shrinkVertically(animationSpec = tween(GlassMotion.DURATION_MEDIUM)) + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AiProvider.values().forEach { provider ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (config.provider == provider)
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            viewModel.updateProvider(provider)
                                            expandedProvider = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = provider.displayName,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (config.provider == provider) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (config.provider == AiProvider.CUSTOM) {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "自定义API地址",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        GlassTextField(
                            value = customUrl,
                            onValueChange = { customUrl = it },
                            label = "Base URL",
                            placeholder = "https://api.example.com/v1/"
                        )
                    }
                }
            }

            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "API Key",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "您的API Key将加密存储在本地，不会上传到任何服务器",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = "API Key",
                        placeholder = "sk-..."
                    )
                }
            }

            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "模型",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = "模型名称",
                        placeholder = config.provider.defaultModel
                    )
                }
            }

            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "仅使用本地AI",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "不调用云端API，所有分析在本地完成",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LiquidGlassSwitch(
                        checked = useLocalOnly,
                        onCheckedChange = { useLocalOnly = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LiquidGlassButton(
                text = "保存配置",
                onClick = {
                    viewModel.saveConfig(
                        apiKey = apiKey,
                        model = model,
                        customUrl = customUrl,
                        useLocalOnly = useLocalOnly
                    )
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    LiquidGlassTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
