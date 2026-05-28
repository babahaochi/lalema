package com.lalema.app.data

import android.content.Context
import com.lalema.app.api.ApiClient
import com.lalema.app.api.SyncRecord
import com.lalema.app.domain.PoopRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SyncManager {

    suspend fun syncToServer(context: Context, repository: PoopRepository): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                if (!ApiClient.isLoggedIn(context)) {
                    return@withContext Result.failure(Exception("未登录"))
                }
                val api = ApiClient.getService(context)
                val allRecords = repository.getAll()
                val syncRecords = allRecords.map { record ->
                    SyncRecord(
                        localId = record.id,
                        date = record.date,
                        timeHour = record.timeHour,
                        timeMinute = record.timeMinute,
                        amount = record.amount,
                        consistency = record.consistency,
                        color = record.color,
                        smell = record.smell,
                        painLevel = record.painLevel,
                        blood = record.blood,
                        mucus = record.mucus,
                        notes = record.notes
                    )
                }
                if (syncRecords.isEmpty()) {
                    return@withContext Result.success(0)
                }
                val response = api.syncRecords(syncRecords)
                if (response.code == 200) {
                    Result.success(syncRecords.size)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
