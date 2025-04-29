package com.noom.interview.fullstack.sleep.sleeplog.repository

import com.noom.interview.fullstack.sleep.sleeplog.model.SleepLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface SleepLogRepository : JpaRepository<SleepLog, Long> {

    fun findFirstByUserIdOrderBySleepDateDesc(userId: Long): SleepLog?

    fun findByUserIdAndSleepDateBetweenOrderBySleepDateDesc(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<SleepLog>
}
