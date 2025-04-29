package com.noom.interview.fullstack.sleep.mapper

import com.noom.interview.fullstack.sleep.db.entity.SleepLog
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import org.springframework.stereotype.Component

@Component
class SleepLogMapper {

    fun toDto(sleepLog: SleepLog): SleepLogResponse {
        return SleepLogResponse(
            id = sleepLog.id,
            sleepDate = sleepLog.sleepDate,
            timeToBed = sleepLog.localTimeToBed,
            timeOutOfBed = sleepLog.localTimeOutOfBed,
            totalTimeInBed = sleepLog.totalTimeInBed,
            feeling = sleepLog.feeling
        )
    }
}