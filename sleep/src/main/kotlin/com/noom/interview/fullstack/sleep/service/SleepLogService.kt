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

        // Use the provided time zone or default to user's time zone
        val timeZoneId = request.timeZoneId ?: user.timeZone
        val zoneId = ZoneId.of(timeZoneId)

        // Calculate total time in bed - simplified as confirmed by you
        val totalTimeInBed = calculateTotalTimeInBed(request.timeToBed, request.timeOutOfBed)

        // Create UTC times by combining date, local time, and time zone
        val bedDateTime = ZonedDateTime.of(request.sleepDate, request.timeToBed, zoneId)
        val utcBedTime = bedDateTime.toOffsetDateTime()

        // For wake time, we need to handle if it's the next day
        var wakeUpDate = request.sleepDate
        if (request.timeOutOfBed.isBefore(request.timeToBed)) {
            // If wake time is earlier than bed time, it's the next day
            wakeUpDate = request.sleepDate.plusDays(1)
        }
        val wakeDateTime = ZonedDateTime.of(wakeUpDate, request.timeOutOfBed, zoneId)
        val utcWakeTime = wakeDateTime.toOffsetDateTime()

        val sleepLog = SleepLog(
            user = user,
            sleepDate = request.sleepDate,
            localTimeToBed = request.timeToBed,
            localTimeOutOfBed = request.timeOutOfBed,
            utcTimeToBed = utcBedTime,
            utcTimeOutOfBed = utcWakeTime,
            timeZoneId = timeZoneId,
            totalTimeInBed = totalTimeInBed,
            feeling = request.feeling
        )

        // Save to database
        val savedSleepLog = sleepLogRepository.save(sleepLog)

        // Convert to DTO and return
        return sleepLogMapper.toDto(savedSleepLog)
    }

    @Transactional(readOnly = true)
    fun getLatestSleepLog(userId: Long): SleepLogResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }

        val latestSleepLog = sleepLogRepository.findFirstByUserIdOrderBySleepDateDesc(userId)
            .orElseThrow { ResourceNotFoundException("No sleep logs found for user with id: $userId") }

        return sleepLogMapper.toDto(latestSleepLog)
    }

    @Transactional(readOnly = true)
    fun getSleepStats(userId: Long): SleepStatsResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }

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
            sleepLogs.map { it.localTimeToBed }
        )
        val avgWakeTime = calculateAverageLocalTime(
            sleepLogs.map { it.localTimeOutOfBed }
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

    // Simplified method as confirmed by you
    private fun calculateTotalTimeInBed(bedTime: LocalTime, wakeTime: LocalTime): Int {
        var minutes = ChronoUnit.MINUTES.between(bedTime, wakeTime).toInt()

        // If result is negative, it means sleep crossed midnight
        if (minutes < 0) {
            minutes += 24 * 60 // Add 24 hours in minutes
        }

        return minutes
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
