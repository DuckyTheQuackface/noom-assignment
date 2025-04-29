package com.noom.interview.fullstack.sleep.sleeplog.mapper

import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLogEntity
import com.noom.interview.fullstack.sleep.sleeplog.dto.response.SleepLogResponse
import org.springframework.stereotype.Component

@Component
class SleepLogMapper {
    fun toDto(sleepLogEntity: SleepLogEntity): SleepLogResponse {
        return SleepLogResponse(
            id = sleepLogEntity.id,
            sleepDate = sleepLogEntity.sleepDate,
            timeToBed = sleepLogEntity.localTimeToBed,
            timeOutOfBed = sleepLogEntity.localTimeOutOfBed,
            timeZoneId = sleepLogEntity.timeZoneId,
            totalTimeInBedMinutes = sleepLogEntity.totalTimeInBedMinutes,
            feeling = sleepLogEntity.feeling
        )
    }
}
