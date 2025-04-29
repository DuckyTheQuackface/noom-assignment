package com.noom.interview.fullstack.sleep.sleeplog.controller

import com.noom.interview.fullstack.sleep.shared.constants.X_USER_ID
import com.noom.interview.fullstack.sleep.sleeplog.SleepLogService
import com.noom.interview.fullstack.sleep.sleeplog.dto.request.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.sleeplog.dto.response.SleepLogResponse
import com.noom.interview.fullstack.sleep.sleeplog.dto.response.SleepStatsResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/sleep-logs")
class SleepLogController(
    private val sleepLogService: SleepLogService
) {
    @PostMapping
    fun createSleepLog(
        @RequestHeader(X_USER_ID) userId: Long,
        @RequestBody request: CreateSleepLogRequest
    ): ResponseEntity<SleepLogResponse> {
        val createdSleepLog = sleepLogService.createSleepLog(userId, request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdSleepLog)
    }

    @GetMapping("/latest")
    fun getLatestSleepLog(
        @RequestHeader(X_USER_ID) userId: Long,
    ): ResponseEntity<SleepLogResponse> {
        val latestSleepLog = sleepLogService.getLatestSleepLog(userId)
        return ResponseEntity.ok(latestSleepLog)
    }

    @GetMapping("/stats")
    fun getSleepStats(
        @RequestHeader(X_USER_ID) userId: Long,
    ): ResponseEntity<SleepStatsResponse> {
        val sleepStats = sleepLogService.getSleepStats(userId)
        return ResponseEntity.ok(sleepStats)
    }
}
