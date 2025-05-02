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
import test.data.source.sleeplog.SleepThroughDaylightSavingTime
import test.data.source.sleeplog.SleepTime
import test.data.source.sleeplog.GoingToBadOrWakingUpDuringDstTransition
import test.data.source.sleeplog.TimeZone
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
    @MethodSource("sleepingThroughDaylightSavingTimeScenarios")
    fun `should handle daylight saving time when logging sleep through clock changes`(
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
            return SleepTime.stream()
        }

        @JvmStatic
        fun timeZoneScenarios(): Stream<Arguments> {
            return TimeZone.stream()
        }

        @JvmStatic
        fun sleepingThroughDaylightSavingTimeScenarios(): Stream<Arguments> {
            return SleepThroughDaylightSavingTime.stream()
        }

        @JvmStatic
        fun sleepingDuringDstTransitionScenarios(): Stream<Arguments> {
            return GoingToBadOrWakingUpDuringDstTransition.stream()
        }
    }
}