package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.dto.SleepStatsResponse
import com.noom.interview.fullstack.sleep.service.SleepLogService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users/{userId}/sleep-logs")
class SleepLogController(private val sleepLogService: SleepLogService) {

    @PostMapping
    fun createSleepLog(
        @PathVariable userId: Long,
        @RequestBody request: CreateSleepLogRequest
    ): ResponseEntity<SleepLogResponse> {
        val createdSleepLog = sleepLogService.createSleepLog(userId, request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdSleepLog)
    }

    @GetMapping("/latest")
    fun getLatestSleepLog(@PathVariable userId: Long): ResponseEntity<SleepLogResponse> {
        val latestSleepLog = sleepLogService.getLatestSleepLog(userId)
        return ResponseEntity.ok(latestSleepLog)
    }

    @GetMapping("/stats")
    fun getSleepStats(@PathVariable userId: Long): ResponseEntity<SleepStatsResponse> {
        val sleepStats = sleepLogService.getSleepStats(userId)
        return ResponseEntity.ok(sleepStats)
    }
}