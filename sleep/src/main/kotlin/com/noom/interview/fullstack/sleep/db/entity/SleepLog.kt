package com.noom.interview.fullstack.sleep.db.entity

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.time.LocalDate
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
@Table(name = "sleep_logs")
@TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType::class)
data class SleepLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "sleep_date", nullable = false)
    val sleepDate: LocalDate,

    @Column(name = "time_to_bed", nullable = false)
    val timeToBed: OffsetDateTime,

    @Column(name = "time_out_of_bed", nullable = false)
    val timeOutOfBed: OffsetDateTime,

    @Column(name = "total_time_in_bed", nullable = false)
    val totalTimeInBed: Int, // Duration in minutes

    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val feeling: MorningFeeling,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
)

enum class MorningFeeling {
    BAD, OK, GOOD
}
