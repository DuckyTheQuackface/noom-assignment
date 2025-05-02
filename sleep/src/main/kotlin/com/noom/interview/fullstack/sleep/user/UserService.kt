package com.noom.interview.fullstack.sleep.user

import com.noom.interview.fullstack.sleep.shared.exception.ResourceNotFoundException
import com.noom.interview.fullstack.sleep.user.entity.UserEntity
import com.noom.interview.fullstack.sleep.user.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun getUserById(userId: Long): UserEntity {
        return userRepository.findByIdOrNull(userId)
            ?: throw ResourceNotFoundException("User not found with id: $userId")
    }
}
