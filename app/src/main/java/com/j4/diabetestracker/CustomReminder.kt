package com.j4.diabetestracker

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

object FlexibleFloatSerializer : KSerializer<Float> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleFloat", PrimitiveKind.FLOAT)
    override fun serialize(encoder: Encoder, value: Float) = encoder.encodeFloat(value)
    override fun deserialize(decoder: Decoder): Float {
        return if (decoder is JsonDecoder) {
            decoder.decodeJsonElement().jsonPrimitive.float
        } else {
            decoder.decodeFloat()
        }
    }
}

@Serializable
data class ConfirmationAlertSettings(
    val alertOnNoStreak: Boolean = false,
    val noStreakThreshold: Int = 3,
    val alertOnYesStreak: Boolean = false,
    val yesStreakThreshold: Int = 7,
    val alertOnMissed: Boolean = false,
    val missedThreshold: Int = 2,
    val alertWithSound: Boolean = false,
    val alertWithVibration: Boolean = false
)

@Serializable
data class CustomReminder(
    val id: String = "",
    val emoji: String,
    val name: String,
    val notificationType: NotificationType = NotificationType.SOUND_VIBRATION,
    val hasSound: Boolean = true,
    val hasVibration: Boolean = true,
    val daysOfWeek: Set<DayOfWeek> = DayOfWeek.entries.toSet(), // Default: all days
    val times: List<ReminderTime> = emptyList(),
    @Serializable(with = FlexibleFloatSerializer::class)
    val intervalHours: Float = 0f,
    val intervalStartHour: Int = 8,
    val intervalStartMinute: Int = 0,
    val isEnabled: Boolean = true,
    val requiresConfirmation: Boolean = false,
    val confirmationAlertSettings: ConfirmationAlertSettings = ConfirmationAlertSettings(),
    val createdAt: Long = 0L
)

@Serializable
data class ReminderTime(
    val hour: Int,
    val minute: Int
) {
    override fun toString(): String {
        return String.format("%02d:%02d", hour, minute)
    }
}

@Serializable
enum class NotificationType {
    DEFAULT,
    SOUND_VIBRATION,
    SILENT
}

@Serializable
enum class DayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;
    
    fun toShortString(): String {
        return when (this) {
            MONDAY -> "Mon"
            TUESDAY -> "Tue"
            WEDNESDAY -> "Wed"
            THURSDAY -> "Thu"
            FRIDAY -> "Fri"
            SATURDAY -> "Sat"
            SUNDAY -> "Sun"
        }
    }
}
