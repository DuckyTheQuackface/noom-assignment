package com.noom.interview.fullstack.sleep.sleeplog.entity

import com.noom.interview.fullstack.sleep.user.entity.UserEntity
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "sleep_log")
@TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType::class)
data class SleepLogEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Column(name = "sleep_date", nullable = false)
    val sleepDate: LocalDate,

    @Column(name = "local_time_to_bed", nullable = false)
    val localTimeToBed: LocalTime,

    @Column(name = "local_time_out_of_bed", nullable = false)
    val localTimeOutOfBed: LocalTime,

    @Column(name = "utc_time_to_bed", nullable = false)
    val utcTimeToBed: OffsetDateTime,

    @Column(name = "utc_time_out_of_bed", nullable = false)
    val utcTimeOutOfBed: OffsetDateTime,

    @Column(name = "time_zone_id", nullable = false)
    val timeZoneId: String,

    @Column(name = "total_time_in_bed_minutes", nullable = false)
    val totalTimeInBedMinutes: Int,

    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val feeling: MorningFeeling,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now()
)

enum class MorningFeeling {
    BAD, OK, GOOD
}
