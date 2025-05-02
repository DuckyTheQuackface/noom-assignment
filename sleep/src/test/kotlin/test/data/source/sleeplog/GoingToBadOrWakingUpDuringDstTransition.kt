package test.data.source.sleeplog

import org.junit.jupiter.params.provider.Arguments
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.stream.Stream

object GoingToBadOrWakingUpDuringDstTransition {
    fun stream(): Stream<Arguments> {
        return Stream.of(
            // US Spring Forward (March 2023) - Going to bed right at 2:00 AM transition (non-existent hour)
            // When clock jumps from 1:59 AM to 3:00 AM
            Arguments.of(
                "going to bed during spring forward (at non-existent hour)",
                LocalDate.of(2023, 3, 11), // Night before transition
                LocalTime.of(2, 30), // 1:30 AM (before transition)
                LocalTime.of(9, 30), // 9:30 AM
                "America/New_York",
                480, // 8 hours in local time
                OffsetDateTime.of(2023, 3, 12, 1, 30, 0, 0, ZoneOffset.ofHours(-5)), // EST
                OffsetDateTime.of(2023, 3, 12, 9, 30, 0, 0, ZoneOffset.ofHours(-4))  // EDT
            ),

            // US Spring Forward - Waking up after transition
            Arguments.of(
                "going to bed before and waking up after spring forward",
                LocalDate.of(2023, 3, 11),
                LocalTime.of(23, 0), // 11:00 PM (before transition day)
                LocalTime.of(3, 30), // 3:30 AM (after transition)
                "America/New_York",
                210, // 4.5 hours (accounting for lost hour)
                OffsetDateTime.of(2023, 3, 11, 23, 0, 0, 0, ZoneOffset.ofHours(-5)), // EST
                OffsetDateTime.of(2023, 3, 12, 3, 30, 0, 0, ZoneOffset.ofHours(-4))  // EDT
            ),

            // US Fall Back (November 2023) - Going to bed during ambiguous hour
            // At 2:00 AM, clocks go back to 1:00 AM, creating an ambiguous hour
            Arguments.of(
                "going to bed during fall back ambiguous hour",
                LocalDate.of(2023, 11, 4),
                LocalTime.of(1, 30), // 1:30 AM (in the ambiguous hour)
                LocalTime.of(9, 30), // 9:30 AM
                "America/New_York",
                540, // 9 hours - take the earlier hour
                OffsetDateTime.of(2023, 11, 5, 1, 30, 0, 0, ZoneOffset.ofHours(-4)), // First 1:30 AM (EDT)
                OffsetDateTime.of(2023, 11, 5, 9, 30, 0, 0, ZoneOffset.ofHours(-5))  // EST
            ),

            // US Fall Back - Waking up during the repeated hour
            Arguments.of(
                "waking up during fall back repeated hour",
                LocalDate.of(2023, 11, 4),
                LocalTime.of(21, 0), // 9:00 PM
                LocalTime.of(1, 30), // 1:30 AM (in repeated hour)
                "America/New_York",
                270, // 4.5 hours (taking the earlier hour)
                OffsetDateTime.of(2023, 11, 4, 21, 0, 0, 0, ZoneOffset.ofHours(-4)), // EDT
                OffsetDateTime.of(2023, 11, 5, 1, 30, 0, 0, ZoneOffset.ofHours(-4))  // EST (first 1:30 AM)
            ),

            // European DST - "Skipped hour" scenario (going to sleep before transition, waking during "skipped" hour)
            Arguments.of(
                "waking up at skipped hour during European DST change",
                LocalDate.of(2025, 3, 29),
                LocalTime.of(23, 0), // 11:00 PM
                LocalTime.of(2, 30), // 2:30 AM (skipped hour - clocks jumped from 1:00 to 3:00)
                "Europe/Berlin",
                210, // 3.5 hours (accounting for lost hour)
                OffsetDateTime.of(1999, 3, 25, 23, 0, 0, 0, ZoneOffset.ofHours(0)), // GMT
                OffsetDateTime.of(1999, 3, 26, 2, 30, 0, 0, ZoneOffset.ofHours(1))  // BST
            ),

            // European DST Fall Back - Very short sleep that spans exactly the transition
            Arguments.of(
                "short sleep spanning exactly the European DST fall transition",
                LocalDate.of(2023, 10, 28),
                LocalTime.of(1, 30), // 1:30 AM
                LocalTime.of(3, 30), // 3:30 AM (after repeated hour)
                "Europe/London",
                180, // 3 hours (accounting for repeated hour)
                OffsetDateTime.of(2023, 10, 29, 1, 30, 0, 0, ZoneOffset.ofHours(1)), // BST
                OffsetDateTime.of(2023, 10, 29, 3, 30, 0, 0, ZoneOffset.ofHours(0))  // GMT
            )
        )
    }
}