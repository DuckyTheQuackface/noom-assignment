package com.noom.interview.fullstack.sleep.dto

import com.noom.interview.fullstack.sleep.db.entity.MorningFeeling
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime

/**
 * DTO for creating a new sleep log
 */
data class CreateSleepLogRequest(
    val timeToBed: LocalDateTime,
    val timeOutOfBed: LocalDateTime,
    val feeling: MorningFeeling
)

/**
 * DTO for returning a single sleep log
 */
data class SleepLogResponse(
    val id: Long,
    val sleepDate: LocalDate,
    val timeToBed: LocalDateTime,
    val timeOutOfBed: LocalDateTime,
    val totalTimeInBed: Int, // Duration in minutes
    val feeling: MorningFeeling
)

/**
 * DTO for returning sleep statistics over a 30-day period
 */
data class SleepStatsResponse(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val averageTimeInBed: Int, // Average duration in minutes
    val averageBedTime: LocalTime, // Average time user goes to bed
    val averageWakeTime: LocalTime, // Average time user wakes up
    val feelingFrequencies: Map<MorningFeeling, Int> // Count of each feeling
)
