package com.noom.interview.fullstack.sleep.sleeplog.usecase

import com.noom.interview.fullstack.sleep.sleeplog.dto.request.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.sleeplog.entity.MorningFeeling
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLogEntity
import com.noom.interview.fullstack.sleep.sleeplog.exception.TimeDoesNotExist
import com.noom.interview.fullstack.sleep.sleeplog.mapper.SleepLogMapper
import com.noom.interview.fullstack.sleep.sleeplog.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.user.UserService
import com.noom.interview.fullstack.sleep.user.entity.UserEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.stream.Stream

@ExtendWith(MockitoExtension::class)
class CreateSleepLogUseCaseTest {

    @Mock(lenient = true)
    private lateinit var sleepLogRepository: SleepLogRepository

    @Mock
    private lateinit var userService: UserService

    @Captor
    private lateinit var sleepLogCaptor: ArgumentCaptor<SleepLogEntity>

    private lateinit var createSleepLogUseCase: CreateSleepLogUseCase

    private val userId = 1L
    private val defaultTimeZone = "America/New_York"
    private val mockUser = UserEntity(id = userId, username = "testuser", timeZone = defaultTimeZone)

    @BeforeEach
    fun setUp() {
        createSleepLogUseCase = CreateSleepLogUseCase(
            sleepLogRepository,
            SleepLogMapper(),
            userService
        )
        `when`(userService.getUserById(userId)).thenReturn(mockUser)
        `when`(sleepLogRepository.save(any())).thenAnswer { it.arguments[0] as SleepLogEntity }
    }

    @ParameterizedTest(name = "Should calculate correct UTC times when {0}")
    @MethodSource("sleepTimeScenarios")
    fun `should calculate correct UTC times when various sleep patterns are entered`(
        testName: String,
        sleepDate: LocalDate,
        timeToBed: LocalTime,
        timeOutOfBed: LocalTime,
        timeZoneId: String,
        expectedTotalMinutes: Int,
        expectedUtcBedTime: OffsetDateTime,
        expectedUtcWakeTime: OffsetDateTime
    ) {
        // Given
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.GOOD,
            timeZoneId = timeZoneId
        )

        // When
        val result = createSleepLogUseCase.createSleepLog(userId, request)

        // Then
        verify(sleepLogRepository).save(sleepLogCaptor.capture())
        val savedEntity = sleepLogCaptor.value

