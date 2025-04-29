package com.noom.interview.fullstack.sleep.sleeplog.dto.response

import com.noom.interview.fullstack.sleep.sleeplog.entity.MorningFeeling
import java.time.LocalDate
import java.time.LocalTime

data class SleepStatsResponse(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val averageTimeInBedMinutes: Int,
    val averageBedTime: LocalTime,
    val averageWakeTime: LocalTime,
    val feelingFrequencies: Map<MorningFeeling, Int>
)
