package com.example.reminderapp.feature.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.reminderapp.core.model.Event
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Main screen displaying the list of events grouped by date.
 *
 * @param viewModel The events ViewModel.
 * @param modifier Optional modifier.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsListScreen(
    viewModel: EventsViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Мои события",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Неизвестная ошибка",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            events.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет событий",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                EventList(
                    events = events,
                    onEventClick = { event -> viewModel.openEditScreen(event) },
                    contentPadding = innerPadding
                )
            }
        }
    }
}

/**
 * Lazy list of events grouped by date with date separator headers.
 */
@Composable
private fun EventList(
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    contentPadding: androidx.compose.foundation.layout.PaddingValues
) {
    val groupedEvents = events.groupBy { it.date }
    val sortedDates = groupedEvents.keys.sorted()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sortedDates.forEach { date ->
            val dateEvents = groupedEvents[date] ?: return@forEach

            // Date separator header
            item(key = "date_header_${date.toEpochDay()}") {
                DateHeader(date = date)
            }

            // Events for this date
            items(
                items = dateEvents,
                key = { it.id }
            ) { event ->
                EventsItem(
                    event = event,
                    onClick = { onEventClick(event) }
                )
            }
        }
    }
}

/**
 * A date header displayed between groups of events.
 * Shows "Сегодня", "Вчера", "Завтра", or the full date.
 */
@Composable
private fun DateHeader(date: LocalDate) {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val tomorrow = today.plusDays(1)

    val headerText = when {
        date == today -> "Сегодня"
        date == yesterday -> "Вчера"
        date == tomorrow -> "Завтра"
        else -> {
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"))
            date.format(formatter)
        }
    }

    Text(
        text = headerText,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp)
    )
}
