package com.noom.interview.fullstack.sleep.sleeplog.mapper

import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLogEntity
import com.noom.interview.fullstack.sleep.sleeplog.dto.response.SleepLogResponse
import org.springframework.stereotype.Component

@Component
class SleepLogMapper {
    fun toResponse(sleepLog: SleepLogEntity): SleepLogResponse {
        return SleepLogResponse(
            id = sleepLog.id,
            sleepDate = sleepLog.sleepDate,
            timeToBed = sleepLog.localTimeToBed,
            timeOutOfBed = sleepLog.localTimeOutOfBed,
            timeZoneId = sleepLog.timeZoneId,
            totalTimeInBedMinutes = sleepLog.totalTimeInBedMinutes,
            feeling = sleepLog.feeling
        )
    }
}
