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
            // US Spring Forward (March 2023) - America/New_York
            // On 2023-03-12, at 2am, clocks jump to 3am (losing an hour)
            Arguments.of(
                "sleeping through spring forward (losing an hour)",
                LocalDate.of(2023, 3, 11), // night before DST change
                LocalTime.of(23, 0),      // 11:00 PM
                LocalTime.of(7, 0),       // 7:00 AM next day
                "America/New_York",
                420, // 8 hours in local time (but only 7 hours in actual elapsed time)
                OffsetDateTime.of(2023, 3, 11, 23, 0, 0, 0, ZoneOffset.ofHours(-5)), // EST
                OffsetDateTime.of(2023, 3, 12, 7, 0, 0, 0, ZoneOffset.ofHours(-4))   // EDT
            ),

            // US Fall Back (November 2023) - America/New_York
            // On 2023-11-05, at 2am, clocks go back to 1am (gaining an hour)
            Arguments.of(
                "sleeping through fall back (gaining an hour)",
                LocalDate.of(2023, 11, 4), // night before DST change
                LocalTime.of(23, 0),      // 11:00 PM
                LocalTime.of(7, 0),       // 7:00 AM next day
                "America/New_York",
                540, // 8 hours in local time (but actually 9 hours in elapsed time)
                OffsetDateTime.of(2023, 11, 4, 23, 0, 0, 0, ZoneOffset.ofHours(-4)), // EDT
                OffsetDateTime.of(2023, 11, 5, 7, 0, 0, 0, ZoneOffset.ofHours(-5))   // EST
            ),

            // Europe Spring Forward (March 2023) - Europe/London
            // On 2023-03-26, at 1am, clocks jump to 2am (losing an hour)
            Arguments.of(
                "sleeping through European DST spring forward",
                LocalDate.of(2023, 3, 25), // night before DST change
                LocalTime.of(23, 0),      // 11:00 PM
                LocalTime.of(7, 0),       // 7:00 AM next day
                "Europe/London",
                420, // 8 hours in local time (but only 7 hours in actual elapsed time)
                OffsetDateTime.of(2023, 3, 25, 23, 0, 0, 0, ZoneOffset.ofHours(0)),  // GMT
                OffsetDateTime.of(2023, 3, 26, 7, 0, 0, 0, ZoneOffset.ofHours(1))    // BST
            ),

            // Europe Fall Back (October 2023) - Europe/London
            // On 2023-10-29, at 2am, clocks go back to 1am (gaining an hour)
            Arguments.of(
                "sleeping through European DST fall back",
                LocalDate.of(2023, 10, 28), // night before DST change
                LocalTime.of(23, 0),       // 11:00 PM
                LocalTime.of(7, 0),        // 7:00 AM next day
                "Europe/London",
                540, // 8 hours in local time (but actually 9 hours in elapsed time)
                OffsetDateTime.of(2023, 10, 28, 23, 0, 0, 0, ZoneOffset.ofHours(1)), // BST
                OffsetDateTime.of(2023, 10, 29, 7, 0, 0, 0, ZoneOffset.ofHours(0))   // GMT
            ),

            // Australia DST starts (October) - Sydney
            // On 2023-10-01, at 2am, clocks jump to 3am (losing an hour)
            Arguments.of(
                "sleeping through Australian DST start",
                LocalDate.of(2023, 9, 30), // night before DST change
                LocalTime.of(23, 0),      // 11:00 PM
                LocalTime.of(7, 0),       // 7:00 AM next day
                "Australia/Sydney",
                420, // 8 hours in local time (but only 7 hours in actual elapsed time)
                OffsetDateTime.of(2023, 9, 30, 23, 0, 0, 0, ZoneOffset.ofHours(10)), // AEST
                OffsetDateTime.of(2023, 10, 1, 7, 0, 0, 0, ZoneOffset.ofHours(11))   // AEDT
            ),

            // Australia DST ends (April) - Sydney
            // On 2023-04-02, at 3am, clocks go back to 2am (gaining an hour)
            Arguments.of(
                "sleeping through Australian DST end",
                LocalDate.of(2023, 4, 1), // night before DST change
                LocalTime.of(23, 0),     // 11:00 PM
                LocalTime.of(7, 0),      // 7:00 AM next day
                "Australia/Sydney",
                540, // 8 hours in local time (but actually 9 hours in elapsed time)
                OffsetDateTime.of(2023, 4, 1, 23, 0, 0, 0, ZoneOffset.ofHours(11)), // AEDT
                OffsetDateTime.of(2023, 4, 2, 7, 0, 0, 0, ZoneOffset.ofHours(10))   // AEST
            )
        )
    }
}