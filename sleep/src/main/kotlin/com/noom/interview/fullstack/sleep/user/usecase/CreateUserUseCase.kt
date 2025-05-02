package com.noom.interview.fullstack.sleep.user.usecase

import com.noom.interview.fullstack.sleep.user.dto.request.CreateUserRequest
import com.noom.interview.fullstack.sleep.user.entity.UserEntity
import com.noom.interview.fullstack.sleep.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class CreateUserUseCase(
    private val userRepository: UserRepository
) {
    fun createUser(request: CreateUserRequest) {
        userRepository.save(
            UserEntity(
                username = request.username,
                timeZone = request.timeZone
            )
        )
    }
}
