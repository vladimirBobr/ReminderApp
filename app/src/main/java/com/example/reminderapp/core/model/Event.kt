package com.example.reminderapp.core.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Custom serializer for [LocalDate] — formats as "yyyy-MM-dd".
 */
object LocalDateSerializer : kotlinx.serialization.KSerializer<LocalDate> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }
}

/**
 * Custom serializer for [LocalTime] — formats as "HH:mm".
 */
object LocalTimeSerializer : kotlinx.serialization.KSerializer<LocalTime> {
    override val descriptor = PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeString(value.format(DateTimeFormatter.ofPattern("HH:mm")))
    }

    override fun deserialize(decoder: Decoder): LocalTime {
        return LocalTime.parse(decoder.decodeString(), DateTimeFormatter.ofPattern("HH:mm"))
    }
}

/**
 * Serializers module for YAML — registers custom serializers for [LocalDate] and [LocalTime].
 */
val yamlSerializersModule = SerializersModule {
    contextual(LocalDate::class, LocalDateSerializer)
    contextual(LocalTime::class, LocalTimeSerializer)
}

/**
 * Core data model representing a single event/reminder.
 *
 * @property id Unique identifier — UUID as string (for conflict-free sync with GitHub).
 * @property title Short event title.
 * @property description Optional detailed description.
 * @property date Event date.
 * @property time Optional event time (null = весь день).
 */
@Serializable
data class Event(
    val id: String,
    val title: String,
    val description: String = "",
    @Contextual val date: LocalDate,
    @Contextual val time: LocalTime? = null
)

/**
 * Wrapper for YAML serialization — holds a list of [Event] objects.
 */
@Serializable
data class EventList(
    val events: List<Event>
)
