package com.example.reminderapp.feature.events

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.reminderapp.core.model.Event
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Main screen displaying the list of events grouped by date.
 * Supports swipe-to-delete and FAB for adding new events.
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

    // Track which event is pending deletion confirmation
    var pendingDeleteEvent by remember { mutableStateOf<Event?>(null) }

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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openEditScreen(null) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить событие"
                )
            }
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
                    onSwipeDelete = { event -> pendingDeleteEvent = event },
                    contentPadding = innerPadding
                )
            }
        }
    }

    // Delete confirmation dialog
    pendingDeleteEvent?.let { event ->
        ConfirmDeleteDialog(
            eventTitle = event.title,
            onConfirm = {
                viewModel.deleteEvent(event.id)
                pendingDeleteEvent = null
            },
            onDismiss = {
                pendingDeleteEvent = null
            }
        )
    }
}

/**
 * Lazy list of events grouped by date with swipe-to-delete support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventList(
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    onSwipeDelete: (Event) -> Unit,
    contentPadding: PaddingValues
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
                SwipeToDismissEventItem(
                    event = event,
                    onClick = { onEventClick(event) },
                    onDelete = { onSwipeDelete(event) }
                )
            }
        }
    }
}

/**
 * Wraps EventsItem in a SwipeToDismissBox for swipe-to-delete.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissEventItem(
    event: Event,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false // Don't actually dismiss - let the dialog handle it
            } else {
                false
            }
        }
    )

    // Animate background color on swipe
    val backgroundColor by animateColorAsState(
        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            Color.Transparent
        },
        label = "swipe_bg"
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Удалить",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        EventsItem(
            event = event,
            onClick = onClick
        )
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
