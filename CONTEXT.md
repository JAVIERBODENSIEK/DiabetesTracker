# DiabetesTracker - Project Context

This document provides a concise overview for AI models to understand the project structure, tech stack, and current state.

---

## 1. Tech Stack & Key Dependencies

### Core Technologies
- **Language**: Kotlin 1.9.22
- **Build System**: Gradle 8.8.2
- **UI Framework**: Jetpack Compose with Material3
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)

### Key Dependencies
```kotlin
// UI
androidx.compose:compose-bom:2024.02.00
androidx.compose.material3:material3:1.1.2
androidx.compose.material3:material3-window-size-class
androidx.compose.material:material-icons-extended

// Charts
com.patrykandpatrick.vico:compose:1.13.1
com.patrykandpatrick.vico:compose-m3:1.13.1

// Serialization
org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0

// Core Library Desugaring (for java.time APIs on older Android versions)
com.android.tools:desugar_jdk_libs:2.0.4

// Lifecycle
androidx.lifecycle:lifecycle-runtime-ktx:2.7.0
androidx.activity:activity-compose:1.8.2
```

### Android Permissions
- `POST_NOTIFICATIONS` - For reminder notifications
- `SCHEDULE_EXACT_ALARM` - For precise reminder scheduling
- `USE_EXACT_ALARM` - For exact alarm usage

---

## 2. Project Architecture & Folder Structure

### Root Directory
```
DiabetesTracker/
├── app/
│   ├── src/main/
│   │   ├── java/com/j4/diabetestracker/     # Main source code
│   │   ├── res/                               # Resources (strings, drawables, etc.)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── CHANGELOG.md                               # Development history
├── CHAT_HISTORY.md                            # Conversation history
└── CONTEXT.md                                 # This file
```

### Main Source Code Structure (`app/src/main/java/com/j4/diabetestracker/`)

#### Core Files
- **`MainActivity.kt`** (~1.1MB) - Main UI, state management, data persistence, chart rendering
- **`AnalysisScreen.kt`** (~142KB) - Pattern analysis UI, marker calendar, potential foods section
- **`PatternDetection.kt`** (~23KB) - Pattern detection algorithms (food-marker correlations, blood sugar trends)

#### Pattern & Analysis System
- **`PatternStorage.kt`** - Pattern persistence (SharedPreferences)
- **`PatternAnalysis.kt`** - Pattern insights generation
- **`PatternDetailsPopup.kt`** - Pattern details UI
- **`PatternInsightsDialog.kt`** - Insights dialog UI
- **`PatternExport.kt`** / **`PatternExportPDF.kt`** - Pattern export functionality

#### Food Warning System
- **`FoodWarningHelper.kt`** - Food risk assessment and contradiction detection
- **`PotentialFoodSignals.kt`** - Potential trigger foods UI (list + calendar views)
- **`IgnoredFoodManager.kt`** - Ignored/whitelisted foods persistence

#### Reminder System
- **`CustomReminder.kt`** - Reminder data models
- **`CustomReminderDialog.kt`** (~90KB) - Reminder configuration UI
- **`CustomReminderStorage.kt`** - Reminder persistence
- **`ReminderScheduler.kt`** - Alarm scheduling logic
- **`ReminderReceiver.kt`** - BroadcastReceiver for notifications
- **`NotificationHelper.kt`** - Notification management
- **`ReminderConfirmation.kt`** / **`ReminderConfirmationStorage.kt`** / **`ReminderConfirmationUI.kt`** - Confirmation system
- **`ReminderHistory.kt`** / **`ReminderHistoryDialog.kt`** - Reminder history tracking

#### Settings & Configuration
- **`LanguageSettingsDialog.kt`** - Language selection (English, German, Spanish)
- **`AppearanceSettingsDialog.kt`** - Color schemes, font sizes
- **`NotificationSettingsDialog.kt`** (~58KB) - Notification preferences
- **`ConfirmationAlertSettingsDialog.kt`** (~24KB) - Alert configuration
- **`DataManagementDialog.kt`** - Data export/import
- **`BackupRestoreDialog.kt`** - Backup/restore functionality

#### Testing & Utilities
- **`TestDataGenerator.kt`** (~24KB) - Test data generation for pattern validation
- **`DataManager.kt`** (in MainActivity.kt) - Entry persistence with caching
- **`PresetManager.kt`** (in MainActivity.kt) - Food/color preset management

---

## 3. Implemented Core Features & Logic

### Diary Entry Management
- **Per-cell food entries** with custom columns (morning, afternoon, evening, night)
- **Blood sugar tracking** for each time period
- **Custom markers** (feel bad, headaches, etc.) with time-of-day specificity
- **Undo/Redo system** with action descriptions
- **Date navigation** with calendar picker and quick date filters

### Pattern Detection System
- **Food-marker correlation detection** (immediate and delayed reactions)
- **Blood sugar trend analysis** (high/low patterns, time-of-day patterns)
- **Pattern confidence scoring** based on occurrence frequency and analysis window
- **Pattern dismissal system** for irrelevant patterns
- **Pattern export** to PDF and TXT formats

### Potential Trigger Foods (Phases 1-5 Complete)
- **List view** with per-food status (Potential, No longer potential, Ignored)
- **Calendar view** with food emoji indicators and month navigation
- **Contradiction detection**: Foods marked "No longer potential" if "feel bad" occurs without the food after support dates
- **Contradiction-aware warning suppression**: Contradicted foods don't trigger warning dialogs or risk highlighting
- **Test data generator** with validation fixtures (Donut, Blueberries)
- **In-app expected results helper** for quick QA validation

### Food Warning System
- **In-app warning dialogs** when adding suspicious foods
- **Risk-based highlighting** (red/orange/yellow) for food chips
- **Contradiction-aware suppression** (Phase 5) - contradicted foods no longer show warnings
- **Ignored foods whitelist** via `IgnoredFoodManager`

### Reminder System
- **Custom meal reminders** with flexible scheduling (daily, specific days, intervals)
- **Blood sugar check reminders** with time-based patterns
- **Pattern-based reminders** triggered by detected patterns
- **Confirmation system** with streak tracking
- **Reminder history** with statistics
- **Notification preferences** with sound/vibration settings

### Backup & Restore
- **Full backup** of settings, entries, presets, patterns, reminders
- **Selective backup** with metadata preview
- **Auto-backup** with configurable frequency and retention
- **Folder picker** for backup location selection

### Export Functionality
- **PDF export** with date range selection (table and chart modes)
- **TXT export** with date range selection
- **Pattern export** to PDF with detailed insights

### Localization
- **Multi-language support**: English, German, Spanish
- **Dynamic string resources** throughout the app

### Charting (Vico Library)
- **Blood sugar charts** (morning, afternoon, evening, night)
- **Average blood sugar charts**
- **Insulin charts**
- **Chart date range filtering** with presets
- **Chart preview strip** for quick navigation
- **Marker-based date selection** with tap-to-jump

---

## 4. Current State of Development & Open Issues

### Recent Completed Work (June 2026)
- **Phase 5**: Contradiction-aware food warning suppression integrated
- **Phase 3+4**: Potential trigger foods calendar view with food emojis
- **Phase 1+2**: Potential trigger foods list view with status classification
- **Test data generator** for trigger-food validation
- **In-app expected results helper** for QA
- **Regression fix**: Removed confidence gate that was hiding valid foods

### Known Technical Debt
- **MainActivity.kt size**: ~1.1MB (21,560 lines) - exceeds 2000-line limit significantly
  - Contains UI, data persistence, chart rendering, reminder logic
  - Should be refactored into smaller, focused modules
- **AnalysisScreen.kt size**: ~142KB - large but manageable
- **Multiple PowerShell scripts** in root directory for incremental changes (cleanup recommended)

### Build Notes
- **Compilation**: Use `.\gradlew :app:compileDebugKotlin --no-daemon`
- **Wrapper timeout**: Post-completion wait/I/O timeout is a known tool wrapper issue; Gradle output confirms success
- **Lint**: Set to `abortOnError = false` in build.gradle.kts

### Component Size Limits (User Rules)
- **UI Components**: 500 lines MAX
- **Regular Functions**: 200 lines MAX
- **State Variables per Component**: 10 MAX
- **Effect Hooks per Component**: 5 MAX
- **Classes**: 1000 lines MAX
- **Files**: 2000 lines MAX

**Critical Violations**:
- `MainActivity.kt` is ~21,560 lines (10x over limit)
- Several composables likely exceed 500-line limit

---

## 5. Next Planned Tasks / TODOs

### Immediate (From Previous Session)
- **Phase 6**: User guidance block refinement (not yet started)
- **Phase 7**: Hardening and verification (not yet started)

### Recommended Refactoring (Technical Debt)
1. **Extract MainActivity.kt** into focused modules:
   - `DiaryEntryManager.kt` - Entry CRUD operations
   - `ChartRenderer.kt` - Vico chart logic
   - `ReminderManager.kt` - Reminder scheduling
   - `DataManager.kt` - Data persistence (already exists but could be expanded)
2. **Extract large composables** from MainActivity.kt into separate files
3. **Clean up PowerShell scripts** in root directory (consolidate or remove)

### Feature Enhancements (Potential)
- **Pattern-based reminder refinement** (Phase 6)
- **Additional chart types** (custom column data visualization)
- **Enhanced pattern insights** with actionable recommendations
- **Food preset sharing/import** between users

---

## 6. Key Data Models

