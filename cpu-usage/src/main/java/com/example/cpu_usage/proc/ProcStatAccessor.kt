package com.example.cpu_usage.proc

import android.system.Os
import android.system.OsConstants
import androidx.annotation.WorkerThread
import java.io.IOException
import java.io.RandomAccessFile

@WorkerThread
class ProcStatAccessor {

    val processorCount by lazy { Os.sysconf(OsConstants._SC_NPROCESSORS_CONF) }
    val clockTickHz by lazy { Os.sysconf(OsConstants._SC_CLK_TCK) }

    fun getProcStat(): ProcStat? {
        val value = getProcStatFileContent() ?: return null
        val tokens = value.split(" ")

        if (tokens.size <= 22) {
            return null
        }

        return try {
            // Docs: https://man7.org/linux/man-pages/man5/proc.5.html
            // /proc/[pid]/stat
            ProcStat(
                uTime = tokens[13].toLong(),
                sTime = tokens[14].toLong(),
                cuTime = tokens[15].toLong(),
                csTime = tokens[16].toLong(),
                startTime = tokens[21].toLong()
            )
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun getProcStatFileContent(): String? {
        try {
            RandomAccessFile("/proc/self/stat", "r").use { file ->
                val fileContent = StringBuilder()
                var line = file.readLine()
                while (line != null) {
                    fileContent.append(line)
                    line = file.readLine()
                }
                return fileContent.toString()
            }
        } catch (ioe: IOException) {
            return null
        }
    }
}
