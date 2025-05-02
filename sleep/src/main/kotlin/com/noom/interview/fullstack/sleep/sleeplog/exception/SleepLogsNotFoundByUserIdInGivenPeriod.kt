package com.noom.interview.fullstack.sleep.sleeplog.exception

import com.noom.interview.fullstack.sleep.shared.exception.ResourceNotFoundException
import java.time.LocalDate

class SleepLogsNotFoundByUserIdSinceDate(
    userId: Long,
    date: LocalDate
) : ResourceNotFoundException(
    "No sleep logs found for user with id $userId from $date to today"
)
