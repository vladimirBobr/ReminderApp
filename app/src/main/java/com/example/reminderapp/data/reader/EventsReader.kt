package com.example.reminderapp.data.reader

import com.example.reminderapp.data.model.Event
import java.time.LocalDate
import java.time.LocalTime

object EventsReader {

    fun getEvents(): List<Event> {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)
        val nextWeek = today.plusDays(7)
        val nextMonth = today.plusMonths(1)

        return listOf(
            Event(
                id = 1,
                title = "Встреча с заказчиком",
                description = "Обсуждение нового проекта и сроков",
                date = yesterday,
                time = LocalTime.of(14, 0)
            ),
            Event(
                id = 2,
                title = "Завтрак с командой",
                description = "Кафе на первом этаже",
                date = today,
                time = LocalTime.of(9, 30)
            ),
            Event(
                id = 3,
                title = "Звонок с партнёрами",
                description = "Zoom",
                date = today,
                time = LocalTime.of(15, 0)
            ),
            Event(
                id = 4,
                title = "День рождения Алексея",
                description = "Не забыть подарок",
                date = tomorrow,
                time = null
            ),
            Event(
                id = 5,
                title = "Планёрка",
                description = "Еженедельное собрание отдела",
                date = tomorrow,
                time = LocalTime.of(10, 0)
            ),
            Event(
                id = 6,
                title = "Сдача отчёта",
                description = "Квартальный отчёт в бухгалтерию",
                date = nextWeek,
                time = LocalTime.of(18, 0)
            ),
            Event(
                id = 7,
                title = "Конференция Mobile Dev",
                description = "Онлайн-участие, ссылка придёт на почту",
                date = nextWeek.plusDays(2),
                time = LocalTime.of(11, 0)
            ),
            Event(
                id = 8,
                title = "Отпуск",
                description = "Две недели на море 🏖",
                date = nextMonth,
                time = null
            )
        ).sortedWith(compareBy<Event> { it.date }.thenBy { it.time ?: LocalTime.MAX })
    }
}
