package com.example.cpu_usage

import android.os.SystemClock
import androidx.annotation.WorkerThread
import com.example.cpu_usage.proc.ProcStat
import com.example.cpu_usage.proc.ProcStatAccessor
import java.util.concurrent.TimeUnit

// Based on: https://eng.lyft.com/monitoring-cpu-performance-of-lyfts-android-applications-4e36fafffe12
@WorkerThread
class CpuUsageMeasurer {

    private data class State(
        val uptime: Long,
        val procStat: ProcStat
    )

    private val procStatAccessor = ProcStatAccessor()
    private val clockTickHzFloat by lazy { procStatAccessor.clockTickHz.toFloat() }
    private var state: State? = null

    /**
     * Returns CPU usage since last [getInfo] call.
     * First time returns null.
     */
    fun getInfo(): CpuUsageInfo? {
        if (!sanityCheck()) {
            return null
        }

        val previousState = state
        val state = getState()
        this.state = getState()

        return if (previousState == null || state == null) {
            null
        } else {
            return calculateCpuUsageInfo(previousState, state)
        }
    }

    private fun sanityCheck(): Boolean {
        return procStatAccessor.clockTickHz > 0 && procStatAccessor.processorCount > 0
    }

    private fun getState(): State? {
        val procStat = procStatAccessor.getProcStat() ?: return null
        return State(
            uptime = TimeUnit.MILLISECONDS.toSeconds(SystemClock.elapsedRealtime()),
            procStat = procStat
        )
    }

    private fun calculateCpuUsageInfo(
        previousState: State,
        state: State
    ): CpuUsageInfo? {
        val cpuTimeDelta = getCpuTimeSec(state) - getCpuTimeSec(previousState)
        val processTimeDelta = getProcessTimeSec(state) - getProcessTimeSec(previousState)

        return if (processTimeDelta.isFinite() && processTimeDelta != 0f) {
            val cpuUsageTotal = cpuTimeDelta / processTimeDelta
            CpuUsageInfo(cpuUsageTotal / procStatAccessor.processorCount)
        } else {
            null
        }
    }

    private fun getCpuTimeSec(state: State): Float {
        val procStat = state.procStat
        return (procStat.csTime + procStat.cuTime + procStat.sTime + procStat.uTime) / clockTickHzFloat
    }

    private fun getProcessTimeSec(state: State): Float {
        return state.uptime - (state.procStat.startTime / clockTickHzFloat)
    }
}
