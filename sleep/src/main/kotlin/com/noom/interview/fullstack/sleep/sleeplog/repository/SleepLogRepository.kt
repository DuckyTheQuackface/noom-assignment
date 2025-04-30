package com.noom.interview.fullstack.sleep.sleeplog.repository

import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLogEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface SleepLogRepository : JpaRepository<SleepLogEntity, Long> {

    fun findFirstByUserIdOrderBySleepDateDesc(userId: Long): SleepLogEntity?

    fun findByUserIdAndSleepDateIsGreaterThan(
        userId: Long,
        sleepDateAfter: LocalDate
    ): List<SleepLogEntity>
}
