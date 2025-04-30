package com.noom.interview.fullstack.sleep.sleeplog.usecase

import com.noom.interview.fullstack.sleep.sleeplog.dto.request.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.sleeplog.dto.response.SleepLogResponse
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLogEntity
import com.noom.interview.fullstack.sleep.sleeplog.mapper.SleepLogMapper
import com.noom.interview.fullstack.sleep.sleeplog.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.user.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Service
class CreateSleepLogUseCase(
    private val sleepLogRepository: SleepLogRepository,
    private val sleepLogMapper: SleepLogMapper,
    private val userService: UserService
) {
    @Transactional
    fun createSleepLog(userId: Long, request: CreateSleepLogRequest): SleepLogResponse {
        val user = userService.getUserById(userId)

        val timeZoneId = request.timeZoneId ?: user.timeZone
        val zoneId = ZoneId.of(timeZoneId)

        val totalTimeInBed = calculateTotalTimeInBed(request.timeToBed, request.timeOutOfBed)
        val (utcBedTime, utcWakeTime) = getUtcBedAndWakeTimes(request, zoneId, totalTimeInBed)

        val sleepLog = SleepLogEntity(
            user = user,
            sleepDate = request.sleepDate,
            localTimeToBed = request.timeToBed,
            localTimeOutOfBed = request.timeOutOfBed,
            utcTimeToBed = utcBedTime,
            utcTimeOutOfBed = utcWakeTime,
            timeZoneId = timeZoneId,
            totalTimeInBedMinutes = totalTimeInBed,
            feeling = request.feeling
        )

        val savedSleepLog = sleepLogRepository.save(sleepLog)

        // Convert to DTO and return
        // We need to ensure all fields are correctly preserved in the mapping
        return sleepLogMapper.toResponse(savedSleepLog)
    }

    // Assuming client is allowing user to enter one time for each night.
    // User can in theory go to sleep before or after midnight,
    // but can as well wake up before or after midnight.
    // We are relying on client to send sleepDate as start of the night for which
    // user is entering sleeping log.
    // Day 1 of logging -> Night of 05.08., went to sleep at 23:00, woke up at 07:00
    // Day 2 of logging -> Night of 06.08., went to sleep at 01:00, woke up at 08:00
    // Day 3 of logging -> Night of 07.08., went to sleep at 00:00, woke up at 07:00
    // Day 4 of logging -> Night of 08.08., went to sleep at 22:00, woke up at 07:00
    // eg. "Night of 05.08." represents sleep on 05.08., that should usually start at 23:00 and end at 07:00
    private fun getUtcBedAndWakeTimes(
        request: CreateSleepLogRequest,
        zoneId: ZoneId,
        totalTimeInBed: Int
    ): Pair<OffsetDateTime, OffsetDateTime> {
        val bedDateTime = if (
            request.timeToBed.isAfter(LocalTime.NOON) &&
            request.timeToBed != LocalTime.MIDNIGHT
        ) {
            // <12:00-00:00>
            ZonedDateTime.of(request.sleepDate, request.timeToBed, zoneId)
        } else {
            // [00:00-12:00]
            ZonedDateTime.of(request.sleepDate.plusDays(1), request.timeToBed, zoneId)
        }

        val utcBedTime = bedDateTime.toOffsetDateTime()
        val utcWakeTime = utcBedTime.plusMinutes(totalTimeInBed.toLong())

        return utcBedTime to utcWakeTime
    }

    private fun calculateTotalTimeInBed(bedTime: LocalTime, wakeTime: LocalTime): Int {
        var minutes = ChronoUnit.MINUTES.between(bedTime, wakeTime).toInt()

        if (minutes < 0) {
            minutes += 24 * 60 // Add 24 hours in minutes
        }

        if (minutes == 0 && bedTime == wakeTime) {
            minutes = 24 * 60 // Assume 24 hours if both times are identical
        }

        return minutes
    }
}