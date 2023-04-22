package com.example.cpu_usage.proc

data class ProcStat(
    val uTime: Long,
    val sTime: Long,
    val cuTime: Long,
    val csTime: Long,
    val startTime: Long
)
