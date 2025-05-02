package test.data.source.sleeplog

import org.junit.jupiter.params.provider.Arguments
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.stream.Stream

object SleepThroughDaylightSavingTime {
    fun stream(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                "sleeping through dst start us",
                LocalDate.of(2023, 3, 11),
                LocalTime.of(23, 0),
                LocalTime.of(7, 0),
                "America/New_York",
                420,
                OffsetDateTime.of(2023, 3, 11, 23, 0, 0, 0, ZoneOffset.ofHours(-5)),
                OffsetDateTime.of(2023, 3, 12, 7, 0, 0, 0, ZoneOffset.ofHours(-4))
            ),
            Arguments.of(
                "sleeping through dst end us",
                LocalDate.of(2023, 11, 4),
                LocalTime.of(23, 0),
                LocalTime.of(7, 0),
                "America/New_York",
                540,
                OffsetDateTime.of(2023, 11, 4, 23, 0, 0, 0, ZoneOffset.ofHours(-4)),
                OffsetDateTime.of(2023, 11, 5, 7, 0, 0, 0, ZoneOffset.ofHours(-5))
            ),

            Arguments.of(
                "sleeping through dst start europe",
                LocalDate.of(2023, 3, 25),
                LocalTime.of(23, 0),
                LocalTime.of(7, 0),
                "Europe/London",
                420,
                OffsetDateTime.of(2023, 3, 25, 23, 0, 0, 0, ZoneOffset.ofHours(0)),
                OffsetDateTime.of(2023, 3, 26, 7, 0, 0, 0, ZoneOffset.ofHours(1))
            ),
            Arguments.of(
                "sleeping through dst end europe",
                LocalDate.of(2023, 10, 28),
                LocalTime.of(23, 0),
                LocalTime.of(7, 0),
                "Europe/London",
                540,
                OffsetDateTime.of(2023, 10, 28, 23, 0, 0, 0, ZoneOffset.ofHours(1)),
                OffsetDateTime.of(2023, 10, 29, 7, 0, 0, 0, ZoneOffset.ofHours(0))
            ),

            Arguments.of(
                "sleeping through dst start australia",
                LocalDate.of(2023, 9, 30),
                LocalTime.of(23, 0),
                LocalTime.of(7, 0),
                "Australia/Sydney",
                420,
                OffsetDateTime.of(2023, 9, 30, 23, 0, 0, 0, ZoneOffset.ofHours(10)),
                OffsetDateTime.of(2023, 10, 1, 7, 0, 0, 0, ZoneOffset.ofHours(11))
            ),
            Arguments.of(
                "sleeping through dst end australia",
                LocalDate.of(2023, 4, 1),
                LocalTime.of(23, 0),
                LocalTime.of(7, 0),
                "Australia/Sydney",
                540,
                OffsetDateTime.of(2023, 4, 1, 23, 0, 0, 0, ZoneOffset.ofHours(11)),
                OffsetDateTime.of(2023, 4, 2, 7, 0, 0, 0, ZoneOffset.ofHours(10))
            )
        )
    }
}
