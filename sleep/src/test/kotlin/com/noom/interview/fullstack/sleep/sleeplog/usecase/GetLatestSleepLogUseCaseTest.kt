package com.noom.interview.fullstack.sleep.sleeplog.usecase

import com.noom.interview.fullstack.sleep.sleeplog.dto.response.SleepLogResponse
import com.noom.interview.fullstack.sleep.sleeplog.entity.MorningFeeling
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLogEntity
import com.noom.interview.fullstack.sleep.sleeplog.exception.SleepLogNotFoundByUserId
import com.noom.interview.fullstack.sleep.sleeplog.mapper.SleepLogMapper
import com.noom.interview.fullstack.sleep.sleeplog.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.user.entity.UserEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.time.LocalTime
import java.time.OffsetDateTime

@ExtendWith(MockitoExtension::class)
class GetLatestSleepLogUseCaseTest {

    @Mock
    private lateinit var sleepLogRepository: SleepLogRepository

    @Mock
    private lateinit var sleepLogMapper: SleepLogMapper

    @Mock
    private lateinit var getLatestSleepLogUseCase: GetLatestSleepLogUseCase

    @BeforeEach
    fun setUp() {
        getLatestSleepLogUseCase = GetLatestSleepLogUseCase(
            sleepLogRepository = sleepLogRepository,
            sleepLogMapper = sleepLogMapper
        )
    }

    @Test
    fun `should return mapped response when sleep log is found for user`() {
        // Given
        val userId = 123L
        val userEntity = UserEntity(id = userId, username = "Test User", timeZone = "America/New_York")

        val sleepLogEntity = SleepLogEntity(
            id = 1L,
            user = userEntity,
            sleepDate = LocalDate.of(2025, 5, 1),
            localTimeToBed = LocalTime.of(22, 0),
            localTimeOutOfBed = LocalTime.of(6, 0),
            utcTimeToBed = OffsetDateTime.now()
                .withDayOfMonth(1).withMonth(5).withYear(2025)
                .withHour(22).withMinute(0).withSecond(0).withNano(0),
            utcTimeOutOfBed = OffsetDateTime.now()
                .withDayOfMonth(1).withMonth(5).withYear(2025)
                .withHour(6).withMinute(0).withSecond(0).withNano(0),
            timeZoneId = "America/New_York",
            totalTimeInBedMinutes = 480,
            feeling = MorningFeeling.GOOD,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val expectedResponse = SleepLogResponse(
            id = 1L,
            sleepDate = LocalDate.of(2025, 5, 1),
            timeToBed = LocalTime.of(22, 0),
            timeOutOfBed = LocalTime.of(6, 0),
            timeZoneId = "America/New_York",
            totalTimeInBedMinutes = 480,
            feeling = MorningFeeling.GOOD
        )

        `when`(sleepLogRepository.findFirstByUserIdOrderBySleepDateDesc(userId)).thenReturn(sleepLogEntity)
        `when`(sleepLogMapper.toResponse(sleepLogEntity)).thenReturn(expectedResponse)

        // When
        val result = getLatestSleepLogUseCase.getLatestSleepLog(userId)

        // Then
        assertThat(result).isEqualTo(expectedResponse)
        verify(sleepLogRepository).findFirstByUserIdOrderBySleepDateDesc(userId)
        verify(sleepLogMapper).toResponse(sleepLogEntity)
    }

    @Test
    fun `should throw SleepLogNotFoundByUserId when no sleep log is found for user`() {
        // Given
        val userId = 123L
        `when`(sleepLogRepository.findFirstByUserIdOrderBySleepDateDesc(userId)).thenReturn(null)

        // When
        val exception = assertThrows<SleepLogNotFoundByUserId> {
            getLatestSleepLogUseCase.getLatestSleepLog(userId)
        }

        // Then
        assertThat(exception.message).isEqualTo("No sleep logs found for user with id: 123")
    }
}