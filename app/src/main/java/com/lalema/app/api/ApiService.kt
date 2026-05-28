package com.lalema.app.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.*

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("auth/me")
    suspend fun getMe(): ApiResult<UserData>

    @POST("records")
    suspend fun saveRecord(@Body record: SyncRecord): ApiResult<SyncRecord>

    @POST("records/sync")
    suspend fun syncRecords(@Body records: List<SyncRecord>): ApiResult<List<SyncRecord>>

    @GET("records")
    suspend fun getRecords(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResult<PagedRecords>

    @GET("records/date/{date}")
    suspend fun getRecordsByDate(@Path("date") date: String): ApiResult<List<SyncRecord>>

    @GET("records/stats")
    suspend fun getMonthStats(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): ApiResult<MonthStats>

    @DELETE("records/{id}")
    suspend fun deleteRecord(@Path("id") id: Long): ApiResult<Unit>

    @GET("friends/search")
    suspend fun searchFriends(@Query("keyword") keyword: String): ApiResult<List<FriendUser>>

    @POST("friends/request")
    suspend fun sendFriendRequest(@Body body: Map<String, Any>): ApiResult<Unit>

    @POST("friends/accept/{requestId}")
    suspend fun acceptFriendRequest(@Path("requestId") requestId: Long): ApiResult<Unit>

    @POST("friends/reject/{requestId}")
    suspend fun rejectFriendRequest(@Path("requestId") requestId: Long): ApiResult<Unit>

    @DELETE("friends/{friendId}")
    suspend fun removeFriend(@Path("friendId") friendId: Long): ApiResult<Unit>

    @GET("friends/list")
    suspend fun getFriends(): ApiResult<List<FriendUser>>

    @GET("friends/requests")
    suspend fun getFriendRequests(): ApiResult<List<FriendRequestData>>

    @GET("friends/requests/count")
    suspend fun getFriendRequestCount(): ApiResult<Int>

    @GET("friends/leaderboard")
    suspend fun getLeaderboard(): ApiResult<List<LeaderboardItem>>

    @GET("friends/stats/{friendId}")
    suspend fun getFriendStats(@Path("friendId") friendId: Long): ApiResult<FriendStats>
}

data class PagedRecords(
    @SerializedName("records") val records: List<SyncRecord>,
    @SerializedName("total") val total: Long,
    @SerializedName("size") val size: Int,
    @SerializedName("current") val current: Int,
    @SerializedName("pages") val pages: Int
)
