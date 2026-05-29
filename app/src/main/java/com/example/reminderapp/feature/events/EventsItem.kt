package com.example.reminderapp.feature.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.reminderapp.core.model.Event
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * A card displaying a single event.
 *
 * @param event The event to display.
 * @param onClick Called when the card is tapped (for navigation to edit screen).
 * @param modifier Optional modifier.
 */
@Composable
fun EventsItem(
    event: Event,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (event.description.isNotBlank()) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                event.time?.let { time ->
                    Text(
                        text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Text(
                text = event.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EventsItemPreview() {
    MaterialTheme {
        EventsItem(
            event = Event(
                id = "preview-uuid",
                title = "Встреча с заказчиком",
                description = "Обсуждение нового проекта",
                date = LocalDate.now(),
                time = LocalTime.of(14, 0)
            ),
            onClick = { }
        )
    }
}
