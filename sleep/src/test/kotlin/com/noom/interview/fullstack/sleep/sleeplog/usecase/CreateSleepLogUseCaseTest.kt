package com.noom.interview.fullstack.sleep.sleeplog.usecase

import com.noom.interview.fullstack.sleep.sleeplog.dto.request.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.sleeplog.dto.response.SleepLogResponse
import com.noom.interview.fullstack.sleep.sleeplog.entity.MorningFeeling
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLogEntity
import com.noom.interview.fullstack.sleep.sleeplog.mapper.SleepLogMapper
import com.noom.interview.fullstack.sleep.sleeplog.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.user.UserService
import com.noom.interview.fullstack.sleep.user.entity.UserEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.*

@ExtendWith(MockitoExtension::class)
class CreateSleepLogUseCaseTest {

    @Mock
    private lateinit var sleepLogRepository: SleepLogRepository

    @Mock
    private lateinit var userService: UserService

    @Captor
    private lateinit var sleepLogCaptor: ArgumentCaptor<SleepLogEntity>

    private lateinit var createSleepLogUseCase: CreateSleepLogUseCase

    private val userId = 1L
    private val defaultTimeZone = "America/New_York"
    private val mockUser = UserEntity(id = userId, username = "testuser", timeZone = defaultTimeZone)
    private val mockResponse = SleepLogResponse(
        id = 1L,
        sleepDate = LocalDate.now(),
        timeToBed = LocalTime.of(23, 0),
        timeOutOfBed = LocalTime.of(7, 0),
        totalTimeInBedMinutes = 480,
        feeling = MorningFeeling.GOOD,
        timeZoneId = defaultTimeZone
    )

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

    @Test
    fun `test create sleep log - standard evening to morning sleep`() {
        // Given
        val sleepDate = LocalDate.of(2023, 5, 8)
        val timeToBed = LocalTime.of(23, 0)
        val timeOutOfBed = LocalTime.of(7, 0)
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.GOOD,
            timeZoneId = defaultTimeZone
        )

        // When
        val result = createSleepLogUseCase.createSleepLog(userId, request)

        // Then
        verify(sleepLogRepository).save(sleepLogCaptor.capture())
        val savedEntity = sleepLogCaptor.value

        assertEquals(sleepDate, savedEntity.sleepDate)
        assertEquals(timeToBed, savedEntity.localTimeToBed)
        assertEquals(timeOutOfBed, savedEntity.localTimeOutOfBed)
        assertEquals(480, savedEntity.totalTimeInBedMinutes)
        assertEquals(MorningFeeling.GOOD, savedEntity.feeling)
        assertEquals(defaultTimeZone, savedEntity.timeZoneId)

        // Verify UTC times converted correctly
        val zoneId = ZoneId.of(defaultTimeZone)
        val expectedUtcBedTime = ZonedDateTime.of(sleepDate, timeToBed, zoneId).toOffsetDateTime()
        val expectedUtcWakeTime = expectedUtcBedTime.plusMinutes(480)

        assertEquals(expectedUtcBedTime, savedEntity.utcTimeToBed)
        assertEquals(expectedUtcWakeTime, savedEntity.utcTimeOutOfBed)

