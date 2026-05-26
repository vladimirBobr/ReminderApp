package com.example.reminderapp.data.model

import java.time.LocalDate
import java.time.LocalTime

data class Event(
    val id: Long,
    val title: String,
    val description: String = "",
    val date: LocalDate,
    val time: LocalTime? = null
)
