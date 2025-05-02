package com.noom.interview.fullstack.sleep

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.time.ZoneOffset
import java.util.TimeZone

@SpringBootApplication
class SleepApplication {
	companion object {
		const val UNIT_TEST_PROFILE = "unittest"
	}
}

fun main(args: Array<String>) {
	TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC))
	runApplication<SleepApplication>(*args)
}
