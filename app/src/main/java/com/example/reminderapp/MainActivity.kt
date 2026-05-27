package com.example.reminderapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reminderapp.feature.events.EventsListScreen
import com.example.reminderapp.feature.events.EventsViewModel
import com.example.reminderapp.ui.theme.ReminderAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as ReminderApp).container
        val repository = appContainer.eventsRepository

        enableEdgeToEdge()
        setContent {
            ReminderAppTheme {
                val viewModel: EventsViewModel = viewModel(
                    factory = EventsViewModel.Factory(repository)
                )

                EventsListScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
