package com.noom.interview.fullstack.sleep.sleeplog.usecase

import com.noom.interview.fullstack.sleep.sleeplog.entity.MorningFeeling
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLogEntity
import com.noom.interview.fullstack.sleep.sleeplog.exception.SleepLogsNotFoundByUserIdSinceDate
import com.noom.interview.fullstack.sleep.sleeplog.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.user.entity.UserEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

@ExtendWith(MockitoExtension::class)
class GetSleepStatsUseCaseTest {

    @Mock
    private lateinit var sleepLogRepository: SleepLogRepository

    private lateinit var getSleepStatsUseCase: GetSleepStatsUseCase

    @BeforeEach
    fun setUp() {
        getSleepStatsUseCase = GetSleepStatsUseCase(
            sleepLogRepository = sleepLogRepository
        )
    }

    @Test
    fun `should return sleep stats when sleep logs are found for user in the last 30 days`() {
        // Given
        val userId = 123L
        val today = LocalDate.now()
        val thirtyDaysAgo = today.minusDays(30)
        val userEntity = UserEntity(id = userId, username = "Test User", timeZone = "America/New_York")

        val sleepLog1 = createSleepLogEntity(
            id = 1L,
            user = userEntity,
            sleepDate = today.minusDays(1),
            localTimeToBed = LocalTime.of(22, 0),
            localTimeOutOfBed = LocalTime.of(6, 0),
            totalMinutes = 480,
            feeling = MorningFeeling.GOOD
        )
        val sleepLog2 = createSleepLogEntity(
            id = 2L,
            user = userEntity,
            sleepDate = today.minusDays(2),
            localTimeToBed = LocalTime.of(23, 0),
            localTimeOutOfBed = LocalTime.of(7, 0),
            totalMinutes = 510,
            feeling = MorningFeeling.OK
        )
        val sleepLog3 = createSleepLogEntity(
            id = 3L,
            user = userEntity,
            sleepDate = today.minusDays(3),
            localTimeToBed = LocalTime.of(21, 30),
            localTimeOutOfBed = LocalTime.of(5, 30),
            totalMinutes = 450,
            feeling = MorningFeeling.GOOD
        )

        val sleepLogs = listOf(sleepLog1, sleepLog2, sleepLog3)

        `when`(sleepLogRepository.findByUserIdAndSleepDateIsGreaterThan(userId, thirtyDaysAgo))
            .thenReturn(sleepLogs)

        // Expected values
        // Average bed time: (22:00 + 23:00 + 21:30) / 3 = 22:10
        val expectedAvgBedTime = LocalTime.of(22, 10)
        // Average wake time: (6:00 + 7:00 + 5:30) / 3 = 6:10
        val expectedAvgWakeTime = LocalTime.of(6, 10)
        // Average time in bed: (480 + 510 + 450) / 3 = 480
        val expectedAvgTimeInBed = 480
        // Feeling frequencies: {GOOD=2, OK=1}
        val expectedFeelingFrequencies = mapOf(
            MorningFeeling.GOOD to 2,
            MorningFeeling.OK to 1
        )

        // When
        val result = getSleepStatsUseCase.getSleepStats(userId)

        // Then
        assertThat(result.startDate).isEqualTo(thirtyDaysAgo)
        assertThat(result.endDate).isEqualTo(today)
        assertThat(result.averageTimeInBedMinutes).isEqualTo(expectedAvgTimeInBed)
        assertThat(result.averageBedTime).isEqualTo(expectedAvgBedTime)
        assertThat(result.averageWakeTime).isEqualTo(expectedAvgWakeTime)
        assertThat(result.feelingFrequencies).isEqualTo(expectedFeelingFrequencies)

        verify(sleepLogRepository).findByUserIdAndSleepDateIsGreaterThan(userId, thirtyDaysAgo)
    }

    @Test
    fun `should throw SleepLogsNotFoundByUserIdSinceDate when no sleep logs are found for user in the last 30 days`() {
        // Given
        val userId = 123L
        val today = LocalDate.now()
        val thirtyDaysAgo = today.minusDays(30)

        `when`(sleepLogRepository.findByUserIdAndSleepDateIsGreaterThan(userId, thirtyDaysAgo))
            .thenReturn(emptyList())

        // When
        val exception = assertThrows<SleepLogsNotFoundByUserIdSinceDate> {
            getSleepStatsUseCase.getSleepStats(userId)
        }

        // Then
        assertThat(exception.message).isEqualTo("No sleep logs found for user with id $userId from $thirtyDaysAgo to today")
        verify(sleepLogRepository).findByUserIdAndSleepDateIsGreaterThan(userId, thirtyDaysAgo)
    }

    // Helper method to create sleep log entities with common fields
    private fun createSleepLogEntity(
        id: Long,
        user: UserEntity,
        sleepDate: LocalDate,
        localTimeToBed: LocalTime,
        localTimeOutOfBed: LocalTime,
        totalMinutes: Int,
        feeling: MorningFeeling
    ): SleepLogEntity {
        return SleepLogEntity(
            id = id,
            user = user,
            sleepDate = sleepDate,
            localTimeToBed = localTimeToBed,
            localTimeOutOfBed = localTimeOutOfBed,
            utcTimeToBed = OffsetDateTime.now()
                .withDayOfMonth(sleepDate.dayOfMonth)
                .withMonth(sleepDate.monthValue)
                .withYear(sleepDate.year)
                .withHour(localTimeToBed.hour)
                .withMinute(localTimeToBed.minute)
                .withSecond(0)
                .withNano(0),
            utcTimeOutOfBed = OffsetDateTime.now()
                .withDayOfMonth(sleepDate.dayOfMonth)
                .withMonth(sleepDate.monthValue)
                .withYear(sleepDate.year)
                .withHour(localTimeOutOfBed.hour)
                .withMinute(localTimeOutOfBed.minute)
                .withSecond(0)
                .withNano(0),
            timeZoneId = "America/New_York",
            totalTimeInBedMinutes = totalMinutes,
            feeling = feeling,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
}