package com.noom.interview.fullstack.sleep.db.repositorty

import com.noom.interview.fullstack.sleep.db.entity.MorningFeeling
import com.noom.interview.fullstack.sleep.db.entity.SleepLog
import com.noom.interview.fullstack.sleep.db.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): Optional<User>
}

@Repository
interface SleepLogRepository : JpaRepository<SleepLog, Long> {

    fun findFirstByUserIdOrderBySleepDateDesc(userId: Long): Optional<SleepLog>

    fun findByUserIdAndSleepDateBetweenOrderBySleepDateDesc(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<SleepLog>
}

interface FeelingCount {
    val feeling: MorningFeeling
    val count: Long
}
