package com.noom.interview.fullstack.sleep

import com.fasterxml.jackson.databind.ObjectMapper
import com.noom.interview.fullstack.sleep.db.entity.MorningFeeling
import com.noom.interview.fullstack.sleep.db.entity.User
import com.noom.interview.fullstack.sleep.db.repositorty.UserRepository
import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.time.ZoneOffset

@SpringBootTest
@AutoConfigureMockMvc
class SleepLogApiIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var testUser: User
    private val userId: Long = 999

    @BeforeEach
    fun setup() {
        // Create test user if it doesn't exist
        if (!userRepository.existsById(userId)) {
            testUser = User(
                id = userId,
                username = "test_user",
                timeZone = "UTC"
            )
            userRepository.save(testUser)
        } else {
            testUser = userRepository.findById(userId).get()
        }
    }

    @Test
    fun `should create sleep log`() {
        // Given
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val createRequest = CreateSleepLogRequest(
            timeToBed = now.minusHours(8),
            timeOutOfBed = now,
            feeling = MorningFeeling.GOOD
        )

        // When/Then
        mockMvc.perform(
            post("/api/v1/users/$userId/sleep-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.feeling").value("GOOD"))
    }

    @Test
    fun `should get latest sleep log`() {
        // Given - First create a sleep log
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val createRequest = CreateSleepLogRequest(
            timeToBed = now.minusHours(8),
            timeOutOfBed = now,
            feeling = MorningFeeling.GOOD
        )

        mockMvc.perform(
            post("/api/v1/users/$userId/sleep-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        ).andExpect(status().isCreated)

        // When/Then - Get latest sleep log
        mockMvc.perform(get("/api/v1/users/$userId/sleep-logs/latest"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.feeling").value("GOOD"))
    }

    @Test
    fun `should get sleep stats`() {
        // Given - First create some sleep logs
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        // Create a sleep log for today
        val createRequest1 = CreateSleepLogRequest(
            timeToBed = now.minusHours(8),
            timeOutOfBed = now,
            feeling = MorningFeeling.GOOD
        )

        mockMvc.perform(
            post("/api/v1/users/$userId/sleep-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest1))
        ).andExpect(status().isCreated)

        // Create a sleep log for yesterday
        val createRequest2 = CreateSleepLogRequest(
            timeToBed = now.minusDays(1).minusHours(8),
            timeOutOfBed = now.minusDays(1),
            feeling = MorningFeeling.OK
        )

        mockMvc.perform(
            post("/api/v1/users/$userId/sleep-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest2))
        ).andExpect(status().isCreated)

        // When/Then - Get sleep stats
        mockMvc.perform(get("/api/v1/users/$userId/sleep-logs/stats"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.startDate").exists())
            .andExpect(jsonPath("$.endDate").exists())
            .andExpect(jsonPath("$.averageTimeInBed").exists())
            .andExpect(jsonPath("$.averageBedTime").exists())
            .andExpect(jsonPath("$.averageWakeTime").exists())
            .andExpect(jsonPath("$.feelingFrequencies").exists())
    }
}