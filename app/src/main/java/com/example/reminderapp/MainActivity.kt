package com.example.reminderapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reminderapp.feature.events.EventsEditScreen
import com.example.reminderapp.feature.events.EventsListScreen
import com.example.reminderapp.feature.events.EventsViewModel
import com.example.reminderapp.feature.events.RawDataScreen
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

                val isEditScreenVisible by viewModel.isEditScreenVisible.collectAsState()
                val editingEvent by viewModel.editingEvent.collectAsState()
                val isRawDataVisible by viewModel.isRawDataVisible.collectAsState()

                when {
                    isEditScreenVisible -> {
                        EventsEditScreen(
                            event = editingEvent,
                            onSave = { title, description, date, time ->
                                viewModel.saveEvent(title, description, date, time)
                            },
                            onCancel = { viewModel.closeEditScreen() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    isRawDataVisible -> {
                        RawDataScreen(
                            repository = repository,
                            onBack = { viewModel.closeRawData() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        EventsListScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
