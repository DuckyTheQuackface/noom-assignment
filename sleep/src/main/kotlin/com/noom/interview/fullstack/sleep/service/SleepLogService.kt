package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.configuration.ResourceNotFoundException
import com.noom.interview.fullstack.sleep.db.entity.SleepLog
import com.noom.interview.fullstack.sleep.db.repositorty.SleepLogRepository
import com.noom.interview.fullstack.sleep.db.repositorty.UserRepository
import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.dto.SleepStatsResponse
import com.noom.interview.fullstack.sleep.mapper.SleepLogMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Service
class SleepLogService(
    private val sleepLogRepository: SleepLogRepository,
    private val userRepository: UserRepository,
    private val sleepLogMapper: SleepLogMapper
) {

    @Transactional
    fun createSleepLog(userId: Long, request: CreateSleepLogRequest): SleepLogResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }

        // Extract the date in the user's time zone from the timeToBed
        val sleepDate = request.timeToBed.toLocalDate()

        // Format timeToBed and timeOutOfBed with the user's time zone
        val zonedTimeToBad: ZonedDateTime = request.timeToBed.atZone(ZoneId.of(user.timeZone))
        val offsetTimeToBad = zonedTimeToBad.toOffsetDateTime()
        val zonedTimeOutOfBad = request.timeOutOfBed.atZone(ZoneId.of(user.timeZone))
        val offsetTimeOutOfBad = zonedTimeOutOfBad.toOffsetDateTime()

        val sleepLog = SleepLog(
            user = user,
            sleepDate = sleepDate,
            timeToBed = offsetTimeToBad,
            timeOutOfBed = offsetTimeOutOfBad,
            totalTimeInBed = 0, // TODO: Calculated
            feeling = request.feeling
        )

        // Save to database
        val savedSleepLog = sleepLogRepository.save(sleepLog)

        // Convert to DTO and return
        return sleepLogMapper.toDto(savedSleepLog, ZoneId.of(user.timeZone))
    }

    @Transactional(readOnly = true)
    fun getLatestSleepLog(userId: Long): SleepLogResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }

        val latestSleepLog = sleepLogRepository.findFirstByUserIdOrderBySleepDateDesc(userId)
            .orElseThrow { ResourceNotFoundException("No sleep logs found for user with id: $userId") }

        return sleepLogMapper.toDto(latestSleepLog, ZoneId.of(user.timeZone))
    }

    @Transactional(readOnly = true)
    fun getSleepStats(userId: Long): SleepStatsResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }
        val zone = ZoneId.of(user.timeZone)

        // Calculate date range (last 30 days)
        val today = LocalDate.now()
        val thirtyDaysAgo = today.minusDays(30)

        // Get sleep logs for last 30 days
        val sleepLogs = sleepLogRepository.findByUserIdAndSleepDateBetweenOrderBySleepDateDesc(
            userId, thirtyDaysAgo, today
        )

        if (sleepLogs.isEmpty()) {
            throw ResourceNotFoundException("No sleep logs found for user with id: $userId in the last 30 days")
        }

        // Calculate average time in bed
        val avgTimeInBed = sleepLogs
            .map { it.totalTimeInBed }
            .average()
            .toInt()

        // Calculate average bed time and wake time
        val avgBedTime = calculateAverageLocalTime(
            sleepLogs.map {
                it.timeToBed.atZoneSameInstant(zone).toLocalTime()
            }
        )
        val avgWakeTime = calculateAverageLocalTime(
            sleepLogs.map {
                it.timeOutOfBed.atZoneSameInstant(zone).toLocalTime()
            }
        )

        // Calculate feeling frequencies
        val feelingFrequencies = sleepLogs
            .groupBy { it.feeling }
            .mapValues { it.value.size }

        return SleepStatsResponse(
            startDate = thirtyDaysAgo,
            endDate = today,
            averageTimeInBed = avgTimeInBed,
            averageBedTime = avgBedTime,
            averageWakeTime = avgWakeTime,
            feelingFrequencies = feelingFrequencies
        )
    }

    private fun calculateMinutesBetween(start: OffsetDateTime, end: OffsetDateTime): Int {
        return ChronoUnit.MINUTES.between(start, end).toInt()
    }

    private fun calculateAverageLocalTime(times: List<LocalTime>): LocalTime {
        if (times.isEmpty()) {
            return LocalTime.MIDNIGHT
        }

        // Convert each time to seconds from midnight
        val secondsFromMidnight = times.map {
            it.toSecondOfDay().toLong()
        }

        // Calculate average seconds
        val avgSeconds = secondsFromMidnight.average().toLong()

        // Convert back to LocalTime
        return LocalTime.ofSecondOfDay(avgSeconds)
    }
}