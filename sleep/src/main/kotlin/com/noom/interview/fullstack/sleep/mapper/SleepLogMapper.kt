package com.noom.interview.fullstack.sleep.mapper

import com.noom.interview.fullstack.sleep.db.entity.SleepLog
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class SleepLogMapper {
    fun toDto(sleepLog: SleepLog, zone: ZoneId): SleepLogResponse {
        return SleepLogResponse(
            id = sleepLog.id,
            sleepDate = sleepLog.sleepDate,
            timeToBed = sleepLog.timeToBed.atZoneSameInstant(zone).toLocalDateTime(),
            timeOutOfBed = sleepLog.timeOutOfBed.atZoneSameInstant(zone).toLocalDateTime(),
            totalTimeInBed = sleepLog.totalTimeInBed,
            feeling = sleepLog.feeling
        )
    }
}