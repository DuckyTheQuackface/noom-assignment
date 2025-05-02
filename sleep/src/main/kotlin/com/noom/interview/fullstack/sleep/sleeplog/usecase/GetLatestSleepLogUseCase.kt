package com.noom.interview.fullstack.sleep.sleeplog.usecase

import com.noom.interview.fullstack.sleep.sleeplog.dto.response.SleepLogResponse
import com.noom.interview.fullstack.sleep.sleeplog.exception.SleepLogNotFoundByUserId
import com.noom.interview.fullstack.sleep.sleeplog.mapper.SleepLogMapper
import com.noom.interview.fullstack.sleep.sleeplog.repository.SleepLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetLatestSleepLogUseCase(
    private val sleepLogRepository: SleepLogRepository,
    private val sleepLogMapper: SleepLogMapper
) {
    @Transactional(readOnly = true)
    fun getLatestSleepLog(userId: Long): SleepLogResponse {
        val latestSleepLog = sleepLogRepository.findFirstByUserIdOrderBySleepDateDesc(userId)
            ?: throw SleepLogNotFoundByUserId(userId)

        return sleepLogMapper.toResponse(latestSleepLog)
    }
}
