package com.lalema.app.ui.friends

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalema.app.api.ApiClient
import com.lalema.app.api.FriendRequestData
import com.lalema.app.api.FriendUser
import com.lalema.app.api.LeaderboardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendsUiState(
    val friends: List<FriendUser> = emptyList(),
    val requests: List<FriendRequestData> = emptyList(),
    val leaderboard: List<LeaderboardItem> = emptyList(),
    val searchResults: List<FriendUser> = emptyList(),
    val pendingCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val api = ApiClient.getService(context)
                val friendsResult = api.getFriends()
                val requestsResult = api.getFriendRequests()
                val countResult = api.getFriendRequestCount()
                val leaderboardResult = api.getLeaderboard()
                _uiState.value = _uiState.value.copy(
                    friends = friendsResult.data ?: emptyList(),
                    requests = requestsResult.data ?: emptyList(),
                    leaderboard = leaderboardResult.data ?: emptyList(),
                    pendingCount = countResult.data ?: 0,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "loadData failed", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun searchUsers(keyword: String) {
        viewModelScope.launch {
            try {
                val api = ApiClient.getService(context)
                val result = api.searchFriends(keyword)
                Log.d("FriendsViewModel", "searchUsers result: code=${result.code}, data size=${result.data?.size}")
                _uiState.value = _uiState.value.copy(searchResults = result.data ?: emptyList())
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "searchUsers failed", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun sendRequest(receiverId: Long) {
        viewModelScope.launch {
            try {
                Log.d("FriendsViewModel", "sendRequest called with receiverId=$receiverId")
                val api = ApiClient.getService(context)
                val body = mapOf("receiverId" to receiverId)
                Log.d("FriendsViewModel", "sendRequest body: $body")
                val result = api.sendFriendRequest(body)
                Log.d("FriendsViewModel", "sendRequest result: code=${result.code}, message=${result.message}")
                if (result.code == 200) {
                    _uiState.value = _uiState.value.copy(
                        searchResults = _uiState.value.searchResults.map {
                            if (it.userId == receiverId) it.copy(requestSent = true) else it
                        },
                        message = "好友请求已发送"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(error = result.message ?: "发送失败")
                }
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "sendRequest failed", e)
                _uiState.value = _uiState.value.copy(error = "网络错误: ${e.message}")
            }
        }
    }

    fun acceptRequest(requestId: Long) {
        viewModelScope.launch {
            try {
                val api = ApiClient.getService(context)
                api.acceptFriendRequest(requestId)
                loadData()
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "acceptRequest failed", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun rejectRequest(requestId: Long) {
        viewModelScope.launch {
            try {
                val api = ApiClient.getService(context)
                api.rejectFriendRequest(requestId)
                loadData()
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "rejectRequest failed", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun removeFriend(friendId: Long) {
        viewModelScope.launch {
            try {
                val api = ApiClient.getService(context)
                api.removeFriend(friendId)
                loadData()
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "removeFriend failed", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun remindFriend(friendId: Long) {
        viewModelScope.launch {
            try {
                val api = ApiClient.getService(context)
                val result = api.remindFriend(friendId)
                if (result.code == 200) {
                    _uiState.value = _uiState.value.copy(message = "已发送提醒")
                } else {
                    _uiState.value = _uiState.value.copy(error = result.message ?: "提醒失败")
                }
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "remindFriend failed", e)
                _uiState.value = _uiState.value.copy(error = e.message ?: "网络错误")
            }
        }
    }
}
