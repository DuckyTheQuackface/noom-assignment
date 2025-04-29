package com.noom.interview.fullstack.sleep

import com.noom.interview.fullstack.sleep.sleeplog.entity.MorningFeeling
import com.noom.interview.fullstack.sleep.user.entity.UserEntity
import com.noom.interview.fullstack.sleep.sleeplog.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.user.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SleepLogControllerIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var sleepLogRepository: SleepLogRepository

    private lateinit var testUserEntity: UserEntity

    @BeforeEach
    fun setup() {
        // Clean up any existing data
        sleepLogRepository.deleteAll()
        userRepository.deleteAll()

        // Create a test user
        testUserEntity = userRepository.save(TestDataUtils.createTestUser())
    }

    @AfterEach
    fun cleanup() {
        sleepLogRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `should create a sleep log`() {
        // Given
        val sleepDate = LocalDate.now().minusDays(1)
        val timeToBed = LocalTime.of(22, 30)
        val timeOutOfBed = LocalTime.of(6, 45)
        val request = TestDataUtils.createSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            feeling = MorningFeeling.GOOD
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/sleep-logs")
                .header("UserId", testUserEntity.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.sleepDate").value(sleepDate.toString()))
            .andExpect(jsonPath("$.timeToBed").value(timeToBed.format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
            .andExpect(jsonPath("$.timeOutOfBed").value(timeOutOfBed.format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
            .andExpect(jsonPath("$.totalTimeInBedMinutes").value(495))
            .andExpect(jsonPath("$.feeling").value("GOOD"))
    }


    @Test
    fun `should get latest sleep log`() {
        // Given
        val sleepLog1 = TestDataUtils.createSleepLog(
            userEntity = testUserEntity,
            sleepDate = LocalDate.now().minusDays(2)
        )
        val sleepLog2 = TestDataUtils.createSleepLog(
            userEntity = testUserEntity,
            sleepDate = LocalDate.now().minusDays(1)
        )

        sleepLogRepository.save(sleepLog1)
        sleepLogRepository.save(sleepLog2)

        // When & Then
        mockMvc.perform(
            get("/api/v1/sleep-logs/latest")
                .header("UserId", testUserEntity.id)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.sleepDate").value(LocalDate.now().minusDays(1).toString()))
    }

    @Test
    fun `should get sleep stats`() {
        // Given
        TestDataUtils.setupMultipleSleepLogs(userRepository, sleepLogRepository, 30)

        // When & Then
        mockMvc.perform(
            get("/api/v1/sleep-logs/stats")
                .header("UserId", testUserEntity.id)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.startDate").exists())
            .andExpect(jsonPath("$.endDate").exists())
            .andExpect(jsonPath("$.averageTimeInBedMinutes").exists())
            .andExpect(jsonPath("$.averageBedTime").exists())
            .andExpect(jsonPath("$.averageWakeTime").exists())
            .andExpect(jsonPath("$.feelingFrequencies").exists())
            .andExpect(jsonPath("$.feelingFrequencies.GOOD").exists())
            .andExpect(jsonPath("$.feelingFrequencies.OK").exists())
            .andExpect(jsonPath("$.feelingFrequencies.BAD").exists())
    }

    @Test
    fun `should return 404 when user not found`() {
        // Given
        val nonExistentUserId = 999L
        val request = TestDataUtils.createSleepLogRequest()

        // When & Then
        mockMvc.perform(
            post("/api/v1/sleep-logs")
                .header("UserId", nonExistentUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return 404 when no sleep logs exist for latest`() {
        // When & Then
        mockMvc.perform(
            get("/api/v1/sleep-logs/latest")
                .header("UserId", testUserEntity.id)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return 404 when no sleep logs exist for stats`() {
        // When & Then
        mockMvc.perform(
            get("/api/v1/sleep-logs/stats")
                .header("UserId", testUserEntity.id)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should handle time zone transition correctly`() {
        // Given
        val sleepDate = LocalDate.now().minusDays(1)
        val timeToBed = LocalTime.of(23, 0)
        val timeOutOfBed = LocalTime.of(7, 0)
        val request = TestDataUtils.createSleepLogRequest(
            sleepDate = sleepDate,
            timeToBed = timeToBed,
            timeOutOfBed = timeOutOfBed,
            timeZoneId = "Europe/London", // Different from user's default timezone
            feeling = MorningFeeling.GOOD
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/sleep-logs")
                .header("UserId", testUserEntity.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.sleepDate").value(sleepDate.toString()))
            .andExpect(jsonPath("$.timeToBed").value(timeToBed.format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
            .andExpect(jsonPath("$.timeOutOfBed").value(timeOutOfBed.format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
            .andExpect(jsonPath("$.timeZoneId").value("Europe/London"))
    }
}