package com.lalema.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lalema.app.api.ApiClient
import com.lalema.app.api.LoginRequest
import com.lalema.app.api.RegisterRequest
import com.lalema.app.ui.theme.LiquidGlassCard
import com.lalema.app.ui.theme.LiquidGlassButton
import com.lalema.app.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = LocalIsDarkTheme.current
    var isLogin by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isLogin) "登录" else "注册",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "拉了吗",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (isLogin) "登录以同步您的数据" else "创建账号开始同步",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GlassTextField(
                        value = username,
                        onValueChange = { username = it; errorMsg = null },
                        label = "用户名",
                        placeholder = "请输入用户名",
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GlassTextField(
                        value = password,
                        onValueChange = { password = it; errorMsg = null },
                        label = "密码",
                        placeholder = "请输入密码",
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible }
                    )

                    AnimatedVisibility(
                        visible = !isLogin,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            GlassTextField(
                                value = nickname,
                                onValueChange = { nickname = it },
                                label = "昵称（选填）",
                                placeholder = "请输入昵称"
                            )
                        }
                    }

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    LiquidGlassButton(
                        text = if (isLogin) "登录" else "注册",
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                errorMsg = "请填写用户名和密码"
                                return@LiquidGlassButton
                            }
                            isLoading = true
                            errorMsg = null
                            android.util.Log.e("AuthScreen", "=== Button clicked, isLogin=$isLogin ===")
                            scope.launch {
                                try {
                                    android.util.Log.e("AuthScreen", "Getting API service...")
                                    val api = ApiClient.getService(context)
                                    android.util.Log.e("AuthScreen", "API service obtained")
                                    val response = if (isLogin) {
                                        android.util.Log.e("AuthScreen", "Calling login API...")
                                        api.login(LoginRequest(username, password))
                                    } else {
                                        android.util.Log.e("AuthScreen", "Calling register API...")
                                        api.register(RegisterRequest(username, password, nickname.ifBlank { null }))
                                    }
                                    android.util.Log.e("AuthScreen", "API response: code=${response.code}, message=${response.message}")
                                    if (response.code == 200 && response.data != null) {
                                        ApiClient.setToken(context, response.data.token)
                                        navController.popBackStack()
                                    } else {
                                        errorMsg = response.message
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("AuthScreen", "=== API call FAILED ===", e)
                                    android.util.Log.e("AuthScreen", "Exception type: ${e.javaClass.name}")
                                    android.util.Log.e("AuthScreen", "Exception message: ${e.message}")
                                    errorMsg = "网络错误：${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text(
                    text = if (isLogin) "没有账号？" else "已有账号？",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isLogin) "立即注册" else "去登录",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        isLogin = !isLogin
                        errorMsg = null
                    }
                )
            }
        }
    }
}

@Composable
private fun SimpleLoadingDot(color: Color = MaterialTheme.colorScheme.primary) {
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
            .background(color, CircleShape)
    )
}

@Composable
private fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.35f),
            unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.25f),
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.40f)
        ),
        singleLine = true,
        leadingIcon = leadingIcon,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onTogglePassword?.invoke() }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default
    )
}
