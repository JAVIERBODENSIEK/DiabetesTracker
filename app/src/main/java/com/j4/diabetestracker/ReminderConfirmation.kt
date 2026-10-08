package com.j4.diabetestracker

import kotlinx.serialization.Serializable

@Serializable
enum class ConfirmationStatus {
    PENDING,
    YES,
    NO,
    COMMENTED
}

@Serializable
data class ReminderConfirmation(
    val id: String = "",
    val reminderId: String = "",
    val reminderName: String = "",
    val reminderEmoji: String = "",
    val scheduledTime: String = "",
    val date: String = "",
    val timestamp: Long = 0L,
    val status: ConfirmationStatus = ConfirmationStatus.PENDING,
    val comment: String = "",
    val respondedAt: Long = 0L
)
