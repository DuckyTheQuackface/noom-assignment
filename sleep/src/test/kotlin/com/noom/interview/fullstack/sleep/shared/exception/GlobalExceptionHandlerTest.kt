package com.noom.interview.fullstack.sleep.shared.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import javax.servlet.http.HttpServletRequest

class GlobalExceptionHandlerTest {

    private val exceptionHandler = GlobalExceptionHandler()
    private val mockRequest = mock(HttpServletRequest::class.java)
    private val testPath = "/api/test"

    init {
        `when`(mockRequest.requestURI).thenReturn(testPath)
    }

    @Test
    fun `handleResourceNotFoundException should return NOT_FOUND status with correct path`() {
        // Given
        val errorMessage = "Resource not found"
        val exception = ResourceNotFoundException(errorMessage)

        // When
        val response = exceptionHandler.handleResourceNotFoundException(exception, mockRequest)
        val errorResponse = response.body

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNotNull(errorResponse)
        assertEquals(errorMessage, errorResponse?.message)
        assertEquals(testPath, errorResponse?.path)
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse?.status)
        assertEquals(HttpStatus.NOT_FOUND.name, errorResponse?.error)
        assertNotNull(errorResponse?.timestamp)
    }

    @Test
    fun `handleIllegalArgumentException should return BAD_REQUEST status with correct path`() {
        // Given
        val errorMessage = "Invalid input"
        val exception = IllegalArgumentException(errorMessage)

        // When
        val response = exceptionHandler.handleIllegalArgumentException(exception, mockRequest)
        val errorResponse = response.body

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(errorResponse)
        assertEquals(errorMessage, errorResponse?.message)
        assertEquals(testPath, errorResponse?.path)
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse?.status)
        assertEquals(HttpStatus.BAD_REQUEST.name, errorResponse?.error)
        assertNotNull(errorResponse?.timestamp)
    }

    @Test
    fun `handleDataIntegrityViolationException should return CONFLICT status with correct path`() {
        // Given
        val errorMessage = "Database constraint violation"
        val exception = DataIntegrityViolationException(errorMessage)

        // When
        val response = exceptionHandler.handleDataIntegrityViolationException(exception, mockRequest)
        val errorResponse = response.body

        // Then
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertNotNull(errorResponse)
        assertEquals(errorMessage, errorResponse?.message)
        assertEquals(testPath, errorResponse?.path)
        assertEquals(HttpStatus.CONFLICT.value(), errorResponse?.status)
        assertEquals(HttpStatus.CONFLICT.name, errorResponse?.error)
        assertNotNull(errorResponse?.timestamp)
    }

    @Test
    fun `handleGenericException should return INTERNAL_SERVER_ERROR status with correct path`() {
        // Given
        val errorMessage = "Unexpected error"
        val exception = Exception(errorMessage)

        // When
        val response = exceptionHandler.handleGenericException(exception, mockRequest)
        val errorResponse = response.body

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNotNull(errorResponse)
        assertEquals(errorMessage, errorResponse?.message)
        assertEquals(testPath, errorResponse?.path)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse?.status)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.name, errorResponse?.error)
        assertNotNull(errorResponse?.timestamp)
    }

    @Test
    fun `handler should use default message when exception message is null`() {
        // Given
        val exception = IllegalArgumentException()
        val defaultMessage = "Invalid input"

        // When
        val response = exceptionHandler.handleIllegalArgumentException(exception, mockRequest)
        val errorResponse = response.body

        // Then
        assertEquals(defaultMessage, errorResponse?.message)
    }
}