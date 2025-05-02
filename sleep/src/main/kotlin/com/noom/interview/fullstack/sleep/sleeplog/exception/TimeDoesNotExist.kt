package com.noom.interview.fullstack.sleep.sleeplog.exception

import java.time.LocalTime

class TimeDoesNotExist(
    time: LocalTime
) : IllegalArgumentException(
    "Time $time does not exist due to DST transition"
)
