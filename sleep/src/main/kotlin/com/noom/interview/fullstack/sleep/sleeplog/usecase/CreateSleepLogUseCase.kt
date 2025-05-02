package com.noom.interview.fullstack.sleep.sleeplog.usecase

import com.noom.interview.fullstack.sleep.sleeplog.dto.request.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.sleeplog.dto.response.SleepLogResponse
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLogEntity
import com.noom.interview.fullstack.sleep.sleeplog.exception.InvalidSleepDuration
import com.noom.interview.fullstack.sleep.sleeplog.exception.TimeDoesNotExist
import com.noom.interview.fullstack.sleep.sleeplog.mapper.SleepLogMapper
import com.noom.interview.fullstack.sleep.sleeplog.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.user.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.Duration
import java.time.LocalDate

@Service
class CreateSleepLogUseCase(
    private val sleepLogRepository: SleepLogRepository,
    private val sleepLogMapper: SleepLogMapper,
    private val userService: UserService
) {
    companion object {
        private const val MINUTES_IN_HOUR = 60
        private const val MINIMUM_SLEEP_TIME_MINUTES = 2 * MINUTES_IN_HOUR
        private const val MAXIMUM_SLEEP_TIME_MINUTES = 16 * MINUTES_IN_HOUR
    }

    @Transactional
    fun createSleepLog(userId: Long, request: CreateSleepLogRequest): SleepLogResponse {
        val user = userService.getUserById(userId)
        val timeZoneId = request.timeZoneId ?: user.timeZone
        val zoneId = ZoneId.of(timeZoneId)
        val (utcBedTime, utcWakeTime) = getUtcBedAndWakeTimes(request, zoneId)
        val totalTimeInBed = calculateSleepDurationFromUtcTimes(utcBedTime, utcWakeTime)
        validateSleepDuration(totalTimeInBed)

        val sleepLog = sleepLogRepository.save(
            SleepLogEntity(
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
        )

        return sleepLogMapper.toResponse(sleepLog)
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
    // eg. "Night of 05.08." represents night of August 5th to August 6th
    private fun getUtcBedAndWakeTimes(
        request: CreateSleepLogRequest,
        zoneId: ZoneId
    ): Pair<OffsetDateTime, OffsetDateTime> {
        val bedDate = if (
            request.timeToBed.isAfter(LocalTime.NOON) &&
            request.timeToBed != LocalTime.MIDNIGHT
        ) {
            request.sleepDate
        } else {
            request.sleepDate.plusDays(1)
        }

        val wakeDate = if (
            request.timeOutOfBed.isBefore(request.timeToBed) &&
            request.timeToBed.isAfter(LocalTime.NOON) &&
            request.timeToBed != LocalTime.MIDNIGHT
        ) {
            bedDate.plusDays(1)
        } else {
            bedDate
        }

        val bedZonedDateTime = resolveZonedDateTimeWithDST(bedDate, request.timeToBed, zoneId)
        val wakeZonedDateTime = resolveZonedDateTimeWithDST(wakeDate, request.timeOutOfBed, zoneId)

        return bedZonedDateTime.toOffsetDateTime() to wakeZonedDateTime.toOffsetDateTime()
    }

    private fun resolveZonedDateTimeWithDST(
        date: LocalDate,
        time: LocalTime,
        zoneId: ZoneId
    ): ZonedDateTime {
        val localDateTime = date.atTime(time)
        val zoneRules = zoneId.rules
        val validOffsets = zoneRules.getValidOffsets(localDateTime)

        return when {
            validOffsets.isEmpty() -> {
                throw TimeDoesNotExist(time)
            }
            validOffsets.size == 1 -> {
                ZonedDateTime.ofStrict(localDateTime, validOffsets[0], zoneId)
            }
            else -> {
                ZonedDateTime.ofStrict(localDateTime, validOffsets.first(), zoneId)
            }
        }
    }


    private fun calculateSleepDurationFromUtcTimes(
        bedTime: OffsetDateTime,
        wakeTime: OffsetDateTime
    ): Int {
        val durationMinutes = Duration.between(bedTime, wakeTime).toMinutes().toInt()
        return durationMinutes
    }

    private fun validateSleepDuration(totalTimeInBedMinutes: Int) {
        if (totalTimeInBedMinutes < MINIMUM_SLEEP_TIME_MINUTES) {
            throw InvalidSleepDuration(
                "Sleep duration must be at least ${MINIMUM_SLEEP_TIME_MINUTES / 60} hours"
            )
        }

        if (totalTimeInBedMinutes > MAXIMUM_SLEEP_TIME_MINUTES) {
            throw InvalidSleepDuration(
                "Sleep duration cannot exceed ${MAXIMUM_SLEEP_TIME_MINUTES / 60} hours"
            )
        }
    }
}