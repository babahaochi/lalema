package com.lalema.app.ui.friends

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lalema.app.api.ApiClient
import com.lalema.app.api.FriendRequestData
import com.lalema.app.api.FriendUser
import com.lalema.app.api.LeaderboardItem
import com.lalema.app.ui.theme.GlassMotion
import com.lalema.app.ui.theme.LiquidGlassCard
import com.lalema.app.ui.theme.LiquidGlassIconButton
import com.lalema.app.ui.theme.LiquidGlassSurface
import com.lalema.app.ui.theme.LiquidGlassTextField
import com.lalema.app.ui.theme.glassContentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    navController: NavController,
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val searchQueryFlow = remember { MutableStateFlow("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val isLoggedIn = remember { ApiClient.isLoggedIn(context) }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            android.widget.Toast.makeText(context, "请先登录", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(searchQueryFlow) {
        searchQueryFlow
            .debounce(500)
            .filter { it.isNotBlank() }
            .collect { query ->
                viewModel.searchUsers(query)
            }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "好友",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    if (uiState.pendingCount > 0) {
                        LiquidGlassSurface(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(36.dp)
                                .clickable { selectedTab = 1 },
                            cornerRadius = 18.dp,
                            tint = Color(0xFFFF5252),
                            contentPadding = 8.dp,
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${uiState.pendingCount}",
                                color = glassContentColor(Color(0xFFFF5252)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("好友", "请求", "排行").forEachIndexed { index, label ->
                    val selected = selectedTab == index
                    val primaryColor = MaterialTheme.colorScheme.primary
                    LiquidGlassSurface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = index },
                        cornerRadius = 12.dp,
                        tint = if (selected) primaryColor else null,
                        contentPadding = 0.dp,
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) glassContentColor(primaryColor) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> FriendsListTab(
                    friends = uiState.friends,
                    isLoading = uiState.isLoading,
                    onRemove = { viewModel.removeFriend(it) },
                    onRemind = { viewModel.remindFriend(it) },
                    onSearch = { query ->
                        searchQueryFlow.value = query
                    },
                    searchResults = uiState.searchResults,
                    onSendRequest = { userId -> viewModel.sendRequest(userId) },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )
                1 -> RequestsTab(
                    requests = uiState.requests,
                    isLoading = uiState.isLoading,
                    onAccept = { viewModel.acceptRequest(it) },
                    onReject = { viewModel.rejectRequest(it) }
                )
                2 -> LeaderboardTab(
                    leaderboard = uiState.leaderboard,
                    isLoading = uiState.isLoading
                )
            }
        }
    }
}

