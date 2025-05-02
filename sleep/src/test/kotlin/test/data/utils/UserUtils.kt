package test.data.utils

import com.noom.interview.fullstack.sleep.user.entity.UserEntity

object UserUtils {

    fun createTestUser(
        username: String = "testuser",
        timeZone: String = "America/New_York"
    ): UserEntity {
        return UserEntity(
            username = username,
            timeZone = timeZone
        )
    }
}
