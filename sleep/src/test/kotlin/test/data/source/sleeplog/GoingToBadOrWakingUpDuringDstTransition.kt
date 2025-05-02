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
            // Will throw exception
            Arguments.of(
                "going to bed during dst start us at non existent hour",
                LocalDate.of(2023, 3, 11),
                LocalTime.of(2, 30),
                LocalTime.of(9, 30),
                "America/New_York",
                480,
                OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
            ),
            Arguments.of(
                "going to bed before and waking up after dst start us",
                LocalDate.of(2023, 3, 11),
                LocalTime.of(23, 0),
                LocalTime.of(3, 30),
                "America/New_York",
                210,
                OffsetDateTime.of(2023, 3, 11, 23, 0, 0, 0, ZoneOffset.ofHours(-5)),
                OffsetDateTime.of(2023, 3, 12, 3, 30, 0, 0, ZoneOffset.ofHours(-4))
            ),
            Arguments.of(
                "going to bed during dst end us ambiguous hour",
                LocalDate.of(2023, 11, 4),
                LocalTime.of(1, 30),
                LocalTime.of(9, 30),
                "America/New_York",
                540,
                OffsetDateTime.of(2023, 11, 5, 1, 30, 0, 0, ZoneOffset.ofHours(-4)),
                OffsetDateTime.of(2023, 11, 5, 9, 30, 0, 0, ZoneOffset.ofHours(-5))
            ),
            Arguments.of(
                "waking up during dst end us repeated hour",
                LocalDate.of(2023, 11, 4),
                LocalTime.of(21, 0),
                LocalTime.of(1, 30),
                "America/New_York",
                270,
                OffsetDateTime.of(2023, 11, 4, 21, 0, 0, 0, ZoneOffset.ofHours(-4)),
                OffsetDateTime.of(2023, 11, 5, 1, 30, 0, 0, ZoneOffset.ofHours(-4))
            ),
            // Will throw exception
            Arguments.of(
                "waking up during dst start europe skipped hour",
                LocalDate.of(2025, 3, 29),
                LocalTime.of(23, 0),
                LocalTime.of(2, 30),
                "Europe/Berlin",
                210,
                OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
            ),
            Arguments.of(
                "short sleep spanning dst end europe",
                LocalDate.of(2023, 10, 28),
                LocalTime.of(1, 30),
                LocalTime.of(3, 30),
                "Europe/London",
                180,
                OffsetDateTime.of(2023, 10, 29, 1, 30, 0, 0, ZoneOffset.ofHours(1)),
                OffsetDateTime.of(2023, 10, 29, 3, 30, 0, 0, ZoneOffset.ofHours(0))
            )
        )
    }
}
