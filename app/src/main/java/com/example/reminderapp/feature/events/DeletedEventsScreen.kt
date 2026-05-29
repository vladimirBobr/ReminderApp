package com.example.reminderapp.feature.events

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
 * Screen displaying soft-deleted events (trash / recycle bin).
 * Events are shown as non-clickable cards.
 * Swipe right → restore the event back to the active list.
 *
 * Accessible from [SettingsScreen].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeletedEventsScreen(
    viewModel: EventsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deletedEvents by viewModel.deletedEvents.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Корзина",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (deletedEvents.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Корзина пуста",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        } else {
            // Group by date for nice headers
            val groupedEvents = deletedEvents.groupBy { it.date }
            val sortedDates = groupedEvents.keys.sorted()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                sortedDates.forEach { date ->
                    val dateEvents = groupedEvents[date] ?: return@forEach

                    item(key = "deleted_date_header_${date.toEpochDay()}") {
                        DeletedDateHeader(date = date)
                    }

                    items(
                        items = dateEvents,
                        key = { it.id }
                    ) { event ->
                        SwipeableDeletedEventCard(
                            event = event,
                            onRestore = { viewModel.restoreEvent(event.id) }
                        )
                    }
                }
            }
        }
    }
}

// ==================== Private Components ====================

/**
 * A date header for the deleted events list.
 */
@Composable
private fun DeletedDateHeader(date: LocalDate) {
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
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp)
    )
}

/**
 * A non-clickable event card with swipe-right gesture to restore.
 * Swipe right past threshold → reveals "Восстановить" label in tertiary color.
 * Release past threshold → fires [onRestore].
 */
@Composable
private fun SwipeableDeletedEventCard(
    event: Event,
    onRestore: () -> Unit
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { SWIPE_THRESHOLD_DP.toPx() }

    var dragOffset by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = dragOffset,
        label = "swipe_offset"
    )

    // Only right-swipe (positive offset) is meaningful — restore
    val bgColor = when {
        animatedOffset > thresholdPx -> MaterialTheme.colorScheme.tertiaryContainer
        else -> Color.Transparent
    }
    val bgLabel = when {
        animatedOffset > thresholdPx -> "Восстановить"
        else -> ""
    }
    val bgLabelColor = when {
        animatedOffset > thresholdPx -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> Color.Transparent
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        // Background label (left-aligned for right swipe)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
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
                                onRestore()
                            }
                            dragOffset = 0f
                        },
                        onDragCancel = {
                            dragOffset = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            // Only allow right swipe (positive), clamp left
                            dragOffset = (dragOffset + dragAmount).coerceAtLeast(0f)
                        }
                    )
                }
        ) {
            // Non-clickable event card (onClick = null)
            EventsItem(
                event = event,
                onClick = null
            )
        }
    }
}
