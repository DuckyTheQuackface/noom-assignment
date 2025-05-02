package com.noom.interview.fullstack.sleep.user.controller

import com.noom.interview.fullstack.sleep.user.dto.request.CreateUserRequest
import com.noom.interview.fullstack.sleep.user.usecase.CreateUserUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val createUserUseCase: CreateUserUseCase
) {
    @PostMapping
    fun createSleepLog(
        @RequestBody request: CreateUserRequest
    ): ResponseEntity<Unit> {
        createUserUseCase.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
}
