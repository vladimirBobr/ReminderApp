# Kilo Code — Правила работы над ReminderApp

Назначение этого файла: чтобы Kilo Code в каждой новой сессии знал контекст проекта, архитектурные соглашения и процесс взаимодействия с разработчиком.

---

## 1. Язык и стиль общения

- **Язык:** русский (думать, писать, комментировать)
- **Стиль:** технический, прямой, без «воды» и conversational-фраз («Great!», «Sure!», «Certainly!»)
- **Ответы:** короткие, по делу, без встречных вопросов в конце
- **Обращение к файлам:** с кликабельными ссылками вида [`filename.kt`](path/to/file.kt:line)

---

## 2. Контекст проекта

**ReminderApp** — персональный календарь-напоминалка на Android.

- Данные хранятся в YAML-файлах на устройстве (`events.yml` + `deleted_events.yml`)
- В будущем — синхронизация с GitHub через коммиты
- Минимальный SDK: 24 (Android 7.0)

### Технический стек

| Компонент | Технология |
|-----------|------------|
| Язык | Kotlin |
| UI | Jetpack Compose + Material3 |
| Сериализация | `kotlinx-serialization-yaml` |
| Фоновые задачи | WorkManager (CoroutineWorker) |
| DI | Ручной (`AppContainer` в `ReminderApp.kt`) |
| Навигация | Флаги состояния во ViewModel (без Navigation Component) |
| Сборка | Gradle KTS + Version Catalog (`libs.versions.toml`) |

---

## 3. Архитектурные соглашения

### Vertical Slice Architecture

```
com.example.reminderapp/
├── ReminderApp.kt              # Application + инициализация каналов
├── MainActivity.kt             # Навигационный хаб (when по флагам)
├── core/
│   ├── model/Event.kt          # @Serializable data class + кастомные сериализаторы
│   ├── di/AppContainer.kt      # Ручной DI-контейнер
│   └── notification/           # WorkManager: Worker + Scheduler
└── feature/
    ├── events/                 # ВСЁ для фичи Events в одной папке
    │   ├── EventsRepository.kt # CRUD + YAML I/O (конкретный класс, без интерфейса)
    │   ├── EventsViewModel.kt  # StateFlow + навигационные флаги
    │   ├── EventsListScreen.kt # Главный экран
    │   ├── EventsEditScreen.kt # Создание/редактирование
    │   ├── EventsItem.kt       # Карточка события
    │   ├── DeletedEventsScreen.kt
    │   ├── ConfirmDeleteDialog.kt
    │   └── RawDataScreen.kt
    └── settings/
        └── SettingsScreen.kt
```

### Ключевые принципы

1. **Фича = одна папка.** Screen, ViewModel, Repository, Components лежат рядом в `feature/<имя>/`
2. **`core/` — только переиспользуемое.** Модели, DI, уведомления. Никакой бизнес-логики.
3. **`ui/theme/` — только тема.** Color, Theme, Type.
4. **Repository — конкретный класс, без интерфейса.** Нет нескольких реализаций → не нужен интерфейс.
5. **MVVM без Navigation Component.** Навигация через флаги `MutableStateFlow<Boolean>` во ViewModel. `MainActivity` делает `when` по флагам.
6. **StateFlow для реактивности.** ViewModel выставляет `StateFlow`, UI собирает через `collectAsState()`.

### Почему не Navigation Component?

- Приложение маленькое, экранов мало
- Флаги проще и прозрачнее
- Меньше зависимостей
- Легко добавить back-stack (несколько флагов true одновременно, порядок в `when` задаёт приоритет)

---

## 4. Правила организации файлов

| Тип | Куда класть |
|-----|------------|
| Планы (`*.md`) | `plans/` |
| Новая фича | `feature/<имя>/` |
| Общая модель | `core/model/` |
| Общий сервис (уведомления, сеть, синхронизация) | `core/<имя>/` |
| DI-контейнер | `core/di/AppContainer.kt` |
| Компоненты темы | `ui/theme/` |

---

## 5. Процесс работы

### Главное правило: сначала план → потом код

```
Пользователь ставит задачу
        │
        ▼
┌───────────────────┐
│ 1. ARCHITECT MODE │  ← Изучить контекст, составить план
│    (если сложно)  │    План = todo-список или plans/*.md
└───────┬───────────┘
        │ Пользователь одобряет план
        ▼
┌───────────────────┐
│ 2. CODE MODE      │  ← Реализация по шагам плана
│                   │    Только после разрешения!
└───────┬───────────┘
        │ По запросу
        ▼
┌───────────────────┐
│ 3. COMMIT / DEPLOY│  ← Только по явному запросу
└───────────────────┘
```

### Когда что использовать

| Режим | Когда |
|-------|-------|
| **Architect** | Новая фича, рефакторинг, сложная задача — нужен план |
| **Code** | Реализация по готовому плану, простые правки |
| **Debug** | Ошибки, баги, странное поведение |
| **Review** | Проверка изменений перед коммитом |
| **Ask** | Вопросы по коду, объяснения |

---

## 6. Кодовые конвенции

### Именование

| Элемент | Шаблон | Пример |
|---------|--------|--------|
| Экран (Composable) | `FeatureNameScreen` | `EventsListScreen`, `SettingsScreen` |
| ViewModel | `FeatureNameViewModel` | `EventsViewModel` |
| Repository | `FeatureNameRepository` | `EventsRepository` |
| Компонент | `FeatureNameComponent` | `EventsItem`, `ConfirmDeleteDialog` |
| Сервис в core | `ServiceName` | `NotificationScheduler`, `NotificationWorker` |

### Документирование

- KDoc (`/** ... */`) для публичных классов, методов, свойств
- Комментарии на русском
- Сложная логика — поясняющий комментарий

### Формат данных

- `Event.id` — UUID как строка (для будущей синхронизации без конфликтов)
- `Event.date` — `LocalDate` (сериализуется как `yyyy-MM-dd`)
- `Event.time` — `LocalTime?` (сериализуется как `HH:mm`, null = весь день)
- YAML-файлы: корневая структура `EventList(events: List<Event>)`
- Сортировка событий: по дате, затем по времени (null-время в конце)

### Добавление зависимостей

- Версии в `gradle/libs.versions.toml`
- Библиотеки в `gradle/libs.versions.toml` → блок `[libraries]`
- Подключение в `app/build.gradle.kts` через `implementation(libs....)`

---

## 7. Специальные команды

Эти действия выполняются **только по явному запросу** пользователя:

### Деплой на телефон

По фразам: `залей на телефон`, `залей`, `опубликуй`

```cmd
"C:\Users\Elena\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r d:\1git\ReminderApp\app\build\outputs\apk\debug\app-debug.apk 2>&1
```

### Коммит

По фразам: `коммитни`, `сделай коммит`

- Перед коммитом — Review mode для проверки изменений
- Коммит-месседж на русском языке
- Формат: глагол в прошедшем времени (например: «Добавил систему уведомлений», «Исправил сортировку событий»)

---

## 8. Часовой пояс

- **Часовой пояс пользователя:** `Europe/Samara` (UTC+4)
- Используется при планировании уведомлений (конвертация `LocalDateTime` → `Instant`)

---

## 9. Файлы, которые не трогаем без необходимости

- `gradle/wrapper/` — обновляется через `./gradlew wrapper`
- `app/proguard-rules.pro` — только если добавляем библиотеки с рефлексией
- `ui/theme/` — тема стабильна

---

## 10. История изменений

| Дата | Изменение |
|------|-----------|
| 2026-06-07 | Создан файл с базовыми правилами |