@Composable
private fun FriendsListTab(
    friends: List<FriendUser>,
    isLoading: Boolean,
    onRemove: (Long) -> Unit,
    onRemind: (Long) -> Unit,
    onSearch: (String) -> Unit,
    searchResults: List<FriendUser>,
    onSendRequest: (Long) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    var showSearch by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LiquidGlassTextField(
                value = searchQuery,
                onValueChange = {
                    onSearchQueryChange(it)
                    if (it.isNotBlank()) {
                        showSearch = true
                        onSearch(it)
                    }
                },
                placeholder = "搜索用户名或昵称",
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            LiquidGlassSurface(
                modifier = Modifier
                    .size(48.dp)
                    .clickable {
                        if (searchQuery.isNotBlank()) {
                            showSearch = true
                            onSearch(searchQuery)
                        }
                    },
                cornerRadius = 12.dp,
                tint = MaterialTheme.colorScheme.primary,
                contentPadding = 0.dp,
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = glassContentColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (showSearch && searchResults.isNotEmpty()) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "搜索结果",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    searchResults.forEach { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LiquidGlassSurface(
                                modifier = Modifier.size(36.dp),
                                cornerRadius = 18.dp,
                                tint = MaterialTheme.colorScheme.primary,
                                contentPadding = 0.dp,
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.nickname.take(1),
                                    color = glassContentColor(MaterialTheme.colorScheme.primary),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.nickname,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "@${user.username}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            when {
                                user.isFriend -> {
                                    Text(
                                        text = "已是好友",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                user.requestSent -> {
                                    Text(
                                        text = "已发送",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                else -> {
                                    LiquidGlassSurface(
                                        modifier = Modifier.clickable { onSendRequest(user.userId) },
                                        cornerRadius = 8.dp,
                                        tint = MaterialTheme.colorScheme.primary,
                                        contentPadding = 0.dp,
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "添加",
                                            fontSize = 12.sp,
                                            color = glassContentColor(MaterialTheme.colorScheme.primary),
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                SimpleLoadingIndicator()
            }
        } else if (friends.isEmpty()) {
            AnimatedVisibility(
                visible = !isLoading,
                enter = fadeIn(animationSpec = tween(GlassMotion.DURATION_SLOW)) + slideInVertically(
                    animationSpec = GlassMotion.enter(),
                    initialOffsetY = { 30 }
                ),
                exit = fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "还没有好友",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "搜索用户名添加好友",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(friends, key = { it.userId }) { friend ->
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(GlassMotion.DURATION_MEDIUM)) + slideInVertically(
                            animationSpec = GlassMotion.enter(),
                            initialOffsetY = { 20 }
                        )
                    ) {
                        FriendItem(friend = friend, onRemove = { onRemove(friend.userId) }, onRemind = { onRemind(friend.userId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendItem(friend: FriendUser, onRemove: () -> Unit, onRemind: () -> Unit) {
    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidGlassSurface(
                modifier = Modifier.size(40.dp),
                cornerRadius = 20.dp,
                tint = MaterialTheme.colorScheme.primary,
                contentPadding = 0.dp,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.nickname.take(1),
                    color = glassContentColor(MaterialTheme.colorScheme.primary),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.remark?.ifBlank { null } ?: friend.nickname,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "@${friend.username}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LiquidGlassSurface(
                modifier = Modifier.clickable { onRemind() },
                cornerRadius = 8.dp,
                tint = MaterialTheme.colorScheme.secondary,
                contentPadding = 0.dp,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "提醒",
                    fontSize = 12.sp,
                    color = glassContentColor(MaterialTheme.colorScheme.secondary),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            LiquidGlassIconButton(
                icon = Icons.Default.Close,
                contentDescription = "删除",
                onClick = onRemove,
                size = 32.dp,
                cornerRadius = 16.dp,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun RequestsTab(
    requests: List<FriendRequestData>,
    isLoading: Boolean,
    onAccept: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SimpleLoadingIndicator()
        }
    } else if (requests.isEmpty()) {
        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(animationSpec = tween(GlassMotion.DURATION_SLOW)) + slideInVertically(
                animationSpec = GlassMotion.enter(),
                initialOffsetY = { 30 }
            ),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "暂无好友请求",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(requests) { request ->
                RequestItem(
                    request = request,
                    onAccept = { onAccept(request.requestId) },
                    onReject = { onReject(request.requestId) }
                )
            }
        }
    }
}

@Composable
private fun RequestItem(
    request: FriendRequestData,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidGlassSurface(
                modifier = Modifier.size(40.dp),
                cornerRadius = 20.dp,
                tint = Color(0xFFFF9800),
                contentPadding = 0.dp,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = request.nickname.take(1),
                    color = glassContentColor(Color(0xFFFF9800)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.nickname.ifBlank { request.username },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!request.message.isNullOrBlank()) {
                    Text(
                        text = request.message,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LiquidGlassSurface(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onAccept),
                    cornerRadius = 16.dp,
                    tint = Color(0xFF4CAF50),
                    contentPadding = 0.dp,
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "接受",
                        tint = glassContentColor(Color(0xFF4CAF50)),
                        modifier = Modifier.size(18.dp)
                    )
                }
                LiquidGlassSurface(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onReject),
                    cornerRadius = 16.dp,
                    tint = Color(0xFFFF5252),
                    contentPadding = 0.dp,
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "拒绝",
                        tint = glassContentColor(Color(0xFFFF5252)),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardTab(
    leaderboard: List<LeaderboardItem>,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SimpleLoadingIndicator()
        }
    } else if (leaderboard.isEmpty()) {
        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(animationSpec = tween(GlassMotion.DURATION_SLOW)) + slideInVertically(
                animationSpec = GlassMotion.enter(),
                initialOffsetY = { 30 }
            ),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "暂无排行数据",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(leaderboard.size) { index ->
                LeaderboardItemRow(
                    rank = index + 1,
                    item = leaderboard[index]
                )
            }
        }
    }
}

@Composable
private fun LeaderboardItemRow(rank: Int, item: LeaderboardItem) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = if (item.isMe) 16.dp else 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidGlassSurface(
                modifier = Modifier.size(32.dp),
                cornerRadius = 16.dp,
                tint = if (rank <= 3) rankColor else null,
                contentPadding = 0.dp,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (rank <= 3) glassContentColor(rankColor) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = item.nickname,
                fontWeight = if (item.isMe) FontWeight.Bold else FontWeight.Medium,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (item.isMe) {
                LiquidGlassSurface(
                    cornerRadius = 6.dp,
                    tint = MaterialTheme.colorScheme.primary,
                    contentPadding = 0.dp,
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "我",
                        fontSize = 11.sp,
                        color = glassContentColor(MaterialTheme.colorScheme.primary),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = "${item.monthRecords}次",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SimpleLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(GlassMotion.DURATION_LOADING),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(GlassMotion.DURATION_LOADING),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
    }
}
