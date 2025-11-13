package com.wapps1.redcarga.core.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RcLogger {
    private const val DEFAULT_TAG = "RedCarga"
    private const val MAX_BYTES: Long = 2L * 1024 * 1024 // 2MB

    @Volatile private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun init(context: Context) {
        // Guardamos en filesDir/logs/app.log (interno, exportable por adb)
        val dir = File(context.filesDir, "logs")
        if (!dir.exists()) dir.mkdirs()
        logFile = File(dir, "app.log")
        rollIfNeeded()
        d(DEFAULT_TAG, "RcLogger initialized at: ${logFile?.absolutePath}")
    }

    fun d(tag: String = DEFAULT_TAG, message: String) {
        Log.d(tag, message)
        append("D", tag, message, null)
    }

    fun i(tag: String = DEFAULT_TAG, message: String) {
        Log.i(tag, message)
        append("I", tag, message, null)
    }

    fun w(tag: String = DEFAULT_TAG, message: String, t: Throwable? = null) {
        Log.w(tag, message, t)
        append("W", tag, message, t)
    }

    fun e(tag: String = DEFAULT_TAG, message: String, t: Throwable? = null) {
        Log.e(tag, message, t)
        append("E", tag, message, t)
    }

    @Synchronized
    private fun append(level: String, tag: String, message: String, t: Throwable?) {
        try {
            val file = logFile ?: return
            rollIfNeeded()
            FileWriter(file, true).use { fw ->
                val ts = dateFormat.format(Date())
                fw.appendLine("$ts [$level/$tag] $message")
                if (t != null) {
                    fw.appendLine(t.stackTraceToString())
                }
            }
        } catch (_: Throwable) {
            // Evitar crash por fallo de logging
        }
    }

    private fun rollIfNeeded() {
        val file = logFile ?: return
        if (file.exists() && file.length() > MAX_BYTES) {
            val bak = File(file.parentFile, "app.log.bak")
            if (bak.exists()) bak.delete()
            file.renameTo(bak)
            file.writeText("")
        }
    }
}


