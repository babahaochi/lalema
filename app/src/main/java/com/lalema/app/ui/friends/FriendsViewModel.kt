package com.lalema.app.ui.friends

import android.content.Context
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
    val error: String? = null
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
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun searchUsers(keyword: String) {
        viewModelScope.launch {
            try {
                val api = ApiClient.getService(context)
                val result = api.searchFriends(keyword)
                _uiState.value = _uiState.value.copy(searchResults = result.data ?: emptyList())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun sendRequest(receiverId: Long) {
        viewModelScope.launch {
            try {
                val api = ApiClient.getService(context)
                api.sendFriendRequest(mapOf("receiverId" to receiverId))
                _uiState.value = _uiState.value.copy(
                    searchResults = _uiState.value.searchResults.map {
                        if (it.userId == receiverId) it.copy(isFriend = true) else it
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
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
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
