package com.lalema.app.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.lalema.app.data.PoopAmount
import com.lalema.app.data.PoopColor
import com.lalema.app.data.PoopConsistency
import com.lalema.app.data.PoopRecord
import com.lalema.app.data.PoopSmell
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DataExportManager {

    fun exportToCsv(records: List<PoopRecord>): String {
        val sb = StringBuilder()
        sb.appendLine("日期,时间,量,稠度,颜色,气味,疼痛等级,便血,粘液,备注")
        records.forEach { r ->
            val amountLabel = PoopAmount.valueOf(r.amount).displayName
            val consistencyLabel = PoopConsistency.valueOf(r.consistency).displayName
            val colorLabel = PoopColor.valueOf(r.color).displayName
            val smellLabel = PoopSmell.valueOf(r.smell).displayName
            sb.appendLine(
                "${r.date},${String.format("%02d:%02d", r.timeHour, r.timeMinute)}," +
                        "$amountLabel,$consistencyLabel,$colorLabel,$smellLabel," +
                        "${r.painLevel},${if (r.blood) "是" else "否"}," +
                        "${if (r.mucus) "是" else "否"},${r.notes.replace(",", "，")}"
            )
        }
        return sb.toString()
    }

    fun exportToJson(records: List<PoopRecord>): String {
        val sb = StringBuilder()
        sb.appendLine("{")
        sb.appendLine("  \"exportTime\": \"${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\",")
        sb.appendLine("  \"totalRecords\": ${records.size},")
        sb.appendLine("  \"records\": [")
        records.forEachIndexed { index, r ->
            val comma = if (index < records.size - 1) "," else ""
            sb.appendLine("    {")
            sb.appendLine("      \"date\": \"${r.date}\",")
            sb.appendLine("      \"time\": \"${String.format("%02d:%02d", r.timeHour, r.timeMinute)}\",")
            sb.appendLine("      \"amount\": \"${PoopAmount.valueOf(r.amount).displayName}\",")
            sb.appendLine("      \"consistency\": \"${PoopConsistency.valueOf(r.consistency).displayName}\",")
            sb.appendLine("      \"color\": \"${PoopColor.valueOf(r.color).displayName}\",")
            sb.appendLine("      \"smell\": \"${PoopSmell.valueOf(r.smell).displayName}\",")
            sb.appendLine("      \"painLevel\": ${r.painLevel},")
            sb.appendLine("      \"blood\": ${r.blood},")
            sb.appendLine("      \"mucus\": ${r.mucus},")
            sb.appendLine("      \"notes\": \"${r.notes.replace("\"", "\\\"")}\"")
            sb.appendLine("    }$comma")
        }
        sb.appendLine("  ]")
        sb.append("}")
        return sb.toString()
    }

    fun saveToFile(context: Context, content: String, fileName: String, mimeType: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveWithMediaStore(context, content, fileName, mimeType)
            } else {
                saveLegacy(context, content, fileName)
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun saveWithMediaStore(context: Context, content: String, fileName: String, mimeType: String): Boolean {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/LaLeMa")
        }
        val uri = context.contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            contentValues
        ) ?: return false
        context.contentResolver.openOutputStream(uri)?.use { os ->
            os.write(content.toByteArray(Charsets.UTF_8))
        }
        return true
    }

    @Suppress("DEPRECATION")
    private fun saveLegacy(context: Context, content: String, fileName: String): Boolean {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "LaLeMa"
        )
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        FileOutputStream(file).use { fos ->
            fos.write(content.toByteArray(Charsets.UTF_8))
        }
        return true
    }
}
