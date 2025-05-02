package com.noom.interview.fullstack.sleep.sleeplog.repository

import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLogEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SleepLogRepository : JpaRepository<SleepLogEntity, Long>
