package com.noom.interview.fullstack.sleep.db.repositorty

import com.noom.interview.fullstack.sleep.db.entity.MorningFeeling
import com.noom.interview.fullstack.sleep.db.entity.SleepLog
import com.noom.interview.fullstack.sleep.db.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): Optional<User>
}

@Repository
interface SleepLogRepository : JpaRepository<SleepLog, Long> {
    /**
     * Find the most recent sleep log for a specific user
     */
    fun findFirstByUserIdOrderBySleepDateDesc(userId: Long): Optional<SleepLog>

    /**
     * Find all sleep logs for a specific user within a date range
     */
    fun findByUserIdAndSleepDateBetweenOrderBySleepDateDesc(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<SleepLog>

    /**
     * Count occurrences of each feeling value for a user within a date range
     */
    @Query("""
        SELECT s.feeling as feeling, COUNT(s.id) as count 
        FROM SleepLog s 
        WHERE s.user.id = :userId 
        AND s.sleepDate BETWEEN :startDate AND :endDate 
        GROUP BY s.feeling
    """)
    fun countFeelingsByUserIdAndDateRange(
        @Param("userId") userId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<FeelingCount>

    /**
     * Calculate average time in bed for a user within a date range
     */
    @Query("""
        SELECT AVG(s.totalTimeInBed) 
        FROM SleepLog s 
        WHERE s.user.id = :userId 
        AND s.sleepDate BETWEEN :startDate AND :endDate
    """)
    fun calculateAverageTimeInBed(
        @Param("userId") userId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): Double
}

interface FeelingCount {
    val feeling: MorningFeeling
    val count: Long
}
