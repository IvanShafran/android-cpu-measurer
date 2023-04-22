package com.example.cpu_usage

import androidx.annotation.FloatRange

data class CpuUsageInfo(
    @FloatRange(from = 0.0, to = 1.0) val usagePercent: Float
)
