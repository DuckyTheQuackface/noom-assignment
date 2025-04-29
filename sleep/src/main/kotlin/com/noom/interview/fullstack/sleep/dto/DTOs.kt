package com.noom.interview.fullstack.sleep.dto

import com.noom.interview.fullstack.sleep.db.entity.MorningFeeling
import java.time.LocalDate
import java.time.LocalTime

data class CreateSleepLogRequest(
    val sleepDate: LocalDate,
    val timeToBed: LocalTime,
    val timeOutOfBed: LocalTime,
    val feeling: MorningFeeling,
    val timeZoneId: String? = null // Optional time zone ID for traveling users
)

data class SleepLogResponse(
    val id: Long,
    val sleepDate: LocalDate,
    val timeToBed: LocalTime,
    val timeOutOfBed: LocalTime,
    val timeZoneId: String,
    val totalTimeInBed: Int, // in minutes
    val feeling: MorningFeeling
)

data class SleepStatsResponse(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val averageTimeInBed: Int, // in minutes
    val averageBedTime: LocalTime,
    val averageWakeTime: LocalTime,
    val feelingFrequencies: Map<MorningFeeling, Int>
)