        assertEquals(sleepDate, savedEntity.sleepDate)
        assertEquals(timeToBed, savedEntity.localTimeToBed)
        assertEquals(timeOutOfBed, savedEntity.localTimeOutOfBed)
        assertEquals(expectedTotalMinutes, savedEntity.totalTimeInBedMinutes)
        assertEquals(timeZoneId, savedEntity.timeZoneId)
        assertEquals(expectedUtcBedTime, savedEntity.utcTimeToBed)
        assertEquals(expectedUtcWakeTime, savedEntity.utcTimeOutOfBed)
        assertEquals(expectedTotalMinutes, result.totalTimeInBedMinutes)
    }

    @ParameterizedTest(name = "Should handle different timezones when {0}")
    @MethodSource("timeZoneScenarios")
    fun `should handle different timezones when logging sleep on various dates and times`(
        testName: String,
        sleepDate: LocalDate,
        timeToBed: LocalTime,
        timeOutOfBed: LocalTime,
        timeZoneId: String,
        expectedTotalMinutes: Int,
        expectedUtcBedTime: OffsetDateTime,
        expectedUtcWakeTime: OffsetDateTime
    ) {
        // Given
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.OK,
            timeZoneId = timeZoneId
        )

        // When
        val result = createSleepLogUseCase.createSleepLog(userId, request)

        // Then
        verify(sleepLogRepository).save(sleepLogCaptor.capture())
        val savedEntity = sleepLogCaptor.value

        assertEquals(sleepDate, savedEntity.sleepDate)
        assertEquals(timeToBed, savedEntity.localTimeToBed)
        assertEquals(timeOutOfBed, savedEntity.localTimeOutOfBed)
        assertEquals(expectedTotalMinutes, savedEntity.totalTimeInBedMinutes)
        assertEquals(timeZoneId, savedEntity.timeZoneId)
        assertEquals(expectedUtcBedTime, savedEntity.utcTimeToBed)
        assertEquals(expectedUtcWakeTime, savedEntity.utcTimeOutOfBed)
    }

    @ParameterizedTest(name = "Should handle daylight saving time when {0}")
    @MethodSource("daylightSavingTimeScenarios")
    fun `should handle daylight saving time when logging sleep during clock changes`(
        testName: String,
        sleepDate: LocalDate,
        timeToBed: LocalTime,
        timeOutOfBed: LocalTime,
        timeZoneId: String,
        expectedTotalMinutes: Int,
        expectedUtcBedTime: OffsetDateTime,
        expectedUtcWakeTime: OffsetDateTime
    ) {
        // Given
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.BAD,
            timeZoneId = timeZoneId
        )

        // When
        val result = createSleepLogUseCase.createSleepLog(userId, request)

        // Then
        verify(sleepLogRepository).save(sleepLogCaptor.capture())
        val savedEntity = sleepLogCaptor.value

        assertEquals(sleepDate, savedEntity.sleepDate)
        assertEquals(timeToBed, savedEntity.localTimeToBed)
        assertEquals(timeOutOfBed, savedEntity.localTimeOutOfBed)
        assertEquals(expectedTotalMinutes, savedEntity.totalTimeInBedMinutes)
        assertEquals(timeZoneId, savedEntity.timeZoneId)
        assertEquals(expectedUtcBedTime, savedEntity.utcTimeToBed)
        assertEquals(expectedUtcWakeTime, savedEntity.utcTimeOutOfBed)
    }


    @ParameterizedTest(name = "Should handle calculation of UTC times when {0}")
    @MethodSource("sleepingDuringDstTransitionScenarios")
    fun `should handle daylight saving time calculation when going to bed or waking up during clock changes`(
        testName: String,
        sleepDate: LocalDate,
        timeToBed: LocalTime,
        timeOutOfBed: LocalTime,
        timeZoneId: String,
        expectedTotalMinutes: Int,
        expectedUtcBedTime: OffsetDateTime,
        expectedUtcWakeTime: OffsetDateTime
    ) {
        // Given
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.BAD,
            timeZoneId = timeZoneId
        )

        // When - Then
        kotlin.runCatching {
            createSleepLogUseCase.createSleepLog(userId, request)
        }.onFailure {
            assertTrue(it is TimeDoesNotExist)
        }.onSuccess {
            verify(sleepLogRepository).save(sleepLogCaptor.capture())
            val savedEntity = sleepLogCaptor.value

            assertEquals(sleepDate, savedEntity.sleepDate)
            assertEquals(timeToBed, savedEntity.localTimeToBed)
            assertEquals(timeOutOfBed, savedEntity.localTimeOutOfBed)
            assertEquals(expectedTotalMinutes, savedEntity.totalTimeInBedMinutes)
            assertEquals(timeZoneId, savedEntity.timeZoneId)
            assertEquals(expectedUtcBedTime, savedEntity.utcTimeToBed)
            assertEquals(expectedUtcWakeTime, savedEntity.utcTimeOutOfBed)
        }
    }

    @Test
    fun `should use user default timezone when timeZoneId is not provided`() {
        // Given
        val sleepDate = LocalDate.of(2023, 5, 8)
        val timeToBed = LocalTime.of(23, 0)
        val timeOutOfBed = LocalTime.of(7, 0)
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.GOOD,
            timeZoneId = null  // Not specifying timezone should use user's default
        )

        // When
        val result = createSleepLogUseCase.createSleepLog(userId, request)

        // Then
        verify(sleepLogRepository).save(sleepLogCaptor.capture())
        val savedEntity = sleepLogCaptor.value

        assertEquals(sleepDate, savedEntity.sleepDate)
        assertEquals(timeToBed, savedEntity.localTimeToBed)
        assertEquals(timeOutOfBed, savedEntity.localTimeOutOfBed)
        assertEquals(defaultTimeZone, savedEntity.timeZoneId)
        assertEquals(480, savedEntity.totalTimeInBedMinutes)

        // The expected UTC time for America/New_York at 23:00 on May 8, 2023
        val expectedBedTimeOffset = ZoneOffset.ofHours(-4) // EDT (UTC-4) for this date
        val expectedUtcBedTime = OffsetDateTime.of(2023, 5, 8, 23, 0, 0, 0, expectedBedTimeOffset)
        val expectedUtcWakeTime = expectedUtcBedTime.plusMinutes(480)

        assertEquals(expectedUtcBedTime, savedEntity.utcTimeToBed)
        assertEquals(expectedUtcWakeTime, savedEntity.utcTimeOutOfBed)
    }

    @Test
    fun `should throw exception when sleep duration is less than minimum allowed`() {
        // Given
        val sleepDate = LocalDate.of(2023, 5, 8)
        val timeToBed = LocalTime.of(21, 0)
        val timeOutOfBed = LocalTime.of(22, 30)
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.BAD,
            timeZoneId = defaultTimeZone
        )

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            createSleepLogUseCase.createSleepLog(userId, request)
        }
        assertTrue(exception.message!!.contains("Sleep duration must be at least 2 hours"))
    }

    @Test
    fun `should throw exception when sleep duration exceeds maximum allowed`() {
        // Given
        val sleepDate = LocalDate.of(2023, 5, 8)
        val timeToBed = LocalTime.of(22, 0)
        val timeOutOfBed = LocalTime.of(16, 0) // 24 hours (1440 minutes) of sleep
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.BAD,
            timeZoneId = defaultTimeZone
        )

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            createSleepLogUseCase.createSleepLog(userId, request)
        }
        assertTrue(exception.message!!.contains("Sleep duration cannot exceed 16 hours"))
    }

    companion object {
        @JvmStatic
        fun sleepTimeScenarios(): Stream<Arguments> {
            return Stream.of(
                // Evening to morning sleep (standard case)
                Arguments.of(
                    "sleeping from evening to morning",
                    LocalDate.of(2023, 5, 8),
                    LocalTime.of(23, 0), // 11:00 PM
                    LocalTime.of(7, 0),  // 7:00 AM
                    "America/New_York",
                    480, // 8 hours = 480 minutes
                    OffsetDateTime.of(2023, 5, 8, 23, 0, 0, 0, ZoneOffset.ofHours(-4)),
                    OffsetDateTime.of(2023, 5, 9, 7, 0, 0, 0, ZoneOffset.ofHours(-4))
                ),

                // After midnight to morning sleep
                Arguments.of(
                    "sleeping from after midnight to morning",
                    LocalDate.of(2023, 5, 8),
                    LocalTime.of(1, 0),  // 1:00 AM
                    LocalTime.of(8, 0),  // 8:00 AM
                    "America/New_York",
                    420, // 7 hours = 420 minutes
                    OffsetDateTime.of(2023, 5, 9, 1, 0, 0, 0, ZoneOffset.ofHours(-4)),
                    OffsetDateTime.of(2023, 5, 9, 8, 0, 0, 0, ZoneOffset.ofHours(-4))
                ),

                // Evening to after midnight wake up
                Arguments.of(
                    "sleeping from evening to after midnight",
                    LocalDate.of(2023, 5, 8),
                    LocalTime.of(22, 0), // 10:00 PM
                    LocalTime.of(2, 0),  // 2:00 AM next day
                    "America/New_York",
                    240, // 4 hours = 240 minutes
                    OffsetDateTime.of(2023, 5, 8, 22, 0, 0, 0, ZoneOffset.ofHours(-4)),
                    OffsetDateTime.of(2023, 5, 9, 2, 0, 0, 0, ZoneOffset.ofHours(-4))
                ),

                // Midnight exactly
                Arguments.of(
                    "sleeping from midnight exactly",
                    LocalDate.of(2023, 5, 8),
                    LocalTime.MIDNIGHT, // 12:00 AM
                    LocalTime.of(8, 0),  // 8:00 AM
                    "America/New_York",
                    480, // 8 hours = 480 minutes
                    OffsetDateTime.of(2023, 5, 9, 0, 0, 0, 0, ZoneOffset.ofHours(-4)),
                    OffsetDateTime.of(2023, 5, 9, 8, 0, 0, 0, ZoneOffset.ofHours(-4))
                ),

                // Noon sleep (daytime sleep)
                Arguments.of(
                    "sleeping during daytime (noon)",
                    LocalDate.of(2023, 5, 8),
                    LocalTime.NOON,     // 12:00 PM
                    LocalTime.of(20, 0), // 8:00 PM
                    "America/New_York",
                    480, // 8 hours = 480 minutes
                    OffsetDateTime.of(2023, 5, 9, 12, 0, 0, 0, ZoneOffset.ofHours(-4)),
                    OffsetDateTime.of(2023, 5, 9, 20, 0, 0, 0, ZoneOffset.ofHours(-4))
                ),

                // Minimal allowed sleep
                Arguments.of(
                    "sleeping for minimum allowed duration",
                    LocalDate.of(2023, 5, 8),
                    LocalTime.of(23, 0), // 11:00 PM
                    LocalTime.of(1, 0),  // 1:00 AM next day
                    "America/New_York",
                    120, // 2 hours = 120 minutes (minimum)
                    OffsetDateTime.of(2023, 5, 8, 23, 0, 0, 0, ZoneOffset.ofHours(-4)),
                    OffsetDateTime.of(2023, 5, 9, 1, 0, 0, 0, ZoneOffset.ofHours(-4))
                ),

                // Maximum allowed sleep
                Arguments.of(
                    "sleeping for maximum allowed duration",
                    LocalDate.of(2023, 5, 8),
                    LocalTime.of(20, 0), // 8:00 PM
                    LocalTime.of(12, 0), // 12:00 PM next day
                    "America/New_York",
                    960, // 16 hours = 960 minutes (maximum)
                    OffsetDateTime.of(2023, 5, 8, 20, 0, 0, 0, ZoneOffset.ofHours(-4)),
                    OffsetDateTime.of(2023, 5, 9, 12, 0, 0, 0, ZoneOffset.ofHours(-4))
                )
            )
        }

        @JvmStatic
        fun timeZoneScenarios(): Stream<Arguments> {
            return Stream.of(
                // Month beginning edge case - New York (UTC-4/5)
                Arguments.of(
                    "logging at the start of month in NYC",
                    LocalDate.of(2025, 5, 1),
                    LocalTime.of(23, 0), // 11:00 PM
                    LocalTime.of(7, 0),  // 7:00 AM
                    "America/New_York",
                    480, // 8 hours = 480 minutes
                    OffsetDateTime.of(2025, 5, 1, 23, 0, 0, 0, ZoneOffset.ofHours(-4)),
                    OffsetDateTime.of(2025, 5, 2, 7, 0, 0, 0, ZoneOffset.ofHours(-4))
                ),

                // Year end edge case - Tokyo (UTC+9)
                Arguments.of(
                    "logging at the end of year in Tokyo",
                    LocalDate.of(2024, 12, 31),
                    LocalTime.of(23, 0), // 11:00 PM
                    LocalTime.of(7, 0),  // 7:00 AM
                    "Asia/Tokyo",
                    480, // 8 hours = 480 minutes
                    OffsetDateTime.of(2024, 12, 31, 23, 0, 0, 0, ZoneOffset.ofHours(9)),
                    OffsetDateTime.of(2025, 1, 1, 7, 0, 0, 0, ZoneOffset.ofHours(9))
                ),

                // Sydney, Australia (UTC+10/11)
                Arguments.of(
                    "logging sleep in Sydney",
                    LocalDate.of(2023, 5, 8),
                    LocalTime.of(22, 0), // 10:00 PM
                    LocalTime.of(6, 0),  // 6:00 AM
                    "Australia/Sydney",
                    480, // 8 hours = 480 minutes
                    OffsetDateTime.of(2023, 5, 8, 22, 0, 0, 0, ZoneOffset.ofHours(10)),
                    OffsetDateTime.of(2023, 5, 9, 6, 0, 0, 0, ZoneOffset.ofHours(10))
                ),

                // London (UTC+0/1)
                Arguments.of(
                    "logging sleep in London",
                    LocalDate.of(2023, 5, 8),
                    LocalTime.of(23, 0), // 11:00 PM
                    LocalTime.of(7, 0),  // 7:00 AM
                    "Europe/London",
                    480, // 8 hours = 480 minutes
                    OffsetDateTime.of(2023, 5, 8, 23, 0, 0, 0, ZoneOffset.ofHours(1)),
                    OffsetDateTime.of(2023, 5, 9, 7, 0, 0, 0, ZoneOffset.ofHours(1))
                ),

                // New Delhi, India (UTC+5:30)
                Arguments.of(
                    "logging sleep in New Delhi with half-hour offset",
                    LocalDate.of(2023, 5, 8),
                    LocalTime.of(23, 0), // 11:00 PM
                    LocalTime.of(7, 0),  // 7:00 AM
                    "Asia/Kolkata",
                    480, // 8 hours = 480 minutes
                    OffsetDateTime.of(2023, 5, 8, 23, 0, 0, 0, ZoneOffset.ofHoursMinutes(5, 30)),
                    OffsetDateTime.of(2023, 5, 9, 7, 0, 0, 0, ZoneOffset.ofHoursMinutes(5, 30))
                ),

                // Auckland, New Zealand (UTC+12/13)
                Arguments.of(
                    "logging sleep in Auckland (extreme eastern timezone)",
                    LocalDate.of(2023, 5, 8),
                    LocalTime.of(22, 30), // 10:30 PM
                    LocalTime.of(6, 30),  // 6:30 AM
                    "Pacific/Auckland",
                    480, // 8 hours = 480 minutes
                    OffsetDateTime.of(2023, 5, 8, 22, 30, 0, 0, ZoneOffset.ofHours(12)),
                    OffsetDateTime.of(2023, 5, 9, 6, 30, 0, 0, ZoneOffset.ofHours(12))
                ),

                // Honolulu, Hawaii (UTC-10)
                Arguments.of(
                    "logging sleep in Honolulu (extreme western timezone)",
                    LocalDate.of(2023, 5, 8),
                    LocalTime.of(21, 0), // 9:00 PM
                    LocalTime.of(5, 0),  // 5:00 AM
                    "Pacific/Honolulu",
                    480, // 8 hours = 480 minutes
                    OffsetDateTime.of(2023, 5, 8, 21, 0, 0, 0, ZoneOffset.ofHours(-10)),
                    OffsetDateTime.of(2023, 5, 9, 5, 0, 0, 0, ZoneOffset.ofHours(-10))
                )
            )
        }

        @JvmStatic
        fun daylightSavingTimeScenarios(): Stream<Arguments> {
            return Stream.of(
                // US Spring Forward (March 2023) - America/New_York
                // On 2023-03-12, at 2am, clocks jump to 3am (losing an hour)
                Arguments.of(
                    "sleeping through spring forward (losing an hour)",
                    LocalDate.of(2023, 3, 11), // night before DST change
                    LocalTime.of(23, 0),      // 11:00 PM
                    LocalTime.of(7, 0),       // 7:00 AM next day
                    "America/New_York",
                    420, // 8 hours in local time (but only 7 hours in actual elapsed time)
                    OffsetDateTime.of(2023, 3, 11, 23, 0, 0, 0, ZoneOffset.ofHours(-5)), // EST
                    OffsetDateTime.of(2023, 3, 12, 7, 0, 0, 0, ZoneOffset.ofHours(-4))   // EDT
                ),

                // US Fall Back (November 2023) - America/New_York
                // On 2023-11-05, at 2am, clocks go back to 1am (gaining an hour)
                Arguments.of(
                    "sleeping through fall back (gaining an hour)",
                    LocalDate.of(2023, 11, 4), // night before DST change
                    LocalTime.of(23, 0),      // 11:00 PM
                    LocalTime.of(7, 0),       // 7:00 AM next day
                    "America/New_York",
                    540, // 8 hours in local time (but actually 9 hours in elapsed time)
                    OffsetDateTime.of(2023, 11, 4, 23, 0, 0, 0, ZoneOffset.ofHours(-4)), // EDT
                    OffsetDateTime.of(2023, 11, 5, 7, 0, 0, 0, ZoneOffset.ofHours(-5))   // EST
                ),

                // Europe Spring Forward (March 2023) - Europe/London
                // On 2023-03-26, at 1am, clocks jump to 2am (losing an hour)
                Arguments.of(
                    "sleeping through European DST spring forward",
                    LocalDate.of(2023, 3, 25), // night before DST change
                    LocalTime.of(23, 0),      // 11:00 PM
                    LocalTime.of(7, 0),       // 7:00 AM next day
                    "Europe/London",
                    420, // 8 hours in local time (but only 7 hours in actual elapsed time)
                    OffsetDateTime.of(2023, 3, 25, 23, 0, 0, 0, ZoneOffset.ofHours(0)),  // GMT
                    OffsetDateTime.of(2023, 3, 26, 7, 0, 0, 0, ZoneOffset.ofHours(1))    // BST
                ),

                // Europe Fall Back (October 2023) - Europe/London
                // On 2023-10-29, at 2am, clocks go back to 1am (gaining an hour)
                Arguments.of(
                    "sleeping through European DST fall back",
                    LocalDate.of(2023, 10, 28), // night before DST change
                    LocalTime.of(23, 0),       // 11:00 PM
                    LocalTime.of(7, 0),        // 7:00 AM next day
                    "Europe/London",
                    540, // 8 hours in local time (but actually 9 hours in elapsed time)
                    OffsetDateTime.of(2023, 10, 28, 23, 0, 0, 0, ZoneOffset.ofHours(1)), // BST
                    OffsetDateTime.of(2023, 10, 29, 7, 0, 0, 0, ZoneOffset.ofHours(0))   // GMT
                ),

                // Australia DST starts (October) - Sydney
                // On 2023-10-01, at 2am, clocks jump to 3am (losing an hour)
                Arguments.of(
                    "sleeping through Australian DST start",
                    LocalDate.of(2023, 9, 30), // night before DST change
                    LocalTime.of(23, 0),      // 11:00 PM
                    LocalTime.of(7, 0),       // 7:00 AM next day
                    "Australia/Sydney",
                    420, // 8 hours in local time (but only 7 hours in actual elapsed time)
                    OffsetDateTime.of(2023, 9, 30, 23, 0, 0, 0, ZoneOffset.ofHours(10)), // AEST
                    OffsetDateTime.of(2023, 10, 1, 7, 0, 0, 0, ZoneOffset.ofHours(11))   // AEDT
                ),

                // Australia DST ends (April) - Sydney
                // On 2023-04-02, at 3am, clocks go back to 2am (gaining an hour)
                Arguments.of(
                    "sleeping through Australian DST end",
                    LocalDate.of(2023, 4, 1), // night before DST change
                    LocalTime.of(23, 0),     // 11:00 PM
                    LocalTime.of(7, 0),      // 7:00 AM next day
                    "Australia/Sydney",
                    540, // 8 hours in local time (but actually 9 hours in elapsed time)
                    OffsetDateTime.of(2023, 4, 1, 23, 0, 0, 0, ZoneOffset.ofHours(11)), // AEDT
                    OffsetDateTime.of(2023, 4, 2, 7, 0, 0, 0, ZoneOffset.ofHours(10))   // AEST
                )
            )
        }

        @JvmStatic
        fun sleepingDuringDstTransitionScenarios(): Stream<Arguments> {
            return Stream.of(
                // US Spring Forward (March 2023) - Going to bed right at 2:00 AM transition (non-existent hour)
                // When clock jumps from 1:59 AM to 3:00 AM
                Arguments.of(
                    "going to bed during spring forward (at non-existent hour)",
                    LocalDate.of(2023, 3, 11), // Night before transition
                    LocalTime.of(2, 30), // 1:30 AM (before transition)
                    LocalTime.of(9, 30), // 9:30 AM
                    "America/New_York",
                    480, // 8 hours in local time
                    OffsetDateTime.of(2023, 3, 12, 1, 30, 0, 0, ZoneOffset.ofHours(-5)), // EST
                    OffsetDateTime.of(2023, 3, 12, 9, 30, 0, 0, ZoneOffset.ofHours(-4))  // EDT
                ),

                // US Spring Forward - Waking up after transition
                Arguments.of(
                    "going to bed before and waking up after spring forward",
                    LocalDate.of(2023, 3, 11),
                    LocalTime.of(23, 0), // 11:00 PM (before transition day)
                    LocalTime.of(3, 30), // 3:30 AM (after transition)
                    "America/New_York",
                    210, // 4.5 hours (accounting for lost hour)
                    OffsetDateTime.of(2023, 3, 11, 23, 0, 0, 0, ZoneOffset.ofHours(-5)), // EST
                    OffsetDateTime.of(2023, 3, 12, 3, 30, 0, 0, ZoneOffset.ofHours(-4))  // EDT
                ),

                // US Fall Back (November 2023) - Going to bed during ambiguous hour
                // At 2:00 AM, clocks go back to 1:00 AM, creating an ambiguous hour
                Arguments.of(
                    "going to bed during fall back ambiguous hour",
                    LocalDate.of(2023, 11, 4),
                    LocalTime.of(1, 30), // 1:30 AM (in the ambiguous hour)
                    LocalTime.of(9, 30), // 9:30 AM
                    "America/New_York",
                    540, // 9 hours - take the earlier hour
                    OffsetDateTime.of(2023, 11, 5, 1, 30, 0, 0, ZoneOffset.ofHours(-4)), // First 1:30 AM (EDT)
                    OffsetDateTime.of(2023, 11, 5, 9, 30, 0, 0, ZoneOffset.ofHours(-5))  // EST
                ),

                // US Fall Back - Waking up during the repeated hour
                Arguments.of(
                    "waking up during fall back repeated hour",
                    LocalDate.of(2023, 11, 4),
                    LocalTime.of(21, 0), // 9:00 PM
                    LocalTime.of(1, 30), // 1:30 AM (in repeated hour)
                    "America/New_York",
                    270, // 4.5 hours (taking the earlier hour)
                    OffsetDateTime.of(2023, 11, 4, 21, 0, 0, 0, ZoneOffset.ofHours(-4)), // EDT
                    OffsetDateTime.of(2023, 11, 5, 1, 30, 0, 0, ZoneOffset.ofHours(-4))  // EST (first 1:30 AM)
                ),

                // European DST - "Skipped hour" scenario (going to sleep before transition, waking during "skipped" hour)
                Arguments.of(
                    "waking up at skipped hour during European DST change",
                    LocalDate.of(2025, 3, 29),
                    LocalTime.of(23, 0), // 11:00 PM
                    LocalTime.of(2, 30), // 2:30 AM (skipped hour - clocks jumped from 1:00 to 3:00)
                    "Europe/Berlin",
                    210, // 3.5 hours (accounting for lost hour)
                    OffsetDateTime.of(1999, 3, 25, 23, 0, 0, 0, ZoneOffset.ofHours(0)), // GMT
                    OffsetDateTime.of(1999, 3, 26, 2, 30, 0, 0, ZoneOffset.ofHours(1))  // BST
                ),

                // European DST Fall Back - Very short sleep that spans exactly the transition
                Arguments.of(
                    "short sleep spanning exactly the European DST fall transition",
                    LocalDate.of(2023, 10, 28),
                    LocalTime.of(1, 30), // 1:30 AM
                    LocalTime.of(3, 30), // 3:30 AM (after repeated hour)
                    "Europe/London",
                    180, // 3 hours (accounting for repeated hour)
                    OffsetDateTime.of(2023, 10, 29, 1, 30, 0, 0, ZoneOffset.ofHours(1)), // BST
                    OffsetDateTime.of(2023, 10, 29, 3, 30, 0, 0, ZoneOffset.ofHours(0))  // GMT
                )
            )
        }
    }
}