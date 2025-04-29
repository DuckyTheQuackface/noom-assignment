package com.noom.interview.fullstack.sleep

import com.noom.interview.fullstack.sleep.sleeplog.entity.MorningFeeling
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLogEntity
import com.noom.interview.fullstack.sleep.user.entity.UserEntity
import com.noom.interview.fullstack.sleep.sleeplog.dto.request.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.sleeplog.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.user.repository.UserRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.jvm.optionals.getOrElse

/**
 * Utility class for generating test data
 */
class TestDataUtils {

    companion object {
        /**
         * Create a test user
         */
        fun createTestUser(username: String = "testuser", timeZone: String = "America/New_York"): UserEntity {
            return UserEntity(
                username = username,
                timeZone = timeZone
            )
        }

        /**
         * Create a test sleep log request
         */
        fun createSleepLogRequest(
            sleepDate: LocalDate = LocalDate.now().minusDays(1),
            timeToBed: LocalTime = LocalTime.of(22, 0),
            timeOutOfBed: LocalTime = LocalTime.of(6, 0),
            timeZoneId: String? = "America/New_York",
            feeling: MorningFeeling = MorningFeeling.GOOD
        ): CreateSleepLogRequest {
            return CreateSleepLogRequest(
                sleepDate = sleepDate,
                timeToBed = timeToBed,
                timeOutOfBed = timeOutOfBed,
                timeZoneId = timeZoneId,
                feeling = feeling
            )
        }

        /**
         * Create a test sleep log entity
         */
        fun createSleepLog(
            userEntity: UserEntity,
            sleepDate: LocalDate = LocalDate.now().minusDays(1),
            localTimeToBed: LocalTime = LocalTime.of(22, 0),
            localTimeOutOfBed: LocalTime = LocalTime.of(6, 0),
            timeZoneId: String = "America/New_York",
            feeling: MorningFeeling = MorningFeeling.GOOD
        ): SleepLogEntity {
            val zoneId = ZoneId.of(timeZoneId)

            val bedDateTime = ZonedDateTime.of(sleepDate, localTimeToBed, zoneId)
            val utcBedTime = bedDateTime.toOffsetDateTime()

            // Handle next day for wake time
            var wakeUpDate = sleepDate
            if (localTimeOutOfBed.isBefore(localTimeToBed)) {
                wakeUpDate = sleepDate.plusDays(1)
            }
            val wakeDateTime = ZonedDateTime.of(wakeUpDate, localTimeOutOfBed, zoneId)
            val utcWakeTime = wakeDateTime.toOffsetDateTime()

            // Calculate total time in bed
            var minutes = java.time.temporal.ChronoUnit.MINUTES.between(localTimeToBed, localTimeOutOfBed).toInt()
            if (minutes < 0) {
                minutes += 24 * 60 // Add 24 hours in minutes
            }

            return SleepLogEntity(
                userEntity = userEntity,
                sleepDate = sleepDate,
                localTimeToBed = localTimeToBed,
                localTimeOutOfBed = localTimeOutOfBed,
                utcTimeToBed = utcBedTime,
                utcTimeOutOfBed = utcWakeTime,
                timeZoneId = timeZoneId,
                totalTimeInBedMinutes = minutes,
                feeling = feeling
            )
        }

        /**
         * Setup multiple sleep logs for statistics testing
         */
        fun setupMultipleSleepLogs(
            userRepository: UserRepository,
            sleepLogRepository: SleepLogRepository,
            count: Int = 30
        ): UserEntity {
            val user = createTestUser()
            val savedUser = userRepository.findByUsername(user.username).getOrElse {
                userRepository.save(user)
            }

            val today = LocalDate.now()

            // Create sleep logs for the last 'count' days
            for (i in 0 until count) {
                val date = today.minusDays(i.toLong())
                val feeling = when (i % 3) {
                    0 -> MorningFeeling.GOOD
                    1 -> MorningFeeling.OK
                    else -> MorningFeeling.BAD
                }

                // Vary bed times slightly
                val bedHour = 22 + (i % 2)
                val wakeHour = 6 + (i % 3)

                val sleepLog = createSleepLog(
                    userEntity = savedUser,
                    sleepDate = date,
                    localTimeToBed = LocalTime.of(bedHour, 0),
                    localTimeOutOfBed = LocalTime.of(wakeHour, 0),
                    feeling = feeling
                )

                sleepLogRepository.save(sleepLog)
            }

            return savedUser
        }
    }
}