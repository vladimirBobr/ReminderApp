package com.example.reminderapp.feature.events

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.reminderapp.core.model.Event
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val SWIPE_THRESHOLD_DP = 80.dp

/**
 * Main screen displaying the list of events grouped by date.
 * Supports swipe-left (delete with confirmation) and swipe-right (move to tomorrow).
 * Two-stage swipe: first shows label, further fires action.
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
                actions = {
                    IconButton(onClick = { viewModel.openRawData() }) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Raw Data"
                        )
                    }
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
                    onSwipeMoveToTomorrow = { event ->
                        viewModel.moveEventToTomorrow(event.id)
                    },
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
 * Lazy list of events grouped by date.
 */
@Composable
private fun EventList(
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    onSwipeDelete: (Event) -> Unit,
    onSwipeMoveToTomorrow: (Event) -> Unit,
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

            item(key = "date_header_${date.toEpochDay()}") {
                DateHeader(date = date)
            }

            items(
                items = dateEvents,
                key = { it.id }
            ) { event ->
                SwipeableEventCard(
                    event = event,
                    onClick = { onEventClick(event) },
                    onDelete = { onSwipeDelete(event) },
                    onMoveToTomorrow = { onSwipeMoveToTomorrow(event) }
                )
            }
        }
    }
}

/**
 * A card with swipe gestures:
 * - Swipe past 80dp label appears ("На завтра" / "Удалить")
 * - Action fires **only on release** past the threshold
 * - Swipe back below threshold → release → nothing happens
 */
@Composable
private fun SwipeableEventCard(
    event: Event,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMoveToTomorrow: () -> Unit
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { SWIPE_THRESHOLD_DP.toPx() }

    // Track offset for visual feedback
    var dragOffset by remember { mutableFloatStateOf(0f) }

    // Animate snap-back to 0
    val animatedOffset by animateFloatAsState(
        targetValue = dragOffset,
        label = "swipe_offset"
    )

    // Determine background based on drag direction (single threshold)
    val bgColor = when {
        animatedOffset > thresholdPx -> MaterialTheme.colorScheme.tertiaryContainer
        animatedOffset < -thresholdPx -> MaterialTheme.colorScheme.errorContainer
        else -> Color.Transparent
    }
    val bgLabel = when {
        animatedOffset > thresholdPx -> "На завтра"
        animatedOffset < -thresholdPx -> "Удалить"
        else -> ""
    }
    val bgAlignment = when {
        animatedOffset > thresholdPx -> Alignment.CenterStart
        animatedOffset < -thresholdPx -> Alignment.CenterEnd
        else -> Alignment.CenterEnd
    }
    val bgLabelColor = when {
        animatedOffset > thresholdPx -> MaterialTheme.colorScheme.onTertiaryContainer
        animatedOffset < -thresholdPx -> MaterialTheme.colorScheme.onErrorContainer
        else -> Color.Transparent
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        // Background label
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(horizontal = 20.dp),
            contentAlignment = bgAlignment
        ) {
            Text(
                text = bgLabel,
                color = bgLabelColor,
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Foreground card with offset + gesture detection
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (dragOffset > thresholdPx) {
                                onMoveToTomorrow()
                            } else if (dragOffset < -thresholdPx) {
                                onDelete()
                            }
                            dragOffset = 0f
                        },
                        onDragCancel = {
                            dragOffset = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            dragOffset += dragAmount
                            // Visual only — no action until release
                        }
                    )
                }
        ) {
            EventsItem(
                event = event,
                onClick = onClick
            )
        }
    }
}

/**
 * A date header displayed between groups of events.
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
