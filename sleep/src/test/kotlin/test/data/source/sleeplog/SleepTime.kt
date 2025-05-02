package test.data.source.sleeplog

import org.junit.jupiter.params.provider.Arguments
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.stream.Stream

object SleepTime {
    fun stream(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                "sleeping from evening to morning",
                LocalDate.of(2023, 5, 8),
                LocalTime.of(23, 0),
                LocalTime.of(7, 0),
                "America/New_York",
                480,
                OffsetDateTime.of(2023, 5, 8, 23, 0, 0, 0, ZoneOffset.ofHours(-4)),
                OffsetDateTime.of(2023, 5, 9, 7, 0, 0, 0, ZoneOffset.ofHours(-4))
            ),

            Arguments.of(
                "sleeping from after midnight to morning",
                LocalDate.of(2023, 5, 8),
                LocalTime.of(1, 0),
                LocalTime.of(8, 0),
                "America/New_York",
                420,
                OffsetDateTime.of(2023, 5, 9, 1, 0, 0, 0, ZoneOffset.ofHours(-4)),
                OffsetDateTime.of(2023, 5, 9, 8, 0, 0, 0, ZoneOffset.ofHours(-4))
            ),

            Arguments.of(
                "sleeping from evening to after midnight",
                LocalDate.of(2023, 5, 8),
                LocalTime.of(22, 0),
                LocalTime.of(2, 0),
                "America/New_York",
                240,
                OffsetDateTime.of(2023, 5, 8, 22, 0, 0, 0, ZoneOffset.ofHours(-4)),
                OffsetDateTime.of(2023, 5, 9, 2, 0, 0, 0, ZoneOffset.ofHours(-4))
            ),

            Arguments.of(
                "sleeping from midnight exactly",
                LocalDate.of(2023, 5, 8),
                LocalTime.MIDNIGHT,
                LocalTime.of(8, 0),
                "America/New_York",
                480,
                OffsetDateTime.of(2023, 5, 9, 0, 0, 0, 0, ZoneOffset.ofHours(-4)),
                OffsetDateTime.of(2023, 5, 9, 8, 0, 0, 0, ZoneOffset.ofHours(-4))
            ),

            // Noon sleep (daytime sleep)
            Arguments.of(
                "sleeping during daytime (noon)",
                LocalDate.of(2023, 5, 8),
                LocalTime.NOON,
                LocalTime.of(20, 0),
                "America/New_York",
                480,
                OffsetDateTime.of(2023, 5, 9, 12, 0, 0, 0, ZoneOffset.ofHours(-4)),
                OffsetDateTime.of(2023, 5, 9, 20, 0, 0, 0, ZoneOffset.ofHours(-4))
            ),

            // Minimal allowed sleep
            Arguments.of(
                "sleeping for minimum allowed duration",
                LocalDate.of(2023, 5, 8),
                LocalTime.of(23, 0),
                LocalTime.of(1, 0),
                "America/New_York",
                120,
                OffsetDateTime.of(2023, 5, 8, 23, 0, 0, 0, ZoneOffset.ofHours(-4)),
                OffsetDateTime.of(2023, 5, 9, 1, 0, 0, 0, ZoneOffset.ofHours(-4))
            ),

            // Maximum allowed sleep
            Arguments.of(
                "sleeping for maximum allowed duration",
                LocalDate.of(2023, 5, 8),
                LocalTime.of(20, 0),
                LocalTime.of(12, 0),
                "America/New_York",
                960,
                OffsetDateTime.of(2023, 5, 8, 20, 0, 0, 0, ZoneOffset.ofHours(-4)),
                OffsetDateTime.of(2023, 5, 9, 12, 0, 0, 0, ZoneOffset.ofHours(-4))
            )
        )
    }
}