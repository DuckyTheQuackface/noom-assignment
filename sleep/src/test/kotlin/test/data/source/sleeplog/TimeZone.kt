package test.data.source.sleeplog

import org.junit.jupiter.params.provider.Arguments
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.stream.Stream

object TimeZone {
    fun stream(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                "logging at the start of month in NYC",
                LocalDate.of(2025, 5, 1),
                LocalTime.of(23, 0),
                LocalTime.of(7, 0),
                "America/New_York",
                480,
                OffsetDateTime.of(2025, 5, 1, 23, 0, 0, 0, ZoneOffset.ofHours(-4)),
                OffsetDateTime.of(2025, 5, 2, 7, 0, 0, 0, ZoneOffset.ofHours(-4))
            ),

            Arguments.of(
                "logging at the end of year in Tokyo",
                LocalDate.of(2024, 12, 31),
                LocalTime.of(23, 0),
                LocalTime.of(7, 0),
                "Asia/Tokyo",
                480,
                OffsetDateTime.of(2024, 12, 31, 23, 0, 0, 0, ZoneOffset.ofHours(9)),
                OffsetDateTime.of(2025, 1, 1, 7, 0, 0, 0, ZoneOffset.ofHours(9))
            ),

            Arguments.of(
                "logging sleep in Sydney",
                LocalDate.of(2023, 5, 8),
                LocalTime.of(22, 0),
                LocalTime.of(6, 0),
                "Australia/Sydney",
                480,
                OffsetDateTime.of(2023, 5, 8, 22, 0, 0, 0, ZoneOffset.ofHours(10)),
                OffsetDateTime.of(2023, 5, 9, 6, 0, 0, 0, ZoneOffset.ofHours(10))
            ),

            Arguments.of(
                "logging sleep in London",
                LocalDate.of(2023, 5, 8),
                LocalTime.of(23, 0),
                LocalTime.of(7, 0),
                "Europe/London",
                480, // 8 hours = 480 minutes
                OffsetDateTime.of(2023, 5, 8, 23, 0, 0, 0, ZoneOffset.ofHours(1)),
                OffsetDateTime.of(2023, 5, 9, 7, 0, 0, 0, ZoneOffset.ofHours(1))
            ),

            Arguments.of(
                "logging sleep in New Delhi with half-hour offset",
                LocalDate.of(2023, 5, 8),
                LocalTime.of(23, 0),
                LocalTime.of(7, 0),
                "Asia/Kolkata",
                480, // 8 hours = 480 minutes
                OffsetDateTime.of(2023, 5, 8, 23, 0, 0, 0, ZoneOffset.ofHoursMinutes(5, 30)),
                OffsetDateTime.of(2023, 5, 9, 7, 0, 0, 0, ZoneOffset.ofHoursMinutes(5, 30))
            ),

            Arguments.of(
                "logging sleep in Auckland (extreme eastern timezone)",
                LocalDate.of(2023, 5, 8),
                LocalTime.of(22, 30),
                LocalTime.of(6, 30),
                "Pacific/Auckland",
                480, // 8 hours = 480 minutes
                OffsetDateTime.of(2023, 5, 8, 22, 30, 0, 0, ZoneOffset.ofHours(12)),
                OffsetDateTime.of(2023, 5, 9, 6, 30, 0, 0, ZoneOffset.ofHours(12))
            ),

            Arguments.of(
                "logging sleep in Honolulu (extreme western timezone)",
                LocalDate.of(2023, 5, 8),
                LocalTime.of(21, 0),
                LocalTime.of(5, 0),
                "Pacific/Honolulu",
                480, // 8 hours = 480 minutes
                OffsetDateTime.of(2023, 5, 8, 21, 0, 0, 0, ZoneOffset.ofHours(-10)),
                OffsetDateTime.of(2023, 5, 9, 5, 0, 0, 0, ZoneOffset.ofHours(-10))
            )
        )
    }
}