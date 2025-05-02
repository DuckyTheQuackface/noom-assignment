package com.noom.interview.fullstack.sleep.sleeplog.dto.response

import com.noom.interview.fullstack.sleep.sleeplog.entity.MorningFeeling
import java.time.LocalDate
import java.time.LocalTime

data class SleepLogResponse(
    val id: Long,
    val sleepDate: LocalDate,
    val timeToBed: LocalTime,
    val timeOutOfBed: LocalTime,
    val timeZoneId: String,
    val totalTimeInBedMinutes: Int,
    val feeling: MorningFeeling
)
