package com.noom.interview.fullstack.sleep.sleeplog.usecase

import com.noom.interview.fullstack.sleep.sleeplog.dto.response.SleepStatsResponse
import com.noom.interview.fullstack.sleep.sleeplog.exception.SleepLogsNotFoundByUserIdSinceDate
import com.noom.interview.fullstack.sleep.sleeplog.repository.SleepLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime

@Service
class GetSleepStatsUseCase(
    private val sleepLogRepository: SleepLogRepository
) {
    @Transactional(readOnly = true)
    fun getSleepStats(userId: Long): SleepStatsResponse {
        val today = LocalDate.now()
        val thirtyDaysAgo = today.minusDays(30)
        val sleepLogs = sleepLogRepository.findByUserIdAndSleepDateIsGreaterThan(
            userId = userId,
            sleepDateAfter = thirtyDaysAgo
        )
        if (sleepLogs.isEmpty()) {
            throw SleepLogsNotFoundByUserIdSinceDate(
                userId = userId,
                date = thirtyDaysAgo
            )
        }

        val avgTimeInBed = sleepLogs
            .map { it.totalTimeInBedMinutes }
            .average()
            .toInt()
        val avgBedTime = calculateAverageLocalTime(
            sleepLogs.map { it.localTimeToBed }
        )
        val avgWakeTime = calculateAverageLocalTime(
            sleepLogs.map { it.localTimeOutOfBed }
        )
        val feelingFrequencies = sleepLogs
            .groupBy { it.feeling }
            .mapValues { it.value.size }

        return SleepStatsResponse(
            startDate = thirtyDaysAgo,
            endDate = today,
            averageTimeInBedMinutes = avgTimeInBed,
            averageBedTime = avgBedTime,
            averageWakeTime = avgWakeTime,
            feelingFrequencies = feelingFrequencies
        )
    }

    private fun calculateAverageLocalTime(times: List<LocalTime>): LocalTime {
        val secondsFromMidnight = times.map {
            it.toSecondOfDay().toLong()
        }
        val avgSeconds = secondsFromMidnight.average().toLong()
        return LocalTime.ofSecondOfDay(avgSeconds)
    }
}
