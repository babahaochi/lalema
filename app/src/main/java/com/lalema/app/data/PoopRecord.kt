package com.lalema.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(
    tableName = "poop_records",
    indices = [Index(value = ["date"])]
)
data class PoopRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "date")
    val date: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "time_hour")
    val timeHour: Int = LocalTime.now().hour,
    @ColumnInfo(name = "time_minute")
    val timeMinute: Int = LocalTime.now().minute,
    @ColumnInfo(name = "amount")
    val amount: String = "NORMAL",
    @ColumnInfo(name = "consistency")
    val consistency: String = "NORMAL",
    @ColumnInfo(name = "color")
    val color: String = "BROWN",
    @ColumnInfo(name = "smell")
    val smell: String = "NORMAL",
    @ColumnInfo(name = "pain_level")
    val painLevel: Int = 0,
    @ColumnInfo(name = "blood")
    val blood: Boolean = false,
    @ColumnInfo(name = "mucus")
    val mucus: Boolean = false,
    @ColumnInfo(name = "notes")
    val notes: String = ""
) {
    companion object {
        fun create(
            date: String,
            timeHour: Int = LocalTime.now().hour,
            timeMinute: Int = LocalTime.now().minute,
            amount: String = "NORMAL",
            consistency: String = "NORMAL",
            color: String = "BROWN",
            smell: String = "NORMAL",
            painLevel: Int = 0,
            blood: Boolean = false,
            mucus: Boolean = false,
            notes: String = ""
        ): PoopRecord {
            return PoopRecord(
                date = date,
                createdAt = System.currentTimeMillis(),
                timeHour = timeHour,
                timeMinute = timeMinute,
                amount = amount,
                consistency = consistency,
                color = color,
                smell = smell,
                painLevel = painLevel,
                blood = blood,
                mucus = mucus,
                notes = notes
            )
        }
    }
}

enum class PoopAmount(val displayName: String) {
    SMALL("少量"),
    NORMAL("正常"),
    LARGE("大量")
}

enum class PoopConsistency(val displayName: String) {
    VERY_HARD("非常干"),
    HARD("较干"),
    NORMAL("正常"),
    SOFT("偏软"),
    VERY_SOFT("很软"),
    LIQUID("稀便")
}

enum class PoopColor(val displayName: String, val colorHex: String) {
    BROWN("棕色", "#8B4513"),
    DARK_BROWN("深棕色", "#5D4037"),
    LIGHT_BROWN("浅棕色", "#A0522D"),
    GREEN("绿色", "#228B22"),
    BLACK("黑色", "#1a1a1a"),
    RED("红色", "#DC143C"),
    YELLOW("黄色", "#FFD700"),
    GRAY("灰白色", "#808080")
}

enum class PoopSmell(val displayName: String) {
    NORMAL("正常"),
    SLIGHT("稍有气味"),
    STRONG("气味较重"),
    VERY_STRONG("非常臭")
}

enum class PainLevel(val displayName: String) {
    NONE("无疼痛"),
    MILD("轻微"),
    MODERATE("中等"),
    SEVERE("严重")
}
