package com.example.reminderapp.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reminderapp.core.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * ViewModel for the Events feature.
 * Manages the list of events and coordinates CRUD operations via [EventsRepository].
 */
class EventsViewModel(
    private val repository: EventsRepository
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    /** Loading state — true while data is being loaded. */
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Error message, if any. */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** Currently editing event (null = creating new, non-null = editing). */
    private val _editingEvent = MutableStateFlow<Event?>(null)
    val editingEvent: StateFlow<Event?> = _editingEvent.asStateFlow()

    /** True when the edit screen is visible. */
    private val _isEditScreenVisible = MutableStateFlow(false)
    val isEditScreenVisible: StateFlow<Boolean> = _isEditScreenVisible.asStateFlow()

    /** True when the raw data screen is visible. */
    private val _isRawDataVisible = MutableStateFlow(false)
    val isRawDataVisible: StateFlow<Boolean> = _isRawDataVisible.asStateFlow()

    /** True when the settings screen is visible. */
    private val _isSettingsVisible = MutableStateFlow(false)
    val isSettingsVisible: StateFlow<Boolean> = _isSettingsVisible.asStateFlow()

    init {
        loadEvents()
    }

    /**
     * Loads all active events from the repository.
     */
    fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _events.value = repository.getAll()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== Navigation (state-based) ====================

    /**
     * Opens the edit screen for a new event (null) or existing event.
     */
    fun openEditScreen(event: Event? = null) {
        _editingEvent.value = event
        _isEditScreenVisible.value = true
    }

    /**
     * Closes the edit screen and discards any unsaved changes.
     */
    fun closeEditScreen() {
        _editingEvent.value = null
        _isEditScreenVisible.value = false
    }

    // ==================== Raw Data ====================

    /**
     * Opens the raw data (debug) screen.
     */
    fun openRawData() {
        _isRawDataVisible.value = true
    }

    /**
     * Closes the raw data screen.
     */
    fun closeRawData() {
        _isRawDataVisible.value = false
    }

    // ==================== Settings ====================

    /**
     * Opens the settings screen.
     */
    fun openSettings() {
        _isSettingsVisible.value = true
    }

    /**
     * Closes the settings screen.
     */
    fun closeSettings() {
        _isSettingsVisible.value = false
    }

    // ==================== MOVE ====================

    /**
     * Moves an event to tomorrow (date +1 day).
     */
    fun moveEventToTomorrow(id: String) {
        viewModelScope.launch {
            try {
                repository.moveToTomorrow(id)
                loadEvents()
            } catch (e: Exception) {
                _error.value = "Ошибка переноса: ${e.localizedMessage}"
            }
        }
    }

    // ==================== CRUD ====================

    /**
     * Saves an event (creates new or updates existing).
     */
    fun saveEvent(title: String, description: String, date: LocalDate, time: LocalTime?) {
        viewModelScope.launch {
            try {
                val existing = _editingEvent.value
                if (existing != null) {
                    // Update existing event
                    repository.update(existing.copy(
                        title = title,
                        description = description,
                        date = date,
                        time = time
                    ))
                } else {
                    // Create new event
                    repository.add(Event(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        description = description,
                        date = date,
                        time = time
                    ))
                }
                loadEvents()
                closeEditScreen()
            } catch (e: Exception) {
                _error.value = "Ошибка сохранения: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Soft-deletes an event by its id.
     */
    fun deleteEvent(id: String) {
        viewModelScope.launch {
            try {
                repository.delete(id)
                loadEvents()
            } catch (e: Exception) {
                _error.value = "Ошибка удаления: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Clears the current error message.
     */
    fun clearError() {
        _error.value = null
    }

    // ==================== Factory ====================

    class Factory(private val repository: EventsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EventsViewModel::class.java)) {
                return EventsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
