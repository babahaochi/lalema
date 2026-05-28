package com.lalema.app.api

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("nickname") val nickname: String? = null
)

data class AuthResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: AuthData?
)

data class AuthData(
    @SerializedName("token") val token: String,
    @SerializedName("userId") val userId: Long,
    @SerializedName("username") val username: String,
    @SerializedName("nickname") val nickname: String
)

data class ApiResult<T>(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T?
)

data class UserData(
    @SerializedName("userId") val userId: Long,
    @SerializedName("username") val username: String,
    @SerializedName("nickname") val nickname: String
)

data class SyncRecord(
    @SerializedName("localId") val localId: Long? = null,
    @SerializedName("date") val date: String,
    @SerializedName("timeHour") val timeHour: Int,
    @SerializedName("timeMinute") val timeMinute: Int,
    @SerializedName("amount") val amount: String,
    @SerializedName("consistency") val consistency: String,
    @SerializedName("color") val color: String,
    @SerializedName("smell") val smell: String,
    @SerializedName("painLevel") val painLevel: Int,
    @SerializedName("blood") val blood: Boolean,
    @SerializedName("mucus") val mucus: Boolean,
    @SerializedName("notes") val notes: String
)

data class MonthStats(
    @SerializedName("recordDays") val recordDays: Int,
    @SerializedName("totalRecords") val totalRecords: Int,
    @SerializedName("daysInMonth") val daysInMonth: Int,
    @SerializedName("daysPassed") val daysPassed: Int,
    @SerializedName("checkInRate") val checkInRate: Double,
    @SerializedName("mostCommonHour") val mostCommonHour: Int?
)

data class FriendUser(
    @SerializedName("userId") val userId: Long,
    @SerializedName("username") val username: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("isFriend") val isFriend: Boolean = false,
    @SerializedName("remark") val remark: String = ""
)

data class FriendRequestData(
    @SerializedName("requestId") val requestId: Long,
    @SerializedName("senderId") val senderId: Long,
    @SerializedName("username") val username: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("message") val message: String?,
    @SerializedName("createdAt") val createdAt: String
)

data class LeaderboardItem(
    @SerializedName("userId") val userId: Long,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("monthRecords") val monthRecords: Int,
    @SerializedName("isMe") val isMe: Boolean
)

data class FriendStats(
    @SerializedName("userId") val userId: Long,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("monthRecords") val monthRecords: Int,
    @SerializedName("totalRecords") val totalRecords: Int
)
