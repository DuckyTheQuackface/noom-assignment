package com.noom.interview.fullstack.sleep.sleeplog.controller

import com.noom.interview.fullstack.sleep.shared.constants.X_USER_ID
import com.noom.interview.fullstack.sleep.sleeplog.dto.request.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.sleeplog.dto.response.SleepLogResponse
import com.noom.interview.fullstack.sleep.sleeplog.usecase.CreateSleepLogUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/sleep-log")
class SleepLogController(
    private val createSleepLogUseCase: CreateSleepLogUseCase
) {
    @PostMapping
    fun createSleepLog(
        @RequestHeader(X_USER_ID) userId: Long,
        @RequestBody request: CreateSleepLogRequest
    ): ResponseEntity<SleepLogResponse> {
        val createdSleepLog = createSleepLogUseCase.createSleepLog(userId, request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdSleepLog)
    }
}