### Core Data Classes (in MainActivity.kt)
```kotlin
@Serializable
data class DiabetesEntry(
    val id: String = UUID.randomUUID().toString(),
    var date: String = "",
    var morningBloodSugarLevel: String = "",
    var afternoonBloodSugarLevel: String = "",
    var eveningBloodSugarLevel: String = "",
    var nightBloodSugarLevel: String = "",
    var morningInsulin: String = "",
    var afternoonInsulin: String = "",
    var eveningInsulin: String = "",
    var nightInsulin: String = "",
    var morningNotes: String = "",
    var afternoonNotes: String = "",
    var eveningNotes: String = "",
    var nightNotes: String = "",
    var foodEntriesByColumn: Map<String, List<FoodEntry>> = emptyMap(),
    var markers: List<Marker> = emptyList(),
    var isTestData: Boolean = false
)

@Serializable
data class Marker(
    val id: String = "",
    val type: String, // "feel_bad", "custom"
    val startTime: String, // "morning", "afternoon", "evening", "night", "all_day", "cell_specific"
    val endTime: String = "",
    val customMarkerId: String = "",
    val notes: String = ""
)

@Serializable
data class FoodPreset(
    val name: String,
    val emoji: String,
    val id: String = UUID.randomUUID().toString()
)

@Serializable
data class CustomColumn(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val order: Int = 0,
    val timeOfDay: String
)

@Serializable
data class CustomMarkerType(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String,
    val color: String
)
```

### Pattern Data Classes (in PatternDetection.kt)
```kotlin
@Serializable
data class DetectedPattern(
    val id: String = UUID.randomUUID().toString(),
    val patternType: PatternType,
    val markerName: String,
    val correlatedItem: String, // Food name or blood sugar value
    val occurrences: Int,
    val confidenceScore: Float,
    val timeRange: String,
    val firstDetected: LocalDate,
    val lastOccurrence: LocalDate,
    val details: List<PatternOccurrence>,
    val suggestion: String
)

enum class PatternType {
    FOOD_MARKER_CORRELATION,
    HIGH_BLOOD_SUGAR_PATTERN,
    LOW_BLOOD_SUGAR_PATTERN,
    TIME_OF_DAY_PATTERN,
    CUSTOM
}
```

### Potential Food Data Classes (in PotentialFoodSignals.kt)
```kotlin
private data class PotentialFoodSignal(
    val foodName: String,
    val emoji: String,
    val maxConfidence: Float,
    val patternCount: Int,
    val supportDates: List<LocalDate>,
    val contradictionDates: List<LocalDate>,
    val status: PotentialFoodStatus,
)

enum class PotentialFoodStatus {
    POTENTIAL,
    NOT_POTENTIAL,
    IGNORED
}
```

---

## 7. Important Constants & Configuration

### Pattern Storage
- **PREFS_NAME**: `"pattern_storage"`
- **KEY_PATTERNS**: `"detected_patterns"`
- **KEY_LAST_ANALYSIS**: `"last_analysis_timestamp"`
- **KEY_DISMISSED_PATTERNS**: `"dismissed_patterns"`

### Ignored Foods
- **PREFS_NAME**: `"ignored_foods"`
- **KEY_IGNORED_FOODS**: `"ignored_foods_list"`

### Data Manager
- **PREFS_NAME**: `"diabetes_data"`
- **KEY_ENTRIES**: `"entries"`

### Chart Constants
- **CHART_AXIS_MIN_VISIBLE_LABELS**: 3
- **CHART_AXIS_MIN_LABEL_WIDTH_PX**: (for adaptive spacing)
- **CHART_VIEWPORT_TAP_SLOP_X**: (for tap stability)
- **CHART_TAP_SLOP_PX**: (for tap stability)

---

## 8. Build & Run Commands

```bash
# Compile Kotlin
.\gradlew :app:compileDebugKotlin --no-daemon

# Build debug APK
.\gradlew assembleDebug

# Clean build
.\gradlew clean

# Run tests
.\gradlew test
```

---

## 9. User Rules Summary

### Component Size Limits (ENFORCE ALWAYS)
- **UI Components**: 500 lines MAX
- **Regular Functions**: 200 lines MAX
- **State Variables per Component**: 10 MAX
- **Effect Hooks per Component**: 5 MAX
- **Classes**: 1000 lines MAX
- **Files**: 2000 lines MAX

### State Management
- Single source of truth for each piece of state
- Use `data class` with `copy()` for state updates
- Use `derivedStateOf` for computed values
- Group related state into data classes

### Code Style
- Extract dialogs/modals to separate components
- Extract constants/lists to top-level
- Avoid inline dialogs in parent components
- Use modern Android best practices

### Documentation
- Update `CHANGELOG.md` after every feature/bug fix
- Update `CHAT_HISTORY.md` after every development session
- Both files must be in project root

---

## 10. Quick Start for New AI Model

1. **Read CHANGELOG.md** - Understand recent development history
2. **Read CHAT_HISTORY.md** - Understand conversation context
3. **Review MainActivity.kt** - Main entry point, but be aware of size issues
4. **Review PatternDetection.kt** - Core pattern detection logic
5. **Review PotentialFoodSignals.kt** - Latest feature implementation
6. **Check user rules in .windsurfrules or memory** - Enforce component size limits
7. **Use `.\gradlew :app:compileDebugKotlin --no-daemon`** to verify changes

### Key Integration Points
- **Pattern detection**: Called from MainActivity when user triggers analysis
- **Potential foods**: Integrated in AnalysisScreen.kt
- **Food warnings**: Called from MainActivity when adding foods
- **Reminders**: Scheduled via ReminderScheduler, received by ReminderReceiver

### Testing
- Use `TestDataGenerator.loadTestData(context)` to load test fixtures
- Check in-app expected results helper in Potential Trigger Foods section
- Verify with compile command after changes
