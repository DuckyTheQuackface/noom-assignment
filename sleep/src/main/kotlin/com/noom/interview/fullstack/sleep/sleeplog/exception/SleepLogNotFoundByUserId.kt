package com.noom.interview.fullstack.sleep.sleeplog.exception

import com.noom.interview.fullstack.sleep.shared.exception.ResourceNotFoundException

class SleepLogNotFoundByUserId(
    userId: Long
) : ResourceNotFoundException(
    "No sleep logs found for user with id: $userId"
)