        assertEquals(480, result.totalTimeInBedMinutes)
    }

    @Test
    fun `test create sleep log - after midnight to morning sleep`() {
        // Given
        val sleepDate = LocalDate.of(2023, 5, 8)  // This is the sleep's reference date
        val timeToBed = LocalTime.of(1, 0)    // 1:00 AM
        val timeOutOfBed = LocalTime.of(8, 0) // 8:00 AM
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.OK,
            timeZoneId = defaultTimeZone
        )

        // When
        createSleepLogUseCase.createSleepLog(userId, request)

        // Then
        verify(sleepLogRepository).save(sleepLogCaptor.capture())
        val savedEntity = sleepLogCaptor.value

        assertEquals(sleepDate, savedEntity.sleepDate)
        assertEquals(timeToBed, savedEntity.localTimeToBed)
        assertEquals(timeOutOfBed, savedEntity.localTimeOutOfBed)
        assertEquals(420, savedEntity.totalTimeInBedMinutes) // 7 hours = 420 minutes

        // Since the bed time is early morning, it should be on the next day (sleepDate + 1)
        val zoneId = ZoneId.of(defaultTimeZone)
        val expectedUtcBedTime = ZonedDateTime.of(sleepDate.plusDays(1), timeToBed, zoneId).toOffsetDateTime()
        val expectedUtcWakeTime = expectedUtcBedTime.plusMinutes(420)

        assertEquals(expectedUtcBedTime, savedEntity.utcTimeToBed)
        assertEquals(expectedUtcWakeTime, savedEntity.utcTimeOutOfBed)
    }

    @Test
    fun `test create sleep log - evening to after midnight wake up`() {
        // Given
        val sleepDate = LocalDate.of(2023, 5, 8)
        val timeToBed = LocalTime.of(22, 0)    // 10:00 PM
        val timeOutOfBed = LocalTime.of(2, 0)  // 2:00 AM next day
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.BAD,
            timeZoneId = defaultTimeZone
        )

        // When
        createSleepLogUseCase.createSleepLog(userId, request)

        // Then
        verify(sleepLogRepository).save(sleepLogCaptor.capture())
        val savedEntity = sleepLogCaptor.value

        assertEquals(sleepDate, savedEntity.sleepDate)
        assertEquals(timeToBed, savedEntity.localTimeToBed)
        assertEquals(timeOutOfBed, savedEntity.localTimeOutOfBed)
        assertEquals(240, savedEntity.totalTimeInBedMinutes) // 4 hours = 240 minutes

        // Bed time is in evening of sleepDate
        val zoneId = ZoneId.of(defaultTimeZone)
        val expectedUtcBedTime = ZonedDateTime.of(sleepDate, timeToBed, zoneId).toOffsetDateTime()
        val expectedUtcWakeTime = expectedUtcBedTime.plusMinutes(240)

        assertEquals(expectedUtcBedTime, savedEntity.utcTimeToBed)
        assertEquals(expectedUtcWakeTime, savedEntity.utcTimeOutOfBed)
    }

    @Test
    fun `test create sleep log - midnight exactly`() {
        // Given
        val sleepDate = LocalDate.of(2023, 5, 8)
        val timeToBed = LocalTime.MIDNIGHT      // 12:00 AM
        val timeOutOfBed = LocalTime.of(8, 0)   // 8:00 AM
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.GOOD,
            timeZoneId = defaultTimeZone
        )

        // When
        createSleepLogUseCase.createSleepLog(userId, request)

        // Then
        verify(sleepLogRepository).save(sleepLogCaptor.capture())
        val savedEntity = sleepLogCaptor.value

        assertEquals(sleepDate, savedEntity.sleepDate)
        assertEquals(timeToBed, savedEntity.localTimeToBed)
        assertEquals(timeOutOfBed, savedEntity.localTimeOutOfBed)
        assertEquals(480, savedEntity.totalTimeInBedMinutes) // 8 hours = 480 minutes

        // Midnight should be considered as part of the next day (sleepDate + 1)
        val zoneId = ZoneId.of(defaultTimeZone)
        val expectedUtcBedTime = ZonedDateTime.of(sleepDate.plusDays(1), timeToBed, zoneId).toOffsetDateTime()
        val expectedUtcWakeTime = expectedUtcBedTime.plusMinutes(480)

        assertEquals(expectedUtcBedTime, savedEntity.utcTimeToBed)
        assertEquals(expectedUtcWakeTime, savedEntity.utcTimeOutOfBed)
    }

    @Test
    fun `test create sleep log - noon exactly`() {
        // Given
        val sleepDate = LocalDate.of(2023, 5, 8)
        val timeToBed = LocalTime.NOON          // 12:00 PM
        val timeOutOfBed = LocalTime.of(20, 0)  // 8:00 PM
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.OK,
            timeZoneId = defaultTimeZone
        )

        // When
        createSleepLogUseCase.createSleepLog(userId, request)

        // Then
        verify(sleepLogRepository).save(sleepLogCaptor.capture())
        val savedEntity = sleepLogCaptor.value

        assertEquals(sleepDate, savedEntity.sleepDate)
        assertEquals(timeToBed, savedEntity.localTimeToBed)
        assertEquals(timeOutOfBed, savedEntity.localTimeOutOfBed)
        assertEquals(480, savedEntity.totalTimeInBedMinutes) // 8 hours = 480 minutes

        // Noon should be on the current day
        val zoneId = ZoneId.of(defaultTimeZone)
        val expectedUtcBedTime = ZonedDateTime.of(sleepDate.plusDays(1), timeToBed, zoneId).toOffsetDateTime()
        val expectedUtcWakeTime = expectedUtcBedTime.plusMinutes(480)

        assertEquals(expectedUtcBedTime, savedEntity.utcTimeToBed)
        assertEquals(expectedUtcWakeTime, savedEntity.utcTimeOutOfBed)
    }

    @Test
    fun `test create sleep log - different timezone specified`() {
        // Given
        val sleepDate = LocalDate.of(2023, 5, 8)
        val timeToBed = LocalTime.of(23, 0)
        val timeOutOfBed = LocalTime.of(7, 0)
        val customTimeZone = "Europe/London"
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.GOOD,
            timeZoneId = customTimeZone
        )

        // When
        createSleepLogUseCase.createSleepLog(userId, request)

        // Then
        verify(sleepLogRepository).save(sleepLogCaptor.capture())
        val savedEntity = sleepLogCaptor.value

        assertEquals(sleepDate, savedEntity.sleepDate)
        assertEquals(timeToBed, savedEntity.localTimeToBed)
        assertEquals(timeOutOfBed, savedEntity.localTimeOutOfBed)
        assertEquals(480, savedEntity.totalTimeInBedMinutes) // 8 hours = 480 minutes
        assertEquals(customTimeZone, savedEntity.timeZoneId)

        // Calculate expected UTC times using the custom timezone
        val zoneId = ZoneId.of(customTimeZone)
        val expectedUtcBedTime = ZonedDateTime.of(sleepDate, timeToBed, zoneId).toOffsetDateTime()
        val expectedUtcWakeTime = expectedUtcBedTime.plusMinutes(480)

        assertEquals(expectedUtcBedTime, savedEntity.utcTimeToBed)
        assertEquals(expectedUtcWakeTime, savedEntity.utcTimeOutOfBed)
    }

    @Test
    fun `test create sleep log - user default timezone used when not specified`() {
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
        createSleepLogUseCase.createSleepLog(userId, request)

        // Then
        verify(sleepLogRepository).save(sleepLogCaptor.capture())
        val savedEntity = sleepLogCaptor.value

        assertEquals(sleepDate, savedEntity.sleepDate)
        assertEquals(timeToBed, savedEntity.localTimeToBed)
        assertEquals(timeOutOfBed, savedEntity.localTimeOutOfBed)
        assertEquals(defaultTimeZone, savedEntity.timeZoneId)

        // Calculate expected UTC times using the user's default timezone
        val zoneId = ZoneId.of(defaultTimeZone)
        val expectedUtcBedTime = ZonedDateTime.of(sleepDate, timeToBed, zoneId).toOffsetDateTime()
        val expectedUtcWakeTime = expectedUtcBedTime.plusMinutes(480)

        assertEquals(expectedUtcBedTime, savedEntity.utcTimeToBed)
        assertEquals(expectedUtcWakeTime, savedEntity.utcTimeOutOfBed)
    }

    @Test
    fun `test create sleep log - 24 hour sleep duration`() {
        // Given
        val sleepDate = LocalDate.of(2023, 5, 8)
        val timeToBed = LocalTime.of(7, 0)      // 7:00 AM
        val timeOutOfBed = LocalTime.of(7, 0)   // 7:00 AM (next day)
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.BAD,
            timeZoneId = defaultTimeZone
        )

        // When
        createSleepLogUseCase.createSleepLog(userId, request)

        // Then
        verify(sleepLogRepository).save(sleepLogCaptor.capture())
        val savedEntity = sleepLogCaptor.value

        assertEquals(sleepDate, savedEntity.sleepDate)
        assertEquals(timeToBed, savedEntity.localTimeToBed)
        assertEquals(timeOutOfBed, savedEntity.localTimeOutOfBed)
        assertEquals(1440, savedEntity.totalTimeInBedMinutes) // 24 hours = 1440 minutes
    }

    @Test
    fun `test create sleep log - very short sleep`() {
        // Given
        val sleepDate = LocalDate.of(2023, 5, 8)
        val timeToBed = LocalTime.of(23, 50)    // 11:50 PM
        val timeOutOfBed = LocalTime.of(0, 10)  // 12:10 AM next day
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.BAD,
            timeZoneId = defaultTimeZone
        )

        // When
        createSleepLogUseCase.createSleepLog(userId, request)

        // Then
        verify(sleepLogRepository).save(sleepLogCaptor.capture())
        val savedEntity = sleepLogCaptor.value

        assertEquals(sleepDate, savedEntity.sleepDate)
        assertEquals(timeToBed, savedEntity.localTimeToBed)
        assertEquals(timeOutOfBed, savedEntity.localTimeOutOfBed)
        assertEquals(20, savedEntity.totalTimeInBedMinutes)

        // Evening bed time
        val zoneId = ZoneId.of(defaultTimeZone)
        val expectedUtcBedTime = ZonedDateTime.of(sleepDate, timeToBed, zoneId).toOffsetDateTime()
        val expectedUtcWakeTime = expectedUtcBedTime.plusMinutes(20)

        assertEquals(expectedUtcBedTime, savedEntity.utcTimeToBed)
        assertEquals(expectedUtcWakeTime, savedEntity.utcTimeOutOfBed)
    }

    @Test
    fun `test create sleep log - long sleep crossing multiple days`() {
        // Given
        val sleepDate = LocalDate.of(2023, 5, 8)
        val timeToBed = LocalTime.of(22, 0)     // 10:00 PM
        val timeOutOfBed = LocalTime.of(11, 0)  // 11:00 AM next day
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.GOOD,
            timeZoneId = defaultTimeZone
        )

        // When
        createSleepLogUseCase.createSleepLog(userId, request)

        // Then
        verify(sleepLogRepository).save(sleepLogCaptor.capture())
        val savedEntity = sleepLogCaptor.value

        assertEquals(sleepDate, savedEntity.sleepDate)
        assertEquals(timeToBed, savedEntity.localTimeToBed)
        assertEquals(timeOutOfBed, savedEntity.localTimeOutOfBed)
        assertEquals(780, savedEntity.totalTimeInBedMinutes) // 13 hours = 780 minutes
    }

    @Test
    fun `test create sleep log - response mapping`() {
        // Given
        val sleepDate = LocalDate.of(2023, 5, 8)
        val timeToBed = LocalTime.of(23, 0)
        val timeOutOfBed = LocalTime.of(7, 0)
        val request = CreateSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.GOOD,
            timeZoneId = defaultTimeZone
        )

        // When
        val result = createSleepLogUseCase.createSleepLog(userId, request)

        // Then
        assertEquals(480, result.totalTimeInBedMinutes)
    }
}