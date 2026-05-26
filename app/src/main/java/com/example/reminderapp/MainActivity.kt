package com.example.reminderapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.reminderapp.data.reader.EventsReader
import com.example.reminderapp.ui.screen.EventListScreen
import com.example.reminderapp.ui.theme.ReminderAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReminderAppTheme {
                EventListScreen(
                    events = EventsReader.getEvents(),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}