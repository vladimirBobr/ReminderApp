package com.example.reminderapp.feature.events

import com.example.reminderapp.core.model.Event
import com.example.reminderapp.core.model.EventList
import com.charleskorn.kaml.Yaml
import com.example.reminderapp.core.model.yamlSerializersModule
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

/**
 * Repository for events CRUD operations.
 * Stores data in two YAML files:
 * - [activeFile]: `events.yml` — active (non-deleted) events
 * - [deletedFile]: `deleted_events.yml` — soft-deleted events
 *
 * On first launch, creates [activeFile] with demo data if it doesn't exist.
 */
class EventsRepository(private val storageDir: File) {

    private val activeFile = File(storageDir, "events.yml")
    private val deletedFile = File(storageDir, "deleted_events.yml")

    private val yaml = Yaml(serializersModule = yamlSerializersModule)

    // ==================== READ ====================

    /**
     * Returns all active events sorted by date (closest first), then by time.
     */
    fun getAll(): List<Event> {
        ensureFilesExist()
        return readEvents(activeFile)
    }

    /**
     * Returns a single event by its UUID, or null if not found.
     */
    fun getById(id: String): Event? =
        getAll().find { it.id == id }

    /**
     * Returns all soft-deleted events.
     */
    fun getDeleted(): List<Event> {
        if (!deletedFile.exists()) return emptyList()
        return readEvents(deletedFile)
    }

    /**
     * Returns the raw YAML content of the active events file (for debugging).
     */
    fun getRawActiveYaml(): String {
        ensureFilesExist()
        return activeFile.readText()
    }

    /**
     * Returns the raw YAML content of the deleted events file (for debugging).
     */
    fun getRawDeletedYaml(): String {
        if (!deletedFile.exists()) return "deleted_events.yml не найден"
        return deletedFile.readText()
    }

    // ==================== CREATE / UPDATE ====================

    /**
     * Adds a new event to the active list.
     */
    fun add(event: Event) {
        val events = getAll() + event
        writeEvents(activeFile, events)
    }

    /**
     * Updates an existing event in the active list (matched by id).
     */
    fun update(event: Event) {
        val events = getAll().map { if (it.id == event.id) event else it }
        writeEvents(activeFile, events)
    }

    // ==================== MOVE ====================

    /**
     * Moves an event to tomorrow (date +1 day).
     */
    fun moveToTomorrow(id: String) {
        val events = getAll()
        val updated = events.map { event ->
            if (event.id == id) event.copy(date = event.date.plusDays(1)) else event
        }
        writeEvents(activeFile, updated)
    }

    // ==================== SOFT DELETE ====================

    /**
     * Soft-deletes an event: removes from [activeFile] and appends to [deletedFile].
     */
    fun delete(id: String) {
        val active = getAll()
        val event = active.find { it.id == id } ?: return
        writeEvents(activeFile, active.filter { it.id != id })
        val deleted = getDeleted() + event
        writeEvents(deletedFile, deleted)
    }

    // ==================== RESTORE ====================

    /**
     * Restores a soft-deleted event: removes from [deletedFile] and appends to [activeFile].
     */
    fun restore(id: String) {
        val deleted = getDeleted()
        val event = deleted.find { it.id == id } ?: return
        writeEvents(deletedFile, deleted.filter { it.id != id })
        val active = getAll() + event
        writeEvents(activeFile, active)
    }

    // ==================== INTERNALS ====================

    /**
     * Reads and deserializes events from a YAML file.
     */
    private fun readEvents(file: File): List<Event> {
        val content = file.readText()
        return yaml.decodeFromString(EventList.serializer(), content).events
    }

    /**
     * Serializes and writes events to a YAML file, sorted by date then time.
     */
    private fun writeEvents(file: File, events: List<Event>) {
        val sorted = events.sortedWith(
            compareBy<Event> { it.date }.thenBy { it.time ?: LocalTime.MAX }
        )
        file.writeText(yaml.encodeToString(EventList.serializer(), EventList(sorted)))
    }

    /**
     * Creates the storage directory and demo data files if they don't exist.
     */
    private fun ensureFilesExist() {
        if (activeFile.exists()) return
        storageDir.mkdirs()
        writeEvents(activeFile, generateDemoEvents())
        writeEvents(deletedFile, emptyList())
    }

    /**
     * Generates a set of demo events for the first launch.
     */
    private fun generateDemoEvents(): List<Event> {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)
        val nextWeek = today.plusDays(7)
        val nextMonth = today.plusMonths(1)

        return listOf(
            Event(
                id = java.util.UUID.randomUUID().toString(),
                title = "Встреча с заказчиком 2",
                description = "Обсуждение нового проекта и сроков 2",
                date = yesterday,
                time = LocalTime.of(14, 0)
            ),
            Event(
                id = java.util.UUID.randomUUID().toString(),
                title = "Завтрак с командой 2",
                description = "Кафе на первом этаже 2",
                date = today,
                time = LocalTime.of(9, 30)
            ),
            Event(
                id = java.util.UUID.randomUUID().toString(),
                title = "Звонок с партнёрами",
                description = "Zoom",
                date = today,
                time = LocalTime.of(15, 0)
            ),
            Event(
                id = java.util.UUID.randomUUID().toString(),
                title = "День рождения Алексея",
                description = "Не забыть подарок",
                date = tomorrow,
                time = null
            ),
            Event(
                id = java.util.UUID.randomUUID().toString(),
                title = "Планёрка",
                description = "Еженедельное собрание отдела",
                date = tomorrow,
                time = LocalTime.of(10, 0)
            ),
            Event(
                id = java.util.UUID.randomUUID().toString(),
                title = "Сдача отчёта",
                description = "Квартальный отчёт в бухгалтерию",
                date = nextWeek,
                time = LocalTime.of(18, 0)
            ),
            Event(
                id = java.util.UUID.randomUUID().toString(),
                title = "Конференция Mobile Dev",
                description = "Онлайн-участие, ссылка придёт на почту",
                date = nextWeek.plusDays(2),
                time = LocalTime.of(11, 0)
            ),
            Event(
                id = java.util.UUID.randomUUID().toString(),
                title = "Отпуск",
                description = "Две недели на море 🏖",
                date = nextMonth,
                time = null
            )
        )
    }
}
