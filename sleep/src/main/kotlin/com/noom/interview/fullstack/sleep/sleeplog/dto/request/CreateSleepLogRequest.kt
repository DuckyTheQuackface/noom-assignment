package com.noom.interview.fullstack.sleep.sleeplog.dto.request

import com.noom.interview.fullstack.sleep.sleeplog.entity.MorningFeeling
import java.time.LocalDate
import java.time.LocalTime

data class CreateSleepLogRequest(
    val sleepDate: LocalDate,
    val timeToBed: LocalTime,
    val timeOutOfBed: LocalTime,
    val feeling: MorningFeeling,
    val timeZoneId: String? = null // Optional time zone ID for traveling users
)
