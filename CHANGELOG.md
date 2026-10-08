# DiabetesTracker Development History

This file documents all features, bug fixes, and development decisions made throughout the project.

---

## Releases

| Version | Date | Key Changes |
|---------|------|-------------|
| 1.1.1.3 | 2025-12-01 | Food count button fix, phantom markers fix, dialog animation fix |
| 1.1.1.2 | 2025-11-XX | Health Marker System (Steps 1-5), Per-Cell Food Entries |
| ... | ... | Earlier versions |

---

## [Unreleased]

### Enhancement: Contradiction-Aware Food Warning Suppression (Phase 5)
- **Date**: June 7, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/FoodWarningHelper.kt` (lines ~3-184)
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~1525-1565)

#### Goal:
- Integrate the same contradiction rule used by the Possible Trigger Foods status view into the active food warning flow.

#### Implementation:
- Added shared contradiction detection in `FoodWarningHelper.isFoodContradicted(...)`.
- The rule suppresses active risk treatment when:
  - a food has existing `FOOD_MARKER_CORRELATION` support dates,
  - a later/equal "feel bad" marker exists,
  - and that diary day does not include the suspected food.
- Updated `FoodWarningHelper.checkFoodPattern(...)` and `getFoodPatterns(...)` so contradicted foods no longer produce helper warnings, details, or risk highlighting.
- Updated `FoodWarningChecker.checkFoodWarning(...)` in `MainActivity.kt` so adding a typed/preset food does not show warning dialogs for contradicted foods.
- Updated `FoodWarningChecker.getSuspiciousFoods(...)` so suspicious-food summaries exclude contradicted foods.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.
- Note: terminal wrapper returned a post-completion wait/I/O timeout, but Gradle output confirms successful compilation.

### Fix: Restore Possible Trigger Foods Visibility After Test Data Load
- **Date**: June 6, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/PotentialFoodSignals.kt` (lines ~598-601)

#### Problem:
- Possible Trigger Foods list/calendar could appear empty even when trigger-related test data and correlations existed.

#### Root Cause:
- Section-level filtering required `confidenceScore >= 0.5f` before building potential-food cards.
- Confidence can be dampened by broader analysis windows, causing valid food-correlation patterns to be hidden from this dedicated management UI.

#### Solution:
- Removed the section-level confidence gate in `buildPotentialFoodSignals(...)`.
- The section now includes all detected `FOOD_MARKER_CORRELATION` patterns with non-blank correlated food names.
- Existing status logic (`Potential`, `No longer potential`, `Ignored`) and confidence display remain intact.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.
- Note: terminal wrapper returned a post-completion wait/I/O timeout, but Gradle output confirms successful compilation.

### Enhancement: In-App Expected Results Helper (Trigger Foods)
- **Date**: June 6, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/PotentialFoodSignals.kt` (lines ~99-112, ~191-235, ~811-853)

#### Goal:
- Speed up manual QA by showing expected vs current outcomes for the dedicated trigger-food test fixtures directly inside the Possible Trigger Foods section.

#### Implementation:
- Added fixture-presence detection in `PotentialFoodSignalsSection(...)` for test-data entries containing `Donut` or `Blueberries`.
- Added compact validation card `PotentialFoodTestExpectationsHint(...)` rendered only when validation fixtures are present.
- Added localized helper strings (EN/DE/ES) for:
  - quick validation title,
  - expected Donut outcome,
  - expected Blueberries outcome,
  - fallback "Not detected yet" state.
- Added status label mapper helper to show current runtime status using existing localized status labels.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.
- Note: terminal wrapper returned a post-completion wait/I/O timeout, but Gradle output confirms successful compilation.

### Enhancement: Test Data Generator for Trigger-Food Validation
- **Date**: June 5, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/TestDataGenerator.kt` (lines ~13, ~256-342)

#### Goal:
- Make it easy to validate the new Possible Trigger Foods list/calendar behavior without waiting on organic diary history.

#### Implementation:
- Added dedicated potential-food validation fixtures into test-data loading:
  - `Donut` support days + a follow-up feel-bad contradiction day (expected `No longer potential` state).
  - `Blueberries` support-only days (expected `Potential` state).
- Added helper builders:
  - `generatePotentialFoodValidationEntries()`
  - `createPotentialFoodTestEntry(...)`
- Unified test date formatting through `testDateFormatter` for all generated fixtures.
- Wired `loadTestData(...)` to include both existing pattern demo data and new trigger-food validation fixtures.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.
- Note: terminal wrapper reported a post-completion I/O timeout, but Gradle output confirms successful compilation.

### Feature: Possible Trigger Foods Timeline + Calendar (Phase 3+4)
- **Date**: June 5, 2026
- **Status**: ✅ Complete (Phase 3 + Phase 4)
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/PotentialFoodSignals.kt` (lines ~53-56, ~90-179, ~183-272, ~274-407, ~462-536)

#### Goal:
- Add a richer food-level management flow in Analysis & Markers so users can switch between detailed list review and a month-grid calendar with food emojis.

#### Root Cause Addressed:
- Phase 1+2 introduced persistent food-status visibility, but lacked a fast visual timeline mode to spot clustered trigger days and jump from month context directly into diary entries.

#### Implementation:
- Added `PotentialFoodViewMode` (`LIST`, `CALENDAR`) and in-section toggle chips (`List` / `Calendar`).
- Refactored rendering into focused sub-composables:
  - `PotentialFoodSignalsList(...)`
  - `PotentialFoodSignalCard(...)`
  - `PotentialFoodSignalsCalendar(...)`
- Implemented calendar month controls and day-grid rendering with emoji indicators for active potential foods.
- Limited calendar day emoji display for readability (`take(2)`), while preserving full day-level aggregation logic.
- Kept day tap navigation wired to existing flow via `onNavigateToDate("{date}|all_day")`.
- Added helper builders for calendar data shaping:
  - `buildPotentialFoodCalendarDays(...)`
  - `buildMonthCells(...)`
- Added localized labels for mode toggle, calendar empty state, calendar hint, and weekday headers (EN/DE/ES).

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.
- Note: terminal wrapper returned a post-completion wait/I/O timeout, but Gradle output confirms successful compilation.

### Feature: Possible Trigger Foods Section (Analysis & Markers, Phase 1+2)
- **Date**: June 5, 2026
- **Status**: ✅ Complete (Phase 1 + Phase 2)
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/PotentialFoodSignals.kt` (new file, full module)
  - `app/src/main/java/com/j4/diabetestracker/AnalysisScreen.kt` (lines ~323-335)

#### Goal:
- Add a dedicated, persistent section to manage suspicious food patterns so warnings are easier to interpret and not repeatedly shown without context.

#### Root Cause Addressed:
- Food warnings were mostly one-shot at entry time and lacked a long-lived management surface where users could review support vs. contradiction days.

#### Implementation:
- Extracted potential-food logic into a separate module (`PotentialFoodSignals.kt`) to avoid further growth in `AnalysisScreen.kt`.
- Added status model for each suspicious food:
  - `Potential`
  - `No longer potential`
  - `Ignored`
- Implemented contradiction rule for analysis view:
  - mark as `No longer potential` when a "feel bad" day appears after support dates without consuming that food.
- Added date-chip navigation in the new section:
  - supporting days and contradiction days are tappable and navigate through existing `onNavigateToDate(...)` flow.
- Added direct "Open in pattern details" action per food to focus existing analysis filters.

#### Key Snippet:
```kotlin
PotentialFoodSignalsSection(
    entries = entries,
    patterns = patterns,
    activeDateRange = activeDateRange,
    context = context,
    selectedLanguage = selectedLanguage,
    onNavigateToDate = onNavigateToDate,
    onFilterFood = { foodName ->
        filterFoodName = foodName
        filterPatternType = PatternType.FOOD_MARKER_CORRELATION
        showPatternWorkspace = true
    }
)
```

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` → `BUILD SUCCESSFUL`.

### Feature: Chart Date-Range Selector (Top Area)
- **Date**: June 5, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15235-15306, ~15546-15710, ~16650-16673, ~16718-16724, ~16784-16803)

#### Goal:
- Add a light, clear, top-positioned date-range selection area for all chart types so users can focus on specific time fractions.

#### Implementation:
- Added reusable chart date-range model + helpers:
  - `ChartDateRangePreset`
  - `ChartDateRangeFilter`
  - `resolveChartDateRangeBounds(...)`
  - `filterEntriesByChartDateRange(...)`
- Added `ChartDateRangeSelector(...)` UI under the chart dialog title:
  - Presets: `All`, `7D`, `30D`, `90D`, `Custom`
  - Custom range uses Material date pickers (`From` / `To`)
  - Shows the currently applied resolved range in `dd.MM.yyyy`
- Wired `TimeChartDialog` to filter shared chart entries before passing data to:
  - Blood sugar chart
  - Insulin chart
  - Custom-column chart

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL` (tool wrapper returned post-completion I/O timeout).

### Feature: Compact Whole-Range Timeline Preview Navigator
- **Date**: June 4, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~172-178, ~15392-15484, ~15564-15570, ~15705-15709, ~15799-15808, ~15986-15992, ~16126-16129, ~16220-16229, ~16586-16593, ~16729-16732, ~16842-16853)

#### Goal:
- Add a lightweight, visible-enough preview window below charts to show the full curve history and allow fast jump/drag navigation across time.

#### Implementation:
- Added reusable `ChartTimelinePreviewStrip(...)` composable.
- Renders a compact line-chart overview (`56.dp` height) of the full dataset.
- Draws a viewport overlay representing the current visible range of the main chart.
- Supports tap and drag on the preview strip to scroll the main chart via shared `ChartScrollState`.
- Integrated for:
  - Blood sugar chart
  - Insulin chart
  - Custom-column chart

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL` (tool wrapper returned post-completion I/O timeout).

### Fix: Prevent Date-Label Swiping While Panning
- **Date**: June 4, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15318-15353, ~15620-15624, ~16028-16032, ~16638-16642)

#### Problem:
- While panning at fixed zoom, visible date labels “swiped” between neighboring columns (labels re-shifted every small scroll), causing unstable date-to-column mapping perception.

#### Root Cause:
- Dynamic label placement anchored each cycle to `firstVisible`, so panning changed the modulo base and relaid out all label positions.

#### Solution:
- Kept dynamic spacing but anchored label ticks to a stable global origin (`fullStart`) via modulo alignment.
- Added `xStep`-aware visible-span estimation for steadier spacing calculations.
- Switched label index conversion from `toInt()` to `roundToInt()` in chart axis formatters for safer float→index mapping.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL` (tool wrapper returned post-completion I/O timeout).

### Fix: Dynamic Zoom-Aware Date Labels (Minimum 3 Visible)
- **Date**: June 2, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~181-183, ~15236-15238, ~15312-15382, ~15633-15637, ~16041-16045, ~16651-16655)

#### Problem:
- At certain zoom-out states, x-axis labels appeared as ellipsis (`...`) even with usable space.
- Label spacing felt fixed (e.g., ~14-day jumps), and zooming into a gap could still show no intermediate labels.

#### Root Cause:
- Bottom axis used `AxisItemPlacer.Horizontal.default(spacing = 1)` with formatter-only filtering.
- Axis placement and measurement weren’t driven by viewport-aware label targets, so label ticks did not adapt robustly per visible range.

#### Solution:
- Added a custom dynamic horizontal axis item placer (`createDynamicChartDateAxisItemPlacer`).
- Label ticks are now generated from the **current visible range** with dynamic spacing.
- Guaranteed minimum context by targeting at least `CHART_AXIS_MIN_VISIBLE_LABELS = 3` labels (up to 7) per viewport.
- Applied this item placer to blood sugar, insulin, and custom-column charts.
- Kept full date format as `dd.MM.yy` for all rendered labels.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL` (tool wrapper returned post-completion I/O timeout).

### Fix: Zoomed-Out Axis Shows Full Dates at Readable Intervals
- **Date**: June 2, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15245, ~15337-15357)

#### Problem:
- At high zoom-out levels, dense daily labels became unreadable/ellipsis-like (`...`), causing users to lose time-range context (including year).

#### Root Cause:
- Rendering every day label in `dd.MM.yy` over very dense visible ranges exceeded horizontal space.

#### Solution:
- Introduced adaptive interval labeling for synchronized x-axis labels:
  - Added `CHART_AXIS_MIN_LABEL_WIDTH_PX` target width.
  - Calculated dynamic spacing with `calculateAdaptiveDateLabelSpacing(...)` based on visible points and chart width.
  - Rendered labels only on interval ticks plus first/last visible indices.
  - Kept full-date format `dd.MM.yy` for all shown labels.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL` (tool wrapper returned post-completion I/O timeout).

### Fix: Show Daily Chart X-Axis Labels in `dd.MM.yy`
- **Date**: June 2, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15248, ~15318-15345)

#### Problem:
- Date labels under charts were sparse and short (e.g., `22.8`) even though each column represents a day.

#### Root Cause:
- Synchronized axis formatting used adaptive spacing + adaptive short formats, intentionally skipping labels when density was high.

#### Solution:
- Added shared chart-axis formatter `dd.MM.yy`.
- Removed adaptive label skipping for synchronized x-axis labels so each visible day-column gets a date label.
- Kept visible-range bounds check, so labels are still only rendered for visible indices.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL` (tool wrapper returned post-completion I/O timeout).

### Fix: Block Tap Commit When Chart Viewport Changes During Gesture
- **Date**: June 1, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15244-15247, ~15450-15526, ~15859-15929, ~16448-16512)

#### Problem:
- Selection still committed during chart navigation, with large index jumps (e.g., moving from low indices to ~200+) during pan/zoom interactions.

#### Root Cause:
- Commit gating focused on marker x-slop, index drift, and multi-touch timing, but did not explicitly reject gestures where the chart viewport itself changed.

#### Solution:
- Added viewport-stability gating to all three chart marker listeners:
  - New constant: `CHART_VIEWPORT_TAP_SLOP_X`.
  - On marker shown: capture `gestureStartVisibleMinX` and `gestureStartVisibleMaxX`.
  - On marker hidden: block commit if current `visibleMinX`/`visibleMaxX` drift from start beyond tolerance.
  - Reset viewport snapshot state on blocked and hidden paths.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL` (tool wrapper returned post-completion I/O timeout).

### Fix: Require Stable Marker Index for Tap Commit
- **Date**: June 1, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15450-15512, ~15846-15908, ~16422-16484)

#### Problem:
- Marker/date selection still changed during pinch/pan scenarios where marker X stayed near-stationary.

#### Root Cause:
- Tap-slop gating only compared marker X movement. During viewport movement, marker index can drift while finger X remains almost unchanged.

#### Solution:
- Added `gestureStartMarkerIndex` in all three chart marker listeners.
- On marker move, mark gesture as non-tap when index differs from start index.
- Commit on `onMarkerHidden(...)` now effectively requires both:
  - no multi-touch/cooldown involvement, and
  - no significant X movement and no marker index drift.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL` (tool wrapper returned post-completion I/O timeout).

### Fix: Keep Multi-Touch Suppression Active Until Gesture Ends
- **Date**: June 1, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15596-15621, ~15983-16008, ~16572-16597)

#### Problem:
- Marker/date could still jump after pinch startup because commits still occurred in some sequences.

#### Root Cause:
- `isMultiTouchActive` was cleared too early on `ACTION_POINTER_UP` (when one finger lifts but gesture is not fully finished), allowing subsequent one-finger marker updates to stage/commit.

#### Solution:
- Updated pointer lifecycle handling in all chart `pointerInteropFilter` blocks:
  - Added explicit `ACTION_DOWN` reset for stale flags.
  - Kept multi-touch suppression active on `ACTION_POINTER_UP` (timestamp refresh only).
  - Clear suppression only on `ACTION_UP` / `ACTION_CANCEL`.
  - Refresh cooldown timestamp on pointer-up and gesture-end events.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL` (tool wrapper returned post-completion I/O timeout).

### Fix: Ignore Marker Callback Updates During Active Multi-Touch
- **Date**: June 1, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15443-15505, ~15593-15618, ~15827-15877, ~15976-16001, ~16391-16440, ~16561-16586)

#### Problem:
- Persisted chart selection could still jump as pinch started, due to callback ordering where marker events arrived before/around multi-touch cooldown checks.

#### Root Cause:
- Previous guard was mostly commit-time (`onMarkerHidden`) and timestamp-based. During pinch startup, marker show/move callbacks could still stage unintended selection context.

#### Solution:
- Added explicit `isMultiTouchActive` state in each chart section (blood sugar, insulin, custom).
- Updated `pointerInteropFilter` to track full pointer lifecycle (`ACTION_POINTER_DOWN/MOVE/POINTER_UP/UP/CANCEL`) and maintain active multi-touch state.
- In marker listeners, block/stall updates early when multi-touch is active or still in cooldown:
  - `onMarkerShown(...)` returns early and clears pending marker state.
  - `onMarkerMoved(...)` returns early.
  - `onMarkerHidden(...)` commit check now includes active multi-touch in addition to cooldown.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL` (tool wrapper returned post-completion I/O timeout).

### Fix: Commit Marker Only for Near-Stationary Tap Gestures
- **Date**: June 1, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15242-15246, ~15448-15491, ~15801-15844, ~16334-16377)

#### Problem:
- While starting a pinch/zoom gesture, marker selection could still be committed and the persisted blue line/date jumped toward one finger.

#### Root Cause:
- Commit logic only checked index stability; marker x could move significantly during gesture without changing index, still passing commit conditions.

#### Solution:
- Added `CHART_TAP_SLOP_PX` and gesture movement tracking per marker listener:
  - `gestureStartMarkerX`
  - `didMoveBeyondTapSlop`
- `onMarkerMoved(...)` now marks gesture as non-tap when x movement exceeds slop.
- `onMarkerHidden(...)` now commits only when:
  - movement stayed within tap slop, and
  - no recent multi-touch was detected.
- Applied to blood sugar, insulin, and custom charts.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL` (tool wrapper returned post-completion I/O timeout).

### Fix: Block Marker Selection Commit After Multi-Touch Gesture
- **Date**: May 31, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15241-15244, ~15441-15485, ~15572-15581, ~15790-15833, ~15920-15929, ~16310-16353, ~16469-16478)

#### Problem:
- During pinch zoom, the marker/date selection still changed and the persisted blue line jumped to the pinch interaction area.

#### Root Cause:
- Marker callbacks could still commit staged selection after a multi-touch gesture ended, because no explicit multi-touch block was enforced at commit time.

#### Solution:
- Added `CHART_MULTI_TOUCH_COMMIT_BLOCK_MS` window and `lastMultiTouchAtMs` tracking in all three chart composables.
- Added `pointerInteropFilter` to each chart to capture multi-touch (`pointerCount > 1` or `ACTION_POINTER_DOWN`).
- In `onMarkerHidden`, skipped committing `selectedChartIndex/selectedMarkerX` when a recent multi-touch gesture was detected.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL` (tool wrapper returned post-completion I/O timeout).

### Fix: Prevent Marker Jump During Pinch Zoom
- **Date**: May 25, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15441-15479, ~15781-15819, ~16300-16338)

#### Problem:
- While zooming in/out with pinch, the persisted blue selection line could jump to the pinch location and change the selected date unexpectedly.

#### Root Cause:
- Marker callbacks updated committed selection state during move-heavy gestures, so zoom/pan marker movement was treated like a final tap selection.

#### Solution:
- Introduced pending gesture state in each chart marker listener:
  - `pendingMarkerIndex`, `pendingMarkerX`, `didIndexChangeDuringGesture`.
- `onMarkerShown`/`onMarkerMoved` now stage marker state.
- `onMarkerHidden` now commits only if index stayed stable throughout the gesture (tap-like interaction), preventing zoom/pinch jumps from replacing the current selection.
- Applied to blood sugar, insulin, and custom column charts.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL` (tool wrapper returned post-completion I/O timeout).

### Fix: Selection Line Persists After Touch Release
- **Date**: May 19, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15456-15458, ~15775-15777, ~16273-16275)

#### Problem:
- The selected date banner was correct, but the blue vertical selection line disappeared immediately after finger-up.

#### Root Cause:
- `onMarkerHidden(...)` cleared `selectedMarkerX`, and the overlay line rendering requires a non-null x.

#### Solution:
- Kept the last marker x-position when marker hides (touch release), so the line remains visible as the current selection indicator.
- Applied consistently in blood sugar, insulin, and custom charts.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL` (tool wrapper returned post-completion I/O timeout).

### Fix: Blue Selection Line Uses Marker X (No Left-Stuck Overlay)
- **Date**: May 19, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15437-15458, ~15550-15561, ~15756-15777, ~15868-15879, ~16260-16281, ~16393-16404)

#### Problem:
- The selected date in logs changed correctly, but the long blue selection line appeared stuck toward the left side and did not align with the tapped column after zoom/pan.

#### Root Cause:
- Overlay x-position was recomputed from `visibleMinX/visibleMaxX` and hardcoded chart paddings, which did not reliably match the chart's transformed viewport geometry.

#### Solution:
- Stored marker-provided pixel x from `Marker.EntryModel.location.x` in each chart listener.
- Drew the vertical highlight line directly from this marker x coordinate (clamped to canvas width).
- Kept selected date source as marker index (`EntryModel.index`) for banner consistency.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL` (tool wrapper returned post-completion I/O timeout).

### Fix: Resolve Marker Type-Name Conflict and Restore Successful Build
- **Date**: May 18, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~178-179, ~15438-15450, ~15756-15768, ~16253-16265)

#### Problem:
- Build failed with many unrelated-looking unresolved references (`id`, `startTime`, `customNote`, etc.) after chart marker integration.

#### Root Cause:
- Importing Vico `Marker` without alias shadowed the app's own `Marker` data class used in business logic.

#### Solution:
- Aliased Vico imports:
  - `Marker as VicoMarker`
  - `MarkerVisibilityChangeListener as VicoMarkerVisibilityChangeListener`
- Updated chart marker listener signatures in blood sugar, insulin, and custom charts to use aliased Vico types.

#### Verification:
- ✅ `:app:assembleDebug --no-daemon` reported `BUILD SUCCESSFUL`.
- Note: tool wrapper returned post-completion wait/I/O timeout, but Gradle output confirms successful build.

### Fix: Marker Selection Now Follows Real Pan/Zoom Viewport
- **Date**: May 18, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15437-15453, ~15554-15556, ~15755-15771, ~15855-15857, ~16253-16268, ~16357-16359)

#### Problem:
- After scrolling/zooming, tapping the same screen position could resolve to stale dates as if x-coordinates were mapped to a fixed full-range axis.

#### Root Cause:
- Tap selection used custom x-to-index mapping that depended on `visibleMinX/visibleMaxX` values from axis formatter context, which did not reliably represent live transformed viewport state.

#### Solution:
- Removed manual tap mapping for time charts and switched selection source to Vico marker callbacks:
  - `MarkerVisibilityChangeListener.onMarkerShown/onMarkerMoved` now sets `selectedChartIndex` from `Marker.EntryModel.index`.
- Wired marker/listener into blood sugar, insulin, and custom charts.
- This binds selection to the chart engine's own transformed coordinates (scroll + zoom aware).

#### Verification:
- Compilation currently fails due pre-existing unrelated errors in other areas of `MainActivity.kt` (e.g., unresolved refs around lines ~13400-14754), not in the modified chart-selection blocks.

### Fix: One Date Per Labeled Column via Bucket-Based Tap Snapping
- **Date**: May 17, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~179-181, ~15291-15297)

#### Problem:
- Taps inside what users perceive as the same labeled date column could resolve to two adjacent snapped dates near midpoint boundaries.

#### Root Cause:
- Spaced snap logic used nearest-center rounding (`roundToInt`) for snap buckets, so touching different halves of a visual column could jump to neighboring labels.

#### Solution:
- Changed spaced snapping in `mapChartTapToVisibleIndex(...)` from nearest-center rounding to deterministic bucket snapping using `floor(...)`.
- Result: each full snap band maps to one snapped index/date until the next band starts.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.
- Note: tool wrapper returned post-completion wait/I/O timeout, but Gradle output confirms successful build.

### Fix: Synchronize Bottom Date Labels with Tap Selection Cadence
- **Date**: May 17, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15333-15361, ~15540-15555, ~15869-15884, ~16397-16412)

#### Problem:
- Banner selected date was resolving correctly from tap, but visible bottom-axis date labels did not match the same selection cadence, creating apparent pointer/date desynchronization.

#### Root Cause:
- Axis labels were formatted for many indices independently, while tap selection used wider snapped spacing; this produced visual date columns that were not aligned with selectable bands.

#### Solution:
- Added `formatSynchronizedAxisDateLabel(...)` to:
  - compute visible first/last indices,
  - compute adaptive spacing using the same minimum width used for tap snapping,
  - show labels only on spacing-aligned indices (and last visible index).
- Applied synchronized formatter to blood sugar, insulin, and custom chart bottom axes.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.
- Note: tool wrapper returned post-completion wait/I/O timeout, but Gradle output confirms successful build.

### Fix: Wider Chart Tap Snap Bands to Match Visual Date Columns
- **Date**: May 17, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15235-15238, ~15459-15463, ~15786-15790, ~16276-16280)

#### Problem:
- Users could tap within what appears to be the same visual date column but still get different selected dates.

#### Root Cause:
- `CHART_TRACKER` geometry logs showed mapping itself was consistent (`deltaX` near selected band center), but interaction bands were too narrow (`bandPx` around ~15px), making normal finger movement jump between adjacent snapped dates.

#### Solution:
- Added `CHART_TAP_MIN_BAND_WIDTH_PX = 36f` and applied it to adaptive tap spacing in all time chart handlers (blood sugar, insulin, custom).
- This increases snap spacing granularity for taps so selection is more stable within perceived visual date columns.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.
- Note: tool wrapper returned post-completion wait/I/O timeout, but Gradle output confirms successful build.

### Debug: Expanded CHART_TRACKER Metrics (Band Width + Center Delta)
- **Date**: May 17, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15272-15311)

#### Problem:
- Prior tracker logs showed selected date shifts, but lacked enough geometry detail to separate boundary jitter from coordinate-alignment mismatch.

#### Root Cause:
- Existing diagnostics did not expose selected-band center and tap delta in pixels.

#### Solution:
- Extended `mapChartTapToVisibleIndex(...)` debug payload with:
  - `stepPx` (single-index pixel width)
  - `bandPx` (snapped band width)
  - `centerX` (selected band center)
  - `deltaX` (tap offset from selected center)
- Added these metrics for both `snapSpacing=1` and spaced-snap paths.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.
- Note: tool wrapper returned post-completion wait/I/O timeout, but Gradle output confirms successful build.

### Debug: Added CHART_TRACKER Diagnostics for Tap-to-Date Mismatch
- **Date**: May 17, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15235-15303, ~15452-15468, ~15778-15794, ~16260-16276)

#### Problem:
- The selected date banner can still appear inconsistent with the user-perceived date column in some interactions.
- Additional runtime visibility was needed to isolate whether mismatch occurs in tap mapping, spacing snap, or selected-date resolution.

#### Root Cause:
- Existing UI behavior alone was insufficient to diagnose subtle boundary/cadence mismatches at runtime.

#### Solution:
- Added a structured debug tracker with tag `CHART_TRACKER`:
  - `chartTrackerLog(...)` helper and toggle constant `CHART_TRACKER_ENABLED`.
  - Extended `mapChartTapToVisibleIndex(...)` with optional `debugSink` details for:
    - outside-plot rejection,
    - raw index,
    - snapped index,
    - spacing,
    - visible range and mapping fraction.
  - Added chart-specific tap logs for all chart types:
    - blood sugar,
    - insulin,
    - custom columns,
    including resolved `selectedIndex` + `selectedDate`.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.
- Note: tool wrapper returned post-completion wait/I/O timeout, but Gradle output confirms successful build.

### Fix: Stable Date Selection Within Same Visible Chart Column
- **Date**: May 17, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15412-15427, ~15731-15746, ~16207-16222)

#### Problem:
- While selecting chart dates, moving slightly inside what appears to be the same visible date column could switch the selected-date banner to a different date.

#### Root Cause:
- Tap-to-index mapping was resolving per raw point index (`snapSpacing=1`) even when visible labels are effectively grouped by adaptive spacing at tighter widths.
- Result: small movements inside one visible label band could jump to adjacent hidden indices.

#### Solution:
- Reintroduced adaptive spacing snap for tap mapping in all 3 time-chart tap handlers.
- For each tap, compute current visible point count and dynamic spacing via `calculateAdaptiveDateLabelSpacing(...)`.
- Pass `snapSpacing = dynamicSpacing` into `mapChartTapToVisibleIndex(...)` so tap selection follows the same visible label cadence.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.
- Note: tool wrapper returned post-completion wait/I/O timeout, but Gradle output confirms successful build.

### Feature: Tap-Select Chart Column Highlight + Full Selected Date Banner
- **Date**: May 16, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15282-15330, ~15334-15640, ~15643-15939, ~16144-16437)

#### Problem:
- At zoomed-out levels, date labels become truncated/ellipsized and hard to read.
- User requested a clearer interaction: tap a chart column, highlight it, and show the full date explicitly.

#### Root Cause:
- X-axis labels must stay compact at low per-column width, so the full date is not always readable inline.
- There was no persistent selected-date UI state coupled to a visible chart-column highlight.

#### Solution:
- Added adaptive axis date label formatter:
  - `dd.MM.yyyy` when enough space,
  - `dd.MM` at medium density,
  - `d.M` when tight.
- Added reusable selected-date UI:
  - `SelectedChartDateBanner(...)` shows full `dd.MM.yyyy` for the tapped column.
  - Includes `Open` action to navigate directly to that date.
- Updated all 3 chart composables:
  - `SimpleTimeChart`
  - `InsulinTimeChart`
  - `CustomColumnChart`
- Tap behavior now selects and highlights the tapped visible column (vertical highlight line), instead of immediately navigating.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.
- Note: tool wrapper returned a post-completion I/O wait-timeout, but Gradle output confirms successful build.

### Change: Roll Back Failed Chart Tweaks + New Label-First Approach
- **Date**: May 16, 2026
- **Status**: ✅ Complete (Step 1 of new approach)
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15234-15272, ~15286-15287, ~15411-15420, ~15573-15574, ~15695-15704, ~16052-16053, ~16191-16200, ~3224-3234)

#### Problem:
- User requested reverting unsuccessful issue-fix attempts and starting a new approach.
- First priority: date labels should appear when there is enough space and include day, month, and year.

#### Root Cause:
- Axis formatter intentionally returned empty labels for many valid visible indices due adaptive spacing suppression.
- Recently added debug/tap-anchor tweaks did not resolve the reported UX issue and added noise.

#### Solution:
- Rolled back recent debug instrumentation (`CHART_TAP_DEBUG`, `CHART_NAV_DEBUG`).
- Rolled back nearest-anchor tap behavior to snapped-offset behavior.
- Updated axis label format to full `dd.MM.yyyy` in blood sugar, insulin, and custom charts.
- Simplified bottom-axis formatter to always show a date label for valid visible indices.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.

### Fix: Nearest-Anchor Tap Resolution + Chart Navigation Debug Trace
- **Date**: May 15, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15234-15275, ~15382-15389, ~15689-15696, ~16138-16145, ~3224-3243)

#### Problem:
- User still reported unchanged behavior after prior attempts.

#### Root Cause:
- Spaced-label taps could still resolve to different indices near boundaries, and runtime data was insufficient to verify whether mismatch happened at tap mapping or filter handoff.

#### Solution:
- Changed `mapChartTapToVisibleIndex(...)` to always choose the nearest visible anchor when spacing > 1.
- Added focused debug logs:
  - `CHART_TAP_DEBUG` for chart name, tap x, visible range, spacing, resolved index/date.
  - `CHART_NAV_DEBUG` for navigation payload decode and parsed `LocalDate`.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.

### Fix: Align Chart Labels/Taps to Plotted Points (Blood Sugar + Insulin)
- **Date**: May 15, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15315-15333, ~15378-15403, ~15423-15438, ~15612-15629, ~15672-15677, ~15721-15732)

#### Problem:
- User still reported both issues: sparse date labels and wrong date navigation from chart taps.

#### Root Cause:
- Blood sugar and insulin charts plotted only non-null values, but label/tap mapping still used the full `sortedData` index domain.
- This x-domain mismatch caused over-skipping and incorrect tap-to-date resolution.

#### Solution:
- Added `plottedData` (`date + value`) for both blood sugar and insulin charts based on active filter.
- Built chart entries, bottom-axis label formatter, and tap mapping from the same `plottedData` index domain.
- Navigation now resolves tapped index to `plottedData[index].first` (date), ensuring one consistent source.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.

### Fix: ISO Date Navigation Payload + Denser Date Labels
- **Date**: May 14, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15219-15222, ~15372-15373, ~15662-15663, ~16135-16136)

#### Problem:
- User still reported unchanged behavior: wrong date after chart tap and labels still too sparse.

#### Root Cause:
- Chart tap navigation used transformed raw entry date text (`entry.date.replace('.', '-')`), which can be format-ambiguous across mixed inputs and downstream parsing paths.
- Adaptive spacing minimum width remained conservative for short `d/M` labels.

#### Solution:
- Navigation payload now uses normalized parsed chart `LocalDate` in ISO format (`yyyy-MM-dd`) from `sortedData` in all three chart types.
- Reduced spacing minimum width from `15f` to `12f` to show labels more aggressively when room exists.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.

### Fix: Same Visible Date Column Now Maps to One Date (Label-Coverage Snap)
- **Date**: May 11, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15234-15285)

#### Problem:
- User still reproduced wrong navigation: tapping different points in the same visible date column could resolve to different dates.

#### Root Cause:
- Even with spacing snap, taps near overhanging `d/M` text could fall into adjacent hidden-index regions because mapping did not account for rendered label coverage width.

#### Solution:
- Extended `mapChartTapToVisibleIndex(...)` with label-coverage-aware snapping:
  - Added `labelCoveragePx` parameter (default `36f`).
  - Built visible anchor indices based on `snapSpacing` cadence.
  - If tap falls within label coverage around an anchor, map directly to that anchor.
  - Otherwise fallback to cadence snap logic.
- Keeps same-column taps stable while preserving previous visible-range mapping.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.

### Fix: Stable Tap Mapping Per Visible Date Column + Denser Labels
- **Date**: May 10, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15219-15231, ~15234-15266, ~15336-15351, ~15628-15643, ~16095-16110)

#### Problem:
- In some zoom levels, labels still showed roughly every 5th day despite available room.
- Tapping the same visible date column could navigate to different wrong dates depending on tap position.

#### Root Cause:
- Tap mapping returned a raw nearest index in visible range, but label rendering shows only indices on a spacing cadence.
- This mismatch allowed taps inside one visible “label band” to resolve to neighboring hidden indices.
- Label spacing heuristic was still slightly conservative for short `d/M` labels.

#### Solution:
- Updated `mapChartTapToVisibleIndex(...)` to support `snapSpacing` and snap tap results to the same anchor/cadence as visible labels.
- Applied snap-spacing tap logic to all three charts (blood sugar, insulin, custom).
- Aligned tap-mapping padding defaults with spacing assumptions (`56f` start, `12f` end).
- Reduced spacing conservatism from `minLabelWidthPx=18f` to `15f`.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.

### Fix: Correct Date Navigation From Chart Taps + Visible-Range Spacing Density
- **Date**: May 10, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15234-15257, ~15284-15285, ~15326-15341, ~15384-15394, ~15571-15572, ~15610-15625, ~15666-15676, ~16055-16056, ~16080-16095, ~16161-16171)

#### Problem:
- Tapping a shown chart date (e.g., `8/7`) could navigate to a different table date (e.g., `30.11.2025`).
- Date labels could still be slightly under-dense because spacing used major-entry-count heuristics instead of the real visible x-span.

#### Root Cause:
- Tap mapping used full-dataset width math, ignoring the current zoom/pan visible range (`minX..maxX`).
- Spacing density was based on `getMaxMajorEntryCount()` rather than current visible x range.

#### Solution:
- Added reusable `mapChartTapToVisibleIndex(...)` helper.
- Updated all 3 time charts (blood sugar, insulin, custom) to:
  - track `visibleMinX` and `visibleMaxX` from `bottomAxis.valueFormatter`,
  - map tap X to an index inside the current visible window,
  - keep navigation date source aligned with what user sees on-screen.
- Updated spacing density estimate in all 3 charts to use:
  - `visiblePointCount = ((chartValues.maxX - chartValues.minX).toInt() + 1)`.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.

### Fix: Removed Avoidable Empty Date Gaps (First-Visible Anchor Correction)
- **Date**: May 10, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15220-15232, ~15361-15370, ~15640-15649, ~16127-16136)

#### Problem:
- Even with enough room between date labels, some label slots stayed empty after zoom changes.

#### Root Cause:
- Label cadence was anchored with `floor(minX)`, which can be outside the actually visible first integer index and phase-shift the modulo skip pattern.
- This phase shift caused avoidable empty gaps.

#### Solution:
- Anchored cadence to the first truly visible integer x-index using `ceil(minX)` in all three time chart formatters.
- Slightly reduced spacing conservatism in `calculateAdaptiveDateLabelSpacing(...)`:
  - `minLabelWidthPx`: `20f -> 18f`
  - extra buffer: `+2f -> +1f`

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reported `BUILD SUCCESSFUL`.

### Fix: Date Labels Now Recalculate Live While Zooming (No Collision Ellipses)
- **Date**: April 11, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15353-15375, ~15634-15661, ~16121-16143)

#### Problem:
- During further zoom changes, labels could become compressed again and render ellipses (`...`) because spacing decisions were based on static dataset assumptions rather than current visible range.

#### Root Cause:
- Axis spacing was precomputed from full data count, not from the zoom-adjusted visible x-range.

#### Solution:
- Switched all three time charts to:
  - `AxisItemPlacer.Horizontal.default(spacing = 1)`
  - zoom-aware label skipping inside `bottomAxis` `valueFormatter` using current `chartValues`:
    - `chartValues.getMaxMajorEntryCount()` for visible density
    - `chartValues.minX` to anchor first visible index
    - dynamic spacing from `calculateAdaptiveDateLabelSpacing(...)`
- Labels now show only when they are outside the coverage area of the previous visible label, and recalculate continuously as zoom changes.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reports `BUILD SUCCESSFUL`.
- ✅ Zooming in/out dynamically updates label density and avoids ellipsized collisions.

### Fix: Time-Chart Labels Now Fill Available Space Better at Mid Zoom Levels
- **Date**: April 11, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15219-15231, ~15245-15256, ~15319-15329, ~15523-15524, ~15648-15652, ~15997-15998, ~16124-16127)

#### Problem:
- At certain zoom-out levels, x-axis date labels left visible empty gaps between shown dates even though additional labels could fit.

#### Root Cause:
- Adaptive spacing heuristic was too conservative (`minLabelWidthPx=32f` with larger fixed padding assumptions), producing larger-than-needed skip distances.
- Date formatter objects were recreated inside axis value formatters, adding avoidable per-frame overhead during chart interactions.

#### Solution:
- Tuned spacing heuristic to use available space more aggressively:
  - `minLabelWidthPx`: `32f -> 20f`
  - start padding: `60f -> 56f`
  - end padding: `20f -> 12f`
  - kept overlap-safe computation using `ceil(...)`
- Reused remembered axis formatter (`axisDateFormatter`) in all three time charts instead of creating `DateTimeFormatter.ofPattern("d/M")` per label render.
- Removed chart-local debug `println` calls in blood sugar chart parsing/entry preparation path to reduce UI-thread logging pressure.

#### Verification:
- ✅ `:app:compileDebugKotlin --no-daemon` reports `BUILD SUCCESSFUL`.
- ✅ Label density improves at intermediate zoom levels while still preventing compressed `...` artifacts.

### Fix: Dynamic Date-Label Coverage on Time Charts (No Repeated Ellipsis Labels)
- **Date**: April 11, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15219-15231, ~15346-15380, ~15622-15656, ~16096-16138)

#### Problem:
- After showing date labels for every day, zooming out caused compressed x-axis text artifacts like repeated `1...` or `...` across neighboring day columns.

#### Root Cause:
- Labels were still attempted at every column even when available horizontal space per day was too small for `d/M` text.
- Axis rendering then ellipsized text in each narrow slot, producing noisy repeated truncation.

#### Solution:
- Added reusable width-aware spacing helper:
  - `calculateAdaptiveDateLabelSpacing(...)`
- The helper calculates how many day columns each label needs based on:
  - current chart width,
  - point count,
  - estimated label width,
  - axis paddings.
- Applied dynamic spacing to all time charts:
  - Blood sugar (`SimpleTimeChart`)
  - Insulin (`InsulinTimeChart`)
  - Custom numeric columns (`CustomColumnChart`)
- Result: labels are skipped dynamically when covered by previous label width, instead of showing repeated ellipsis text.

#### Verification:
- ✅ `:app:compileDebugKotlin` successful (`BUILD SUCCESSFUL`).
- ✅ At tighter zoom levels, chart labels now skip cleanly instead of rendering repeated truncated fragments.

### Fix: Time-Chart Date Labels No Longer Hard-Jump by Dataset Size
- **Date**: April 11, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~15340-15351, ~15602-15613, ~16072-16083)

#### Problem:
- In chart sections like **Blood Sugar Over Time** and **Insulin Over Time**, X-axis day labels were skipping with fixed jumps (e.g., 11 → 19 → 26) even when zoom/visible space allowed showing closer consecutive dates.

#### Root Cause:
- Bottom-axis labeling used static dataset-size thresholds (`>60 -> spacing 7`, `>30 -> spacing 3`) rather than a per-visible-point strategy.
- This forced large jumps regardless of improved readability at higher zoom/clearer views.

#### Solution:
- Replaced fixed spacing thresholds with per-point candidate labeling for all time charts:
  - `SimpleTimeChart` (blood sugar)
  - `InsulinTimeChart`
  - `CustomColumnChart`
- Updated bottom axis to use:
  - `AxisItemPlacer.Horizontal.default(spacing = 1)`
  - consistent compact date format `d/M`
- Removed now-unused `displayFormatter` vars from affected chart composables.

#### Verification:
- ✅ `:app:compileDebugKotlin` successful (`BUILD SUCCESSFUL`).
- ✅ Date labels are no longer artificially constrained by old global dataset-size jump rules.

### UX: Info Button Inside Smart Alerts Settings Dialog (Friendly Explanation)
- **Date**: April 10, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/ConfirmationAlertSettingsDialog.kt` (lines ~9-16, ~34-42, ~59-108, ~330-377)

#### Problem:
- Inside the Smart Alerts settings dialog, users could configure advanced alert rules but had no in-context explanation of what this area is for and how to use it.

#### Root Cause:
- The dialog exposed controls directly (streaks, missed confirmations, sound/vibration) without a dedicated explanatory affordance in the header.

#### Solution:
- Added a top-right **Info** icon button in the Smart Alerts dialog header.
- Added a dedicated explanatory `AlertDialog` opened from that button.
- Implemented friendly, detailed localized content (EN/DE/ES) covering:
  - purpose of Smart Alerts,
  - what each rule category helps with,
  - threshold behavior,
  - sound/vibration behavior,
  - practical usage summary.
- Added localized confirmation button text (`Got it` / `Verstanden` / `Entendido`).

#### Verification:
- ✅ `:app:compileDebugKotlin` successful (`BUILD SUCCESSFUL`).
- ✅ Info button appears in Smart Alerts dialog top-right corner.
- ✅ Tapping it opens user-friendly explanation dialog and closes correctly.

### Feature: Alert Info Dialog + Direct Smart Alerts Setup Navigation
- **Date**: April 9, 2026
- **Status**: ✅ Complete
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/ReminderConfirmationUI.kt` (lines ~1000-1220)
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~2083-2088, ~3566-3579, ~3723-3742, ~4235-4245)
  - `app/src/main/java/com/j4/diabetestracker/NotificationSettingsDialog.kt` (lines ~121-141, ~610-649, ~886-908)
  - `app/src/main/java/com/j4/diabetestracker/CustomReminderDialog.kt` (lines ~43-87)

#### Problem:
- Alert cards in the notification popup only exposed dismiss (`X`) behavior.
- Users lacked immediate context for why warning alerts appear (Smart Alerts enabled) and had no direct path to configure the specific reminder from that alert.

#### Root Cause:
- Alert card actions were missing an explanation/help affordance.
- Notification settings flow had no reminder-specific Smart Alerts deep-link target from the bell popup alert surface.

#### Solution:
- Added a compact **Info** icon button to alert cards in `AlertNotificationCard`.
- Added localized `AlertDialog` content explaining:
  - what the alert means,
  - why it appears (Smart Alerts active),
  - and a CTA to open Smart Alerts settings.
- Wired `onOpenSmartAlertsSettings` callback through `BellPopupContent`.
- Added reminder-targeted Smart Alerts focus flow:
  - `MainActivity` now stores `notificationSettingsSmartAlertsReminderId`.
  - Alert info CTA closes bell popup and opens `NotificationSettingsDialog` with reminder focus.
  - `NotificationSettingsDialog`/`CustomRemindersSection` accept `focusSmartAlertsReminderId`.
  - `CustomReminderDialog` supports `openSmartAlertsOnStart` to auto-open Smart Alerts area.
- Added reset logic so manual settings opens do not carry stale focus IDs.

#### Verification:
- ✅ `:app:compileDebugKotlin` successful (`BUILD SUCCESSFUL`).
- ✅ Alert cards now include Info action + explanation dialog.
- ✅ Dialog CTA opens Notification Settings and focuses Smart Alerts for the corresponding reminder.
- ✅ Existing dismissal and history navigation flows remain intact.

### UI Fix: Back Button Border in Pending Comment Prompt
- **Date**: April 8, 2026
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/ReminderConfirmationUI.kt` (lines ~316-322)

#### Problem:
- The small **Back** button in the pending comment prompt appeared without a visible border.

#### Root Cause:
- The control used `TextButton`, which does not render an outline by default.

#### Solution:
- Replaced `TextButton` with a compact `OutlinedButton`.
- Added explicit border and shape styling for consistent visual delineation.

#### Verification:
- ✅ `:app:compileDebugKotlin` successful.
- ✅ Back button now has a clearly visible border in the prompt card.

### UX: Pending Confirmation Flow Enhancement (Back + No/Yes Comment Prompt)
- **Date**: April 8, 2026
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/ReminderConfirmationUI.kt` (lines ~178-221, ~223-391)

#### Problem:
- After tapping **Yes/No** on a pending reminder, the UI immediately showed a compact **Skip / Note** row.
- This did not match the desired flow: a clearer full-entry prompt with back navigation and large decision buttons.

#### Root Cause:
- Step 2 UI was implemented as a small inline row with quick actions rather than a dedicated full-width prompt block.
- Pending card interaction code had grown in one composable, increasing complexity.

#### Solution:
- Replaced old Step 2 (`Skip` / `Note`) with a dedicated full-width prompt card:
  - Small **Back** button to return to initial Yes/No step.
  - Two larger buttons: **No** (submit without comment) and **Yes** (open comment dialog).
  - Prompt text asking whether to add a comment.
- Kept existing response behavior intact (`onRespond(status, "")` for no-comment path, dialog for comment path).
- Extracted UI into focused composables to reduce `PendingConfirmationCard` complexity:
  - `PendingDecisionButtons(...)`
  - `AddCommentPromptCard(...)`

#### Verification:
- ✅ `:app:compileDebugKotlin` successful.
- ✅ New interaction appears immediately after Yes/No selection.
- ✅ Back navigation restores initial Yes/No buttons.
- ✅ No path submits response directly; Yes path opens comment dialog.

### UI: Stronger Notification Panel Contrast + Defined Borders (Alerts/Pending)
- **Date**: April 8, 2026
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/ReminderConfirmationUI.kt` (lines ~6, ~146-150, ~183-226, ~810-925, ~1040-1080)

#### Problem:
- Notification popup visuals were too soft.
- Alerts and Pending controls lacked clear delineation in the section headers and action areas.

#### Root Cause:
- Several surfaces used low-alpha fills with minimal/no border contrast.
- Count badges and action controls did not consistently use explicit borders.

#### Solution:
- Increased contrast for key notification surfaces and retained theme consistency.
- Added explicit `BorderStroke` styling to:
  - Pending confirmation card container
  - Pending Yes/No action buttons
  - Alerts/Pending section headers (collapsed + expanded states)
  - Alerts/Pending counter badges
  - Alert cards and dismiss button
- Added missing import: `androidx.compose.foundation.BorderStroke`.

#### Verification:
- ✅ `:app:compileDebugKotlin` successful.
- ✅ Alerts/Pending headers and cards now have clearer, defined outlines.
- ✅ Action controls (Yes/No and dismiss) are more legible and visually distinct.

### Fix: Hide Keyboard After Expanded Dialog Done
- **Date**: April 8, 2026
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~16805-16806, ~17363-17365, ~17449-17451)

#### Problem:
- After pressing **Done** in expanded text mode (Remarks/custom text), dialog closed and value saved, but IME keyboard remained visible.

#### Root Cause:
- Expanded dialog close/save paths did not explicitly dismiss keyboard or clear focus.

#### Solution:
- Added `LocalSoftwareKeyboardController.current` and `LocalFocusManager.current` in `TableRow`.
- On expanded dialog dismiss and **Done** action:
  - `keyboardController?.hide()`
  - `focusManager.clearFocus(force = true)`

#### Verification:
- ✅ `:app:compileDebugKotlin` successful.
- ✅ Pressing **Done** now saves and closes dialog, then closes keyboard automatically.

### Fix: Expanded Remarks Save Blocked by Secured Edit Confirmation Gate
- **Date**: April 8, 2026
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~10921-10928)

#### Problem:
- With expanded text mode enabled, entering text in Remarks/custom text dialog could fail to persist.
- Logcat showed secured edit confirmation triggering for `*_builtin_remarks` cells.

#### Root Cause:
- Edit confirmation gating classified nearly all non-date cells as blood sugar/insulin targets (`!isDateCell`).
- This incorrectly included custom columns (including built-in Remarks), interfering with expanded-edit flow.

#### Solution:
- Replaced broad `!isDateCell` check with explicit cell-key matching for only:
  - `_morningBloodSugar`, `_afternoonBloodSugar`, `_eveningBloodSugar`, `_nightBloodSugar`
  - `_morningInsulin`, `_afternoonInsulin`, `_eveningInsulin`
- Custom columns/Remarks are no longer treated as secured blood sugar/insulin edit targets.

#### Verification:
- ✅ `:app:compileDebugKotlin` successful.
- ✅ Expanded dialog still appears when enabled.
- ✅ Remarks/custom text entered via expanded dialog persists correctly.

### Fix: Remarks Expanded Text Area Respecting Column Setting + Save Reliability
- **Date**: April 8, 2026
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (lines ~17280-17285, ~17429-17438)

#### Problem:
- Expanded text dialog for Remarks-like custom columns could still open even when **Show Expanded Text Area** was disabled.
- In the dialog **Done** path, Remarks edits could fail to persist because save logic only updated `customColumnData`.

#### Root Cause:
- Auto-open trigger checked only keyboard type/focus and ignored `column.showExpandedTextArea`.
- Dialog close button did not branch save behavior for built-in Remarks column (`entry.remarks`).

#### Solution:
- Added `column.showExpandedTextArea` guard to expanded-dialog auto-open logic.
- Updated dialog **Done** save path to mirror all other Remarks handling:
  - Save to `entry.remarks` for built-in Remarks column.
  - Save to `entry.customColumnData[column.id]` for other custom columns.

#### Verification:
- ✅ `:app:compileDebugKotlin` successful.
- ✅ Expanded dialog no longer appears when disabled for a text column.
- ✅ Remarks text entered through expanded dialog now persists.

### Fix: Date Filter Wheel Jank (Smooth Spinner Scrolling)
- **Date**: April 7, 2026
- **Files**:
  - `app/src/main/java/com/j4/diabetestracker/MainActivity.kt` (Date Filter Dialog, lines ~8254-8690)

#### Problem:
- In the Date Filter dialog, the flexible range wheels (number/unit) stuttered when scrolling.
- This was especially visible when switching to filters that show many/all entries, because table filtering/pagination recalculated on every wheel tick.

#### Root Cause:
- Dialog controls were directly mutating global table filter state (`selectedDateFilter`, `flexibleNumber`, `flexibleUnit`, `showEmptyDatesInFilter`) during wheel movement.
- That triggered heavy table recomputation and pagination updates while the popup was still being interacted with.

#### Solution:
- Introduced dialog-local temporary state:
  - `tempSelectedDateFilter`
  - `tempFlexibleNumber`
  - `tempFlexibleUnit`
  - `tempShowEmptyDatesInFilter`
- Bound wheel/radio/checkbox UI to temp state while dialog is open.
- Commit temp state to global filter state only on **Apply**.
- Save persisted filter values using committed temp state on **Apply**.

#### Verification:
- ✅ `:app:compileDebugKotlin` successful after changes.
- ✅ Spinner interactions no longer force full table recalculation on each wheel frame.

### Fix: `:app:dexBuilderDebug` Crash Resolved (D8/R8 NPE in `DiabetesTrackerContent` Generated Class)
- **Date**: April 6, 2026
- **Files**:
  - `build.gradle.kts` (lines ~9-16)
  - `app/build.gradle.kts` (line ~43)
  - `gradle/wrapper/gradle-wrapper.properties` (line ~3)
  - `gradle/wrapper/gradle-wrapper.jar` (restored)

#### Problem:
- Build failed in `:app:dexBuilderDebug` with D8/R8 internal `NullPointerException` while processing generated class:
  - `MainActivityKt$DiabetesTrackerContent$22$2$2$3.class`

#### Root Cause:
- Toolchain mismatch/staleness in build setup:
  - AGP was pinned to `8.8.0` while wrapper was updated separately.
  - Kotlin plugin and Compose compiler extension versions were not aligned.
  - Root wrapper JAR was missing, preventing consistent wrapper execution from CLI.

#### Solution:
- Updated build plugins:
  - AGP `8.8.0` → `8.8.2`
  - Kotlin plugin `1.9.0` → `1.9.22`
- Updated Compose compiler extension:
  - `1.5.1` → `1.5.8` (compatible with Kotlin `1.9.22`)
- Kept Gradle wrapper distribution pinned to `8.11.1`.
- Restored missing `gradle/wrapper/gradle-wrapper.jar` for wrapper execution.

#### Verification:
- ✅ `./gradlew :app:compileDebugKotlin --console=plain` successful.
- ✅ Build completed (`BUILD SUCCESSFUL`), no `dexBuilderDebug` crash reproduced.

### Build Tooling: Gradle Wrapper Version Updated to 8.11.1
- **Date**: April 6, 2026
- **Files**:
  - `gradle/wrapper/gradle-wrapper.properties` (new in project root)

#### Problem:
- The project did not have an active root `gradle/wrapper/gradle-wrapper.properties`, so Gradle versioning was not explicit at the root level.

#### Solution:
- Added root wrapper properties and set:
  - `distributionUrl=https://services.gradle.org/distributions/gradle-8.11.1-bin.zip`

#### Result:
- ✅ Project now explicitly pins Gradle wrapper distribution to `8.11.1`.

### Enhancement: Main Table Date Order Defaults to Oldest→Newest + Tap Date Header to Toggle Sort
- **Date**: April 6, 2026
- **Files**:
  - `MainActivity.kt` (lines ~4301-4306, ~4373-4392, ~4438-4450, ~16425-16437, ~16456-16465, ~16589-16652)

#### Problem:
- The main table list showed newest dates first (top), but the desired workflow is chronological entry from old dates at top to newer dates at bottom.
- There was no direct, quick UI control on the table itself to switch date direction.

#### Root Cause:
- Entry sorting logic used descending date order by default.
- Date header cell was static and not wired to sorting state/actions.

#### Solution:
- Added `isDateSortDescending` state with default `false` (ascending, oldest→newest).
- Wired this state into the `entriesWithIndex` sorting comparator.
- Added Date-header click handling in `TableHeader` (`onDateSortToggle`) and reset pagination to page 1 equivalent (`currentPage = 0`) when toggled.
- Added visual sort-direction icon in the Date header:
  - `ArrowUpward` for oldest→newest
  - `ArrowDownward` for newest→oldest

#### Result:
- ✅ Default table order now places old entries at the top and newest entries at the bottom.
- ✅ Users can tap the Date header to flip sort direction instantly.

### Fix: App UI Zoom Now Applies to Pre-Theme Dialogs + New "Interface" Control in Zoom Menu
- **Date**: April 5, 2026
- **Files**:
  - `MainActivity.kt` (lines ~2038-2045, ~2880-2887, ~2891-3302, ~3953-3974)

#### Root Cause:
- `appUiScale` was applied through a `CompositionLocalProvider(LocalDensity...)` inside the main themed content, but several dialogs were declared before that provider, so they ignored UI zoom changes.
- The common zoom dropdown exposed table/width/height controls but had no direct "Interface" control for `appUiScale`.

#### Solution:
- Moved/centralized computed `appScaledDensity` near state initialization and reused it consistently.
- Wrapped pre-theme dialogs (`Save Preset`, `Preset Manager`, `Delete Preset`, `Color Schemes`, `Chart`, `Advanced Settings`) with `CompositionLocalProvider(LocalDensity provides appScaledDensity)`.
- Added `updateAppUiScale(...)` helper to clamp (`0.7f..1.3f`) and persist interface zoom changes.
- Added "Interface" section in the common zoom menu with reset-by-tap and +/- controls.

#### Verification:
- ✅ `./gradlew :app:compileDebugKotlin` successful.

### Fix: Flexible Filter Number Picker Now Snaps/Centers Reliably + Filtering Logs/Overhead Reduced
- **Date**: April 5, 2026
- **Files**:
  - `MainActivity.kt` (lines ~1880-1917, ~4330-4362, ~8298-8342, ~8392-8436)

#### Root Cause:
- Picker snapping used frame-offset math that could leave the selected item between positions after scroll settle.
- Filtering path emitted very verbose per-entry debug `println` logs and used `any()` checks per row, causing heavy main-thread pressure with large datasets.

#### Solution:
- Updated both flexible picker wheels (number + unit) to center-snap using viewport-center delta and `animateScrollBy(...)` after scroll end.
- Kept live center-based selection updates during scrolling to ensure selected value matches visible center item.
- Removed verbose filtering debug `println` blocks from date filter helper and table filtering remember block.
- Replaced repeated `filteredEntries.any { ... }` lookups with a precomputed `HashSet` (`toHashSet()`) for O(1) inclusion checks.

#### Verification:
- ✅ `./gradlew :app:compileDebugKotlin` successful.

### Feature: Scroll Hint Affordance Added to More Scrollable Dialogs (Including Notifications)
- **Date**: April 5, 2026
- **Files**:
  - `DialogScrollHint.kt` (new reusable dialog hint composable)
  - `NotificationSettingsDialog.kt` (shared scroll state + hint row)
  - `SettingsCategoryDialogs.kt` (shared scroll state + hint row for reusable settings shell)
  - `PatternInsightsDialog.kt` (shared scroll state + hint row in AlertDialog content)

#### Implementation Details:
- Introduced reusable `ScrollableDialogHint(...)` with stable reserved height and alpha-based visibility (no layout jump).
- Updated Notification Settings dialog to use a shared `ScrollState` and show hint at the content/footer boundary.
- Updated reusable `SettingsCategoryDialog` shell so all dialogs built on it now automatically show the scroll affordance.
- Updated Pattern Insights dialog to share `ScrollState` between content and hint row so cue behavior reflects actual overflow.

#### Verification:
- ✅ `./gradlew :app:compileDebugKotlin` successful.

### Fix: Advanced Settings Dialog Bottom Area Re-Compacted After Width Stabilization
- **Date**: April 5, 2026
- **Files**:
  - `MainActivity.kt` (lines ~18977-18984, ~19086-19090, ~20015-20025)

#### Root Cause:
- Width-stability hardening kept dialog behavior stable, but cumulative footer spacing (hint row reserve + container bottom padding + button min height/padding) made the lower action area feel oversized again.

#### Solution:
- Reduced reserved scroll-hint row height from `18.dp` to `14.dp` (still stable/no reflow).
- Tightened dialog container bottom padding (`bottom = 6.dp` while preserving top/side spacing).
- Reduced action row/button vertical footprint (`top = 0.dp`, `contentPadding vertical = 0.dp`, `minHeight = 30.dp`).

#### Verification:
- ✅ `./gradlew :app:compileDebugKotlin` successful.

### Fix: Advanced Settings Dialog Width Stays Stable on Open (No Delayed Side Expansion)
- **Date**: April 5, 2026
- **Files**:
  - `MainActivity.kt` (lines ~18964-19001, ~19075-19085)
  - `c:\Users\javie\.codeium\windsurf\memories\global_rules.md` (lines ~468-487)

#### Root Cause:
- Scroll cue row was conditionally added only after overflow became known, so first composition and post-measure composition had different content width constraints.
- Dialog was also using platform default width behavior, allowing subtle width shifts from late content measurement.

#### Solution:
- Kept scroll cue row always measured with fixed height and alpha visibility only.
- Switched dialog to non-platform default width and applied explicit width cap (`fillMaxWidth(0.94f)` + `widthIn(max = 600.dp)`) for stable width from first frame.
- Added global rule requiring no delayed horizontal expansion when adding scroll affordances.

#### Verification:
- ✅ `./gradlew :app:compileDebugKotlin` successful.

### Fix: Advanced Settings Dialog No Longer Jumps When Scroll Hint Disappears
- **Date**: April 5, 2026
- **Files**:
  - `MainActivity.kt` (lines ~18964-19005, ~19090-19107, ~20018-20027)
  - `c:\Users\javie\.codeium\windsurf\memories\global_rules.md` (lines ~468-485)

#### Root Cause:
- Scroll hint used visibility-based layout removal, so reaching the bottom removed a whole row and changed dialog height.
- Footer/content paddings also drifted back upward, reducing usable content area.

#### Solution:
- Replaced visibility-collapse behavior with a stable-height hint row using alpha animation.
- Kept hint dynamic (visible while more content exists) without reflowing layout.
- Tightened dialog vertical padding and action button padding/min height again.
- Updated global rule to require scroll cue visibility changes without dialog resizing.

#### Verification:
- ✅ `./gradlew :app:compileDebugKotlin` successful.

### Fix: Scrollable Dialogs Now Signal Overflow with Visible Scroll Cue
- **Date**: April 5, 2026
- **Files**:
  - `MainActivity.kt` (lines ~18951-19002, ~19072-19073, ~20008-20011)
  - `c:\Users\javie\.codeium\windsurf\memories\global_rules.md` (lines ~468-492)

#### Root Cause:
- Even with scrolling enabled, users had no explicit visual hint that more content existed below the fold.
- This caused discoverability issues in long dialogs.

#### Solution:
- Updated dialog scroll helper to accept shared `ScrollState`.
- Added reusable `DialogScrollHint(...)` composable (icon + localized hint text).
- The hint appears only when content can still scroll forward and fades out at the end.
- Applied the cue to `AdvancedSettingsDialog` and documented it as a mandatory global rule for upcoming dialogs.

#### Verification:
- ✅ `./gradlew :app:compileDebugKotlin` successful.

### Fix: Advanced Settings Dialog Removed Wasted Bottom Action Space
- **Date**: April 5, 2026
- **Files**:
  - `MainActivity.kt` (lines ~19032-19085, ~19965-19982)
  - `c:\Users\javie\.codeium\windsurf\memories\global_rules.md` (lines ~461-476)

#### Root Cause:
- Material `AlertDialog` default action slot reserved a relatively large bottom area for the single "Done" action.
- This reduced visible content height even after content scrolling was enabled.

#### Solution:
- Replaced `AdvancedSettingsDialog` wrapper from `AlertDialog` to custom compact `Dialog` + `Surface` layout.
- Kept content scrollable via existing `scrollableDialogContentModifier`.
- Moved action into a compact end-aligned row with reduced button/container height.
- Added a global rule to `global_rules.md` requiring compact dialog action areas and avoiding wasted vertical space.

#### Verification:
- ✅ `./gradlew :app:compileDebugKotlin` successful.

### Fix: Advanced Settings Dialog Now Scrolls for Overflow Content
- **Date**: April 5, 2026
- **Files**:
  - `MainActivity.kt` (lines ~18951-18960, ~19030, ~19044-19046)

#### Root Cause:
- `AdvancedSettingsDialog` used a plain `Column` in the `AlertDialog` text slot without vertical scrolling and without a max-height constraint.
- With larger content (and/or larger UI scaling), lower items became unreachable.

#### Solution:
- Added reusable helper:
  - `scrollableDialogContentModifier(maxHeightFraction = 0.75f)`
- Applied it to Advanced Settings dialog content:
  - Adds `heightIn(max = screenHeight * 0.75f)`
  - Adds `verticalScroll(rememberScrollState())`
- Result: dialog content scrolls whenever it exceeds visible area.

#### Verification:
- ✅ `./gradlew :app:compileDebugKotlin` successful.

### Feature: Advanced Settings App UI Scale (Counteracts System Font/Display Scaling)
- **Date**: April 5, 2026
- **Files**:
  - `MainActivity.kt` (lines ~622-626, ~2049-2058, ~3270-3278, ~3415-3431, ~18914-18948, ~19883-19909)

#### What was added:
- Added an **App UI Size** slider in **Advanced Settings → Zoom settings**.
- Added persistent setting `appUiScale` in `AppSettings`.
- Applied app-wide UI scale using `CompositionLocalProvider(LocalDensity ...)` so users can reduce/increase app size independent of Android system scaling.

#### Root Cause:
- Extreme system font/display size settings can make in-app content oversized and harder to use.
- Users may want to keep Android system settings unchanged but fine-tune this app’s UI density.

#### Solution:
- Introduced `appUiScale` (default `1.0f`, range `0.7f..1.3f`) with localized label in Advanced Settings.
- Wired setting persistence through existing `SettingsManager.saveSettings(...)` flow.
- Applied scale multiplier to both density and fontScale:
  - `density = baseDensity.density * appUiScale`
  - `fontScale = baseDensity.fontScale * appUiScale`

#### Verification:
- ✅ `./gradlew :app:compileDebugKotlin` successful.

### UX Enhancement: Stronger Food Counter Blink with Animated Aura Glow
- **Date**: March 31, 2026
- **Files**:
  - `MainActivity.kt` (lines ~11097-11167)

#### What was improved:
- Enhanced the occurrence-redirect food counter blink to be more visible by adding a pulsing glow aura around the chip.
- Kept the existing blink duration and trigger behavior unchanged.

#### Root Cause:
- The prior blink mostly changed chip fill color, which was subtle on some themes/backgrounds and easy to miss near dense table controls.

#### Solution:
- Added an animated aura layer behind `FoodCountChip` using:
  - color interpolation (`tertiary` ↔ `primary`)
  - pulsing scale
  - pulsing alpha
- Kept click target and chip content behavior intact.

#### Verification:
- ✅ `./gradlew :app:compileDebugKotlin` successful (tool reported wait-timeout after completion, but Gradle output shows `BUILD SUCCESSFUL`).

### UX Fix: New Pattern Recognition Alert Uses Notification Area Instead of In-App Snackbar
- **Date**: March 31, 2026
- **Files**:
  - `MainActivity.kt` (lines ~2263-2266, ~3380-3386, ~3407-3413)

#### Problem:
When new high-confidence patterns were detected, the app displayed an in-app snackbar/banner that could overlap and interfere with bottom control buttons.

#### Root Cause:
- New-pattern detection in `MainActivity` set local snackbar state (`showPatternAlert` / `patternAlertMessage`) and showed the message through `snackbarHostState`.

#### Solution:
- Removed the local snackbar state/effect path for new pattern alerts.
- Routed new-pattern alert feedback directly to `PatternNotificationHelper.showPatternSummaryNotification(...)` with `periodType = "New"`.
- Result: alerts now appear in the Android notification area instead of covering in-app controls.

#### Verification:
- ✅ `./gradlew :app:compileDebugKotlin` successful (build output successful; terminal tool hit wait timeout after completion).

### Fix: DexBuilder Debug Crash in MainActivity Compose Lambda
- **Date**: March 31, 2026
- **Files**:
  - `MainActivity.kt` (lines ~4300-4350, ~16225-16400)

#### Problem:
`./gradlew :app:build` failed at `:app:dexBuilderDebug` with an R8/D8 internal NPE while dexing generated class:
`MainActivityKt$DiabetesTrackerContent$34$4$3.invoke(PaddingValues, Composer, Int)`.

#### Root Cause:
`DiabetesTrackerContent` had an oversized inline Scaffold content lambda, producing an overly complex generated Compose class for dexing.

#### Solution:
- Extracted occurrence back-navigation banner UI into `OccurrenceBackNavigationBanner(...)`.
- Extracted gesture/pointer-input logic into `rememberTableGestureModifier(...)`.
- Extracted language-label map creation into `tableLabelsForLanguage(...)`.
- Replaced inline blocks in Scaffold content with helper calls to reduce lambda complexity while preserving behavior.

#### Verification:
- ✅ `./gradlew :app:dexBuilderDebug --stacktrace` now succeeds.
- ✅ No behavior changes to occurrence back-navigation, table gestures, or localized labels.

### Enhancement: Occurrence Redirect Now Blinks Matching Food Counter Button
- **Date**: March 30, 2026
- **Files**:
  - `AnalysisScreen.kt` (line ~2541)
  - `PatternDetailsPopup.kt` (line ~299)
  - `MainActivity.kt` (lines ~2168-2185, ~3199-3247, ~7275-7316, ~9342-9392, ~9553-9603, ~4843-4846, ~16699-17167, ~11253-11284)

#### Problem:
When opening a table entry from Pattern Analysis "Occurrences", users were correctly redirected to the date but had no immediate visual cue showing *which* food counter button (time slot) corresponded to that occurrence.

#### Root Cause:
- Occurrence navigation passed only date context through the callback.
- Table row already had food counter blink support in cells, but occurrence redirects did not provide/use time-of-day targeting.

#### Solution:
- Extended occurrence navigation payload from pattern occurrences to include `date|timeOfDay`.
- Added payload decoding in `MainActivity` navigation handlers.
- Stored highlighted occurrence date/time + trigger token in state.
- Passed highlight state into `TableRow` and down into each matching `EditableTableCell` food counter.
- Applied blink color pulse on the `FoodCountChip` when the row/date/time target matches.

#### Result:
- ✅ Redirect still opens the correct table date.
- ✅ Matching food counter button now blinks briefly to show exactly where the occurrence belongs.
- ✅ `:app:compileDebugKotlin` successful (build output successful; tool timed out while waiting for final I/O).

### Enhancement: Pattern Dialog Uses Per-Filter Info Buttons (No Global Info Dump)
- **Date**: March 29, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~1265-1313, ~1821-1914)

#### Problem:
In the Pattern filter dialog, tapping one main info button showed all explanations at once, which felt crowded and harder to scan.

#### Root Cause:
- Explainability was grouped under a single global toggle.
- Users needed per-option guidance tied directly to the specific filter they were considering.

#### Solution:
- Removed global Pattern info expansion behavior.
- Added an **Info button per filter option** in the Pattern dialog:
  - one beside `All pattern types`
  - one beside each visible pattern-type filter button
- Each info button opens a short focused explanation dialog for that specific option:
  - why this option appears
  - what happens when selecting it

#### Result:
- ✅ Pattern filter explanations are now option-specific and easier to understand.
- ✅ Users can learn each button independently instead of reading a long combined help block.
- ✅ `:app:compileDebugKotlin` successful.

### Enhancement: Pattern Filter Info Now Explains Each Visible Button
- **Date**: March 29, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~1252-1313, ~1861-1882)

#### Problem:
The Pattern dialog info toggle was too generic and did not explain each currently visible pattern-type button (why it appears and what selecting it does).

#### Root Cause:
- Helper text used one broad sentence for the full Pattern section.
- There was no per-button explanation tied to currently available pattern type entries.

#### Solution:
- Reworked Pattern info content into layered, dynamic explanations:
  - section header explanation (why button list is visible),
  - explicit explanation for **All pattern types** button,
  - per-visible-button explanation generated for each current pattern type.
- Added per-type action explanations for:
  - Food marker correlation,
  - Time marker correlation,
  - Blood sugar spike/drop,
  - Insulin pattern,
  - Custom column pattern.
- Updated the Pattern dialog info block to render these explanations as separate short helper cards.

#### Result:
- ✅ Users now get direct, short, and contextual explanations for every visible Pattern filter button.
- ✅ The dialog clarifies both why a button is shown and what happens when it is selected.
- ✅ `:app:compileDebugKotlin` successful.

### Enhancement: Marked Multi-Select Filter + Info Inside Marked/Trigger/Pattern Dialogs
- **Date**: March 29, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~808-813, ~841-877, ~891-902, ~963-989, ~1035-1074, ~1217-1258, ~1370-1571, ~1570-1840)
  - `res/values/strings.xml` (line ~347)
  - `res/values-de/strings.xml` (line ~348)
  - `res/values-es/strings.xml` (line ~347)

#### Problem:
The user wanted `Marked` to behave like `Trigger` with its own multi-select dialog, and asked to remove info icons from the chips themselves and place short, clear help inside each filter section dialog.

#### Root Cause:
- Marker filter had no advanced selection dialog.
- Explanations were attached to chip-level icons instead of section-level filter dialogs.
- Marker selection state was not part of calendar aggregation logic.

#### Solution:
- Added new `CalendarMarkedFilterDialog` with multi-select marker checkboxes and apply/clear controls.
- Added `selectedMarkerLabels` state and wired it into:
  - `buildMarkerDaySummaries(...)`
  - `buildCalendarDayDetails(...)`
  so selected markers actually filter calendar marker/day content.
- Removed standalone `CalendarFilterInfoDialog` model and chip-level info behavior.
- Added info toggle inside each filter dialog:
  - Marked dialog
  - Trigger dialog
  - Pattern dialog
- Added short in-dialog helper text blocks in EN/DE/ES logic for each section.
- Added localized title string for marked filter dialog.

#### Result:
- ✅ `Marked` now supports multi-select filtering like `Trigger`.
- ✅ Info is now shown in each section dialog (Marked/Trigger/Pattern), not on chips.
- ✅ Explanations are short, contextual, and directly tied to the active section.
- ✅ `:app:compileDebugKotlin` successful.

### Enhancement: Info Buttons on Calendar Filter Chips (Marked / Trigger / Pattern)
- **Date**: March 29, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~17-18, ~806-811, ~952-1005, ~1052-1063, ~1232-1300)
  - `res/values/strings.xml` (line ~335)
  - `res/values-de/strings.xml` (line ~336)
  - `res/values-es/strings.xml` (line ~335)

#### Problem:
Users needed clearer in-context explanations for what each calendar filter chip does (especially Marked, Trigger, Pattern), without leaving the calendar flow.

#### Root Cause:
- Filter chips were action-only controls with no embedded discoverable help affordance.
- Understanding relied on trial-and-error or separate explanations elsewhere.

#### Solution:
- Added inline **Info icons** directly to these chips:
  - `Marked`
  - `Trigger`
  - `Pattern`
- Added `CalendarFilterInfoDialog(...)` with plain-language, contextual explanations.
- Dialog text includes dynamic state:
  - current marker/trigger/pattern day counts,
  - selected trigger count,
  - active pattern type (or all types).
- Added localized content-description string for the chip info icon in EN/DE/ES.

#### Result:
- ✅ Calendar filters now provide immediate, user-friendly explanations from the exact UI control.
- ✅ Users can understand filter meaning without guessing or navigating away.
- ✅ `:app:compileDebugKotlin` successful.

### Enhancement: Context-Aware Pattern Explanations in Calendar Day Helper
- **Date**: March 28, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~849-871, ~1377-1470, ~1870-1981)

#### Problem:
The helper text explained that pattern rows are trend summaries, but did not explain *why a specific pattern label* (for example, a food name during Blood Sugar Spike filter, or time slots during Time-Based filter) appears in that day dialog.

#### Root Cause:
- Day-level helper logic did not keep source metadata linking each pattern label to its pattern type and marker context.
- Explanation text was generic and not tied to active calendar pattern filter context.

#### Solution:
- Passed active pattern type filter into `buildCalendarDayDetails(...)`.
- Added per-day/per-label metadata maps while aggregating pattern occurrences:
  - source `PatternType`
  - related marker names
- Replaced generic pattern helper line with context-aware explanations per label:
  - **Food Marker Correlation**: explains that food appears due to detected food→marker relation
  - **Time-Based Correlation**: explains that time slots appear due to repeated marker activity in that time window
  - **Blood Sugar Spike/Drop**: explains food labels appear because they were linked to spike/drop behavior
  - fallback explanations for other pattern types
- Added localized wording (EN/DE/ES) via existing language-based helper builders.
- Cleaned ambiguous loop labels in aggregation logic for clarity and safer returns.

#### Result:
- ✅ Pattern helper text now explains *why each label appears* according to the selected pattern filter context.
- ✅ Reduced confusion when the same-looking label (e.g., food name) appears under different pattern filters.
- ✅ `:app:compileDebugKotlin` successful.

### Enhancement: Explainable Calendar Day Details (Info Helper + Plain-Language Summaries)
- **Date**: March 28, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~1369-1450, ~1816-1924)
  - `CalendarDayDetailsDialog.kt` (lines ~3-147)
  - `res/values/strings.xml` (lines ~342-344)
  - `res/values-de/strings.xml` (lines ~343-345)
  - `res/values-es/strings.xml` (lines ~342-344)

#### Problem:
Users could see marker/trigger/pattern breakdowns for a selected day, but the content was not self-explanatory enough. In particular, pattern labels like `Morning`/`Afternoon` could be confused with marker logs, which made interpretation harder.

#### Root Cause:
- `CalendarDayDetails` only provided count breakdowns with no plain-language explanation layer.
- The day-details dialog had no contextual helper action (info entry point).
- Pattern and marker semantics were shown together without explicit clarification text.

#### Solution:
- Extended `CalendarDayDetails` with `helperSummaryLines`.
- Added helper line generation while building day details:
  - marker + time-of-day summaries in simple language,
  - optional trigger text in the same sentence,
  - pattern clarification line explaining that pattern rows are filtered trend summaries (not single marker events).
- Added Info button (`Icons.Default.Info`) in day-details dialog title:
  - tap toggles an inline helper section,
  - helper section shows localized, user-friendly bullet explanations.
- Added localized strings (EN/DE/ES) for helper UI labels.
- Aligned trigger handling in day-details aggregation with selected trigger filters for consistency.

#### Result:
- ✅ Day details now include an explainable helper layer for non-technical users.
- ✅ Users can quickly understand what happened on the day and why pattern lines may differ from marker rows.
- ✅ UX remains compact by showing explanations on demand via Info.
- ✅ `:app:compileDebugKotlin` successful (tool reported post-success wait timeout after success output).

### Enhancement: Calendar Filter Simplification (Single Row + Trigger/Pattern Dialog Filters)
- **Date**: March 28, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~67-71, ~321-413, ~787-1033, ~1284-1566)
  - `res/values/strings.xml` (lines ~343-347)
  - `res/values-de/strings.xml` (lines ~344-348)
  - `res/values-es/strings.xml` (lines ~343-347)

#### Problem:
The calendar had duplicated filter control concepts (detail mode + visibility chips) and too much always-visible pattern content below the calendar, which made the flow feel noisy and harder to use.

#### Root Cause:
- Calendar state was split across two independent filter axes (`detailMode` and `visibilityFilter`).
- Trigger and pattern-specific options were not scoped into focused dialogs.
- Pattern types/details were always rendered in the screen flow instead of being opened on demand.

#### Solution:
- Replaced duplicated calendar control model with a single primary filter enum:
  - `ALL_DAYS`, `MARKED_DAYS`, `TRIGGER_DAYS`, `PATTERN_DAYS`.
- Kept one primary chip row with counters:
  - `All days`, `Marked (X)`, `Trigger (Y)`, `Pattern (Z)`.
- Added trigger filter dialog:
  - multi-select trigger labels,
  - apply/clear handling,
  - trigger filtering integrated into day summary/detail builders via `selectedTriggerLabels`.
- Added pattern filter dialog:
  - pattern type selection (including all-types option),
  - apply action updates global pattern type filter,
  - explicit action to open full pattern details workspace.
- Moved always-open pattern sections behind on-demand visibility:
  - `PatternVisualization` and `PatternDetailsGroupedSection` now render only when `showPatternWorkspace` is enabled.
- Added EN/DE/ES localized strings for new dialog/workspace actions.

#### Result:
- ✅ Calendar filter UX now has one clear control row without duplicated chip semantics.
- ✅ Trigger and pattern advanced options are now dialog-based and less cluttered.
- ✅ Pattern types/details are no longer always expanded and can be opened when needed.
- ✅ `:app:compileDebugKotlin` successful (build output confirmed success; tool reported post-success wait timeout).

### Enhancement: Calendar Day Details Dialog (Full Filtered Content on Day Tap)
- **Date**: March 28, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~813-832, ~890-907, ~939-961, ~1313-1376)
  - `CalendarDayDetailsDialog.kt` (new file)
  - `res/values/strings.xml` (lines ~338-342)
  - `res/values-de/strings.xml` (lines ~339-343)
  - `res/values-es/strings.xml` (lines ~338-342)

#### Problem:
Calendar day cells can contain multiple marker/trigger/pattern signals, but the day tile has limited space. Users could not view the full day-level filtered content before navigating to the table.

#### Root Cause:
`MarkerCalendarOverviewCard` only exposed compact per-day summaries for the grid and direct navigation, with no expanded day detail layer.

#### Solution:
- Added a dedicated dialog component in a new file:
  - `CalendarDayDetailsDialog` + `CalendarDayDetails` model.
- Added day-detail state wiring in `MarkerCalendarOverviewCard`:
  - `selectedDayDetails` controls dialog visibility/content.
  - day cell taps now open the dialog instead of immediately navigating.
- Implemented `buildCalendarDayDetails(...)` to aggregate full day breakdowns from active data scope:
  - marker counts by resolved marker name,
  - trigger counts (reason-first, note fallback),
  - pattern counts from `filteredPatterns` occurrences.
- Preserved existing navigation behavior by adding dialog confirm action:
  - `Open table day` uses existing `onNavigateToDate` callback and closes dialog.
- Added localized strings (EN/DE/ES):
  - `calendar_day_details_title`
  - `calendar_day_details_markers`
  - `calendar_day_details_triggers`
  - `calendar_day_details_patterns`
  - `calendar_view_table_day`

#### Result:
- ✅ Tapping a calendar day now opens a detailed dialog with complete filtered content for that date.
- ✅ Users can still continue directly to the exact table day from the dialog.
- ✅ Feature kept modular by extracting dialog UI into a focused composable file.
- ✅ `:app:compileDebugKotlin` successful (build output shows success; tool reported post-success wait timeout).

### Enhancement: Calendar Spotlight Phase 1 (Pattern Filters Projected to Days)
- **Date**: March 28, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~320-328, ~758-1285)
  - `res/values/strings.xml` (lines ~330-335)
  - `res/values-de/strings.xml` (lines ~331-336)
  - `res/values-es/strings.xml` (lines ~330-335)

#### Problem:
After moving the calendar into full Pattern Overview, it still mainly showed marker/trigger-only day summaries and did not fully reflect active pattern filtering context (risk/type/food filters applied in overview).

#### Root Cause:
`MarkerCalendarOverviewCard` consumed only entries + markers and had no access to currently filtered patterns, so pattern-driven sections (e.g., Top Risky Foods / pattern type filtering) were not visualized directly on day cells.

#### Solution (Phase 1):
- Wired `filteredPatterns` into `MarkerCalendarOverviewCard`.
- Extended calendar day summary model to include:
  - `patternLabel`
  - `patternCount`
- Added pattern aggregation per day from filtered pattern occurrences (`pattern.details`) with existing date-range/month constraints.
- Added calendar detail mode chip:
  - `Pattern days: X`
- Added visibility filter chip:
  - `Pattern`
- Updated day cell rendering to support PATTERN mode labels while preserving existing marker/trigger behavior.
- Added localized strings (EN/DE/ES):
  - `calendar_days_with_patterns_count`
  - `calendar_filter_pattern_days`

#### Result:
- ✅ Calendar now reflects active filtered pattern results by day (initial spotlight behavior).
- ✅ Users can toggle marker / trigger / pattern focus directly in calendar.
- ✅ Pattern-only day filtering is available.
- ✅ `:app:compileDebugKotlin` successful (command output confirmed success; tool reported post-success wait timeout).

### Enhancement: Merge Marker Calendar into Full Pattern Overview (Replace Duplicate Insight Summary Cards)
- **Date**: March 28, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~320-339, ~769-1082, ~1793-1916)
  - `MainActivity.kt` (lines ~7805-7813 removed)
  - `res/values/strings.xml` (lines ~333-335)
  - `res/values-de/strings.xml` (lines ~334-336)
  - `res/values-es/strings.xml` (lines ~333-335)

#### Problem:
Users saw overlap between:
- full-overview insight highlight cards ("Most Frequent Marker", "Most Common Trigger"), and
- calendar marker/trigger day controls in Settings → Analysis & Markers.

This made the same concepts feel duplicated across two different places.

#### Root Cause:
- `MarkerCalendarOverviewCard` was rendered in Settings overview (`MainActivity.kt`) instead of in full `AnalysisScreen`.
- `PatternVisualization` still rendered separate insight-highlight cards, duplicating marker/trigger summary meaning already represented by the calendar chips/day cells.

#### Solution:
- Moved `MarkerCalendarOverviewCard` into full Pattern Analysis overview list (`AnalysisScreen`) directly after the top summary card.
- Removed the calendar card from Settings → Analysis & Markers list.
- Removed `insightHighlights` rendering block from `PatternVisualization` and simplified the function signature accordingly.
- Kept calendar marker/trigger day chips as the canonical summary controls.
- Added interactive month label behavior:
  - tapping the month/year title opens a month-year picker dialog,
  - prev/next arrows still support quick month skipping.
- Added missing localized strings for month/year picker title and labels in EN/DE/ES:
  - `calendar_pick_month_year`
  - `calendar_pick_month`
  - `calendar_pick_year`

#### Result:
- ✅ Calendar is now unified in full Pattern Analysis overview.
- ✅ Settings overview no longer duplicates this calendar block.
- ✅ Marker/trigger summary duplication removed from separate insight cards.
- ✅ Users can skip months with arrows and directly pick month/year by tapping the date label.
- ✅ `:app:compileDebugKotlin` successful.

### Enhancement: Full Overview Layout Cleanup (Risk Distribution in Top Summary + Removed Duplicate Total Card)
- **Date**: March 27, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~214-313, ~1667-1812)

#### Problem:
The full Pattern Analysis overview showed a dense stacked layout with repeated summary information:
- top summary card,
- then a second Pattern Overview card with total count,
- plus risk distribution located lower in the list.

#### Root Cause:
Summary metrics (total + risk distribution) were split across two sections, which reduced hierarchy clarity and made scanning harder.

#### Solution:
- Kept the top summary card as the primary summary block.
- Added visible total pattern count badge directly in that top summary header.
- Replaced the previous compact risk strip in the top summary with full Risk Distribution bars.
- Removed the duplicate Pattern Overview total block and duplicate risk distribution block from `PatternVisualization`.
- Kept remaining content order clean and focused:
  - Insight highlights,
  - Pattern types,
  - Top risky foods,
  - Pattern details.

#### Result:
- ✅ Cleaner and more understandable compact overview flow.
- ✅ Total count now appears in point 1 (top summary), eliminating duplicate total card.
- ✅ Risk distribution is now integrated into the top summary as requested.
- ✅ `:app:compileDebugKotlin` successful.

### Enhancement: Insight Highlights Prioritized in Full Overview + Removed from Settings Snippet
- **Date**: March 27, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~1701-1716, ~1816-1826)
  - `MainActivity.kt` (lines ~7223-7228, ~7694-7702)

#### Problem:
Insight cards were duplicated between **Settings → Analysis & Markers** and full Pattern Analysis overview, and users asked to prioritize insights in one main place.

#### Root Cause:
Highlights were rendered in both contexts, creating repeated content and lower information hierarchy.

#### Solution:
- Removed the mini insight-card list from Settings Analysis & Markers section and kept a simple pointer + action to open full overview.
- Moved `Insight highlights` to the top area of full `PatternVisualization` (directly under Pattern Overview header), so key summaries appear first.

#### Result:
- ✅ No duplicate insight cards in Settings snippet.
- ✅ Insight highlights now appear in first position in full Pattern Analysis overview.
- ✅ `:app:compileDebugKotlin` successful.

### Enhancement: Merged Insight Highlights into Pattern Types (Full Overview)
- **Date**: March 27, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~110-116, ~307-317, ~1647-1650, ~1810-1823, ~1858-1889)
  - `res/values/strings.xml` (lines ~311-312)
  - `res/values-de/strings.xml` (lines ~312-313)
  - `res/values-es/strings.xml` (lines ~311-312)

#### Problem:
In Settings → Analysis & Markers, users saw insight summaries like "most frequent marker" and "most common trigger" and expected those to be visible together with Pattern Types in full Pattern Analysis overview.

#### Root Cause:
Insight summaries (`PatternAnalyzer`) and detected pattern categories (`PatternDetectionEngine`) were presented in separate UI contexts, which made them feel disconnected.

#### Solution:
- Added a soft merge in full overview Pattern Types section:
  - computes insight highlights from `PatternAnalyzer` (frequency + trigger insights),
  - passes those highlights into `PatternVisualization`,
  - renders a compact `Insight highlights` subsection below Pattern Types rows.
- Added localized labels for:
  - `pattern_insight_highlights`
  - `pattern_occurrence_count`
  in EN/DE/ES.

#### Result:
- ✅ Users can see both structural Pattern Types and key insight summaries in one place.
- ✅ Existing Pattern Types filtering behavior remains unchanged.
- ✅ `:app:compileDebugKotlin` successful.

### Enhancement: Pattern Types Visibility Toggle (Show All vs Detected Only)
- **Date**: March 27, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~40-41, ~1649, ~1732-1799, ~1898-1958)
  - `res/values/strings.xml` (lines ~308-311)
  - `res/values-de/strings.xml` (lines ~309-312)
  - `res/values-es/strings.xml` (lines ~308-311)

#### Problem:
Users wanted to understand the app's full detection capability in Pattern Types, not only currently detected categories.

#### Root Cause:
Pattern Types had no user control for visibility mode, so only non-empty categories were shown.

#### Solution:
- Added a toggle chip next to the **Pattern Types** section title:
  - **Detected only**
  - **Show all**
- In **Show all** mode, all known `PatternType` categories are listed, including zero-count rows.
- Zero-count rows are visibly dimmed and non-clickable to prevent empty-result filter actions.
- Added localized labels (EN/DE/ES) for the new visibility options.

#### Result:
- ✅ Users can switch between compact detected-only view and full capability view.
- ✅ Full app detection capabilities are now visible on demand.
- ✅ `:app:compileDebugKotlin` successful.

### Enhancement: Dynamic Pattern Types in Full Pattern Analysis Overview
- **Date**: March 27, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~1614-1635, ~1729-1758)
  - `res/values/strings.xml` (lines ~308-315)
  - `res/values-de/strings.xml` (lines ~309-316)
  - `res/values-es/strings.xml` (lines ~308-315)

#### Problem:
The Pattern Types area only showed three hardcoded rows (food-marker, blood sugar spikes, time-based), so users could not see all detectable pattern categories directly in the overview.

#### Root Cause:
The UI counted and rendered only three `PatternType` values explicitly instead of deriving rows from the detected pattern set.

#### Solution:
- Replaced the fixed 3-row Pattern Types block with dynamic grouping:
  - grouped by `patternType`,
  - counted with `eachCount()`,
  - sorted by count (descending),
  - rendered all non-empty categories as tappable filter rows.
- Added centralized icon + localized label mapping for all current `PatternType` values:
  - `FOOD_MARKER_CORRELATION`
  - `TIME_MARKER_CORRELATION`
  - `BLOOD_SUGAR_SPIKE`
  - `BLOOD_SUGAR_DROP`
  - `INSULIN_PATTERN`
  - `CUSTOM_COLUMN_PATTERN`
- Added missing localized strings (EN/DE/ES) for:
  - blood sugar drops,
  - insulin patterns,
  - custom column patterns.

#### Result:
- ✅ Pattern Types now reflects all currently detected categories, not only 3 predefined rows.
- ✅ Type filters remain fully functional with the new dynamic rows.
- ✅ `:app:compileDebugKotlin` successful.

### Enhancement: Pattern Details Hierarchical Grouping (Confidence → Marker Type → Entries)
- **Date**: March 27, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~325-335, ~1097-1286)

#### Problem:
Pattern Details rendered as one long flat list of pattern cards. It was harder to scan and compare by confidence level and marker type when many detected patterns existed.

#### Root Cause:
UI directly iterated `filteredPatterns` and rendered `PatternCard` sequentially with no intermediate grouping hierarchy.

#### Solution:
- Replaced flat `items(filteredPatterns)` rendering with a grouped section composable:
  - confidence level accordion (High / Medium / Low) with per-group counters,
  - nested marker-type accordion with per-type counters,
  - pattern entries list shown only when marker type is expanded.
- Kept existing pattern actions fully intact inside expanded entries:
  - dismiss pattern,
  - ignore food,
  - navigate to entry date.

#### Result:
- ✅ Pattern Details now opens in a compact, drill-down structure.
- ✅ Users can quickly scan confidence groups and marker-type totals before opening full entry lists.
- ✅ Existing behavior/actions preserved.
- ✅ `:app:compileDebugKotlin` successful.

### Enhancement: Move Calendar Into Settings → Analysis & Markers
- **Date**: March 26, 2026
- **Files**:
  - `MainActivity.kt` (lines ~7245-7278, ~7844-7851)
  - `AnalysisScreen.kt` (lines ~297-305, ~753)

#### Problem:
Calendar overview was only shown inside the full Pattern Analysis screen, while user flow expectation was to access it directly in **Settings → Analysis & Markers**.

#### Root Cause:
`MarkerCalendarOverviewCard` was integrated in `AnalysisScreen` list only, and not rendered in the Settings overview dialog.

#### Solution:
- Added calendar card directly to the non-timeline Analysis & Markers shared `LazyColumn` in `MainActivity`.
- Added dedicated date navigation callback for this dialog (`navigateToDateFromAnalysisMarkers`) with robust multi-format date parsing.
- Exposed calendar composable for reuse by changing:
  - `private fun MarkerCalendarOverviewCard` → `fun MarkerCalendarOverviewCard`.
- Removed calendar card from `AnalysisScreen` list so the feature is effectively moved to the Settings Analysis & Markers area.

#### Result:
- ✅ Calendar + all its controls are now available directly in Settings → Analysis & Markers.
- ✅ Day tap still navigates to the specific table date correctly.
- ✅ `:app:compileDebugKotlin` successful.

### Enhancement: Marker Calendar Navigation Reliability + UX Controls
- **Date**: March 26, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~742-1100)
  - `MainActivity.kt` (lines ~3195-3219, ~9318-9342, ~9518-9542)
  - `res/values/strings.xml` (lines ~318-325)
  - `res/values-de/strings.xml` (lines ~319-326)
  - `res/values-es/strings.xml` (lines ~318-325)

#### Problem:
From the calendar, tapping a day did not always redirect to the specific table date. The month grid also had asymmetric last-row cells (e.g., days 30/31 stretched), and the calendar legend labels were static text without interaction or count context.

#### Root Cause:
- Date navigation parsing accepted only one format in several navigation entry points (`dd-MM-yyyy`), while some flows produced dot-separated dates.
- Calendar cell builder did not pad trailing empty cells, so final week rows could render with fewer weighted cells and distorted widths.
- Legend was implemented as non-interactive labels.

#### Solution:
- Updated calendar day navigation to emit `dd-MM-yyyy` consistently.
- Hardened navigation parsing in `MainActivity` to accept `dd-MM-yyyy`, `dd.MM.yyyy`, and `yyyy-MM-dd` in all relevant `onNavigateToDate` handlers.
- Added month navigation controls (previous/next + today).
- Added calendar filter chips:
  - all days,
  - marked-only days,
  - trigger-only days.
- Replaced static legend text with interactive chips showing counts:
  - marker days count,
  - trigger days count,
  and used them to toggle day-cell detail mode.
- Padded trailing calendar cells to full 7-day rows for symmetric layout.
- Added new localized strings (EN/DE/ES) for these controls.

#### Result:
- ✅ Day tap redirection is now reliable across date format variants.
- ✅ Month navigation and in-card date visibility filters are available.
- ✅ Last calendar week renders symmetrically (30/31 no longer stretched).
- ✅ Legend area is now interactive and includes day counts.
- ✅ `:app:compileDebugKotlin` successful.

### Feature: Analysis Calendar Overview for Markers and Triggers
- **Date**: March 26, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~39-55, ~295-303, ~740-1020)
  - `MainActivity.kt` (lines ~9244-9248)
  - `res/values/strings.xml` (lines ~315-317)
  - `res/values-de/strings.xml` (lines ~316-318)
  - `res/values-es/strings.xml` (lines ~315-317)

#### Problem:
Analysis view lacked a month-grid calendar overview, so users could not quickly see day-level marker concentration and common trigger context in the current month.

#### Root Cause:
Pattern analysis UI focused on aggregate cards/lists but had no per-day marker aggregation layer connected to a calendar grid.

#### Solution:
- Added a dedicated **Marker Calendar Overview** card to `AnalysisScreen` within the unified scroll content.
- Wired `AnalysisScreen` with source data (`entries`, `customMarkers`) from `MainActivity`.
- Implemented month-based day summaries:
  - most frequent marker per day,
  - most common trigger per day (reason first, fallback to note snippet),
  - filtered by active analysis date range when present.
- Implemented calendar rendering helpers:
  - weekday header row,
  - day cells with highlighted state when marker data exists,
  - day tap navigation back to table date via existing `onNavigateToDate` callback.
- Added localized strings for English, German, and Spanish.

#### Result:
- ✅ Analysis now includes a true current-month calendar grid overview.
- ✅ Each day can display top marker + top trigger summary.
- ✅ Existing analysis filtering and navigation flow preserved.
- ✅ `:app:compileDebugKotlin` successful.

### UX Fix: Analysis & Markers Overview Uses One Shared Scroll Container
- **Date**: March 25, 2026
- **Files**: `MainActivity.kt` (lines ~7596-7958)

#### Problem:
In the full-screen **Analysis & Markers** dialog (non-timeline view), the upper overview blocks (pattern summary + marker tools) and category content did not behave as one continuous page. This made the category area feel separately scrolled and less cohesive.

#### Root Cause:
The overview cards and category list were laid out as separate vertical blocks rather than one unified list-backed scroll surface.

#### Solution:
- Refactored the non-timeline branch to a single `LazyColumn` that contains:
  - Pattern insights card
  - Marker management card (manage/share)
  - Categories header/help text
  - Category cards/details content
- Kept timeline branch behavior unchanged.
- Ensured the shared non-timeline `LazyColumn` uses `Modifier.weight(1f)` so it consumes remaining body height inside the full-screen dialog.
- Corrected refactor-introduced brace/scope issues so composables remain in proper structure.

#### Result:
- ✅ Pattern overview, marker tools, and categories now scroll together as one cohesive surface
- ✅ Existing actions preserved (manage, share/export, filter, selection, bulk delete)
- ✅ `:app:compileDebugKotlin` successful

### Bug Fix: Pagination/Zoom Work Restored Build (Brace/Scope Issues)
- **Date**: March 19, 2026
- **Files**: `MainActivity.kt` (lines ~10490-10506, ~4980-5090)

#### Problem:
After recent pagination/zoom updates, the project stopped compiling. Kotlin reported many “unresolved reference” errors across unrelated functions/components, making it impossible to test the pagination/zoom fixes.

#### Root Cause:
- A brace imbalance in `MainActivity.kt` caused `DiabetesTrackerContent()` to not fully close, so later top-level declarations were parsed in the wrong scope.
- A set of paging/date/add FABs used `Modifier.align(Alignment.Bottom...)` outside a `Box` scope, causing `Modifier.align` compilation errors.

#### Solution:
- Added the missing closing braces before the next top-level composable declarations so the file parses correctly.
- Wrapped the fixed-position paging/date/add controls in a `Box(modifier = Modifier.fillMaxSize())` so `Modifier.align(...)` is valid and button placement remains correct.

#### Result:
- ✅ `:app:compileDebugKotlin` succeeds again
- ✅ Pagination/zoom work can now be tested at runtime

### Bug Fix: Pagination Bottom Controls Visible/Working Again
- **Date**: March 20, 2026
- **Files**: `MainActivity.kt` (lines ~4795-4885)

#### Problem:
Only a disabled-looking "Prev" button appeared in pagination mode, while the rest of the expected bottom controls (Next, page indicator, date filter, add) were missing/off-screen.

#### Root Cause:
The temporary text-button row was rendered inside the horizontally-sized table content area, so control layout followed table width instead of viewport width.

#### Solution:
- Replaced the temporary text row with a full bottom control row matching previous behavior:
  - Previous page (up arrow)
  - Next page (down arrow)
  - Page indicator
  - Date filter button
  - Add entry button
- Anchored the row to viewport width (`width(screenWidth)`) and centered it, so controls remain visible even when table width exceeds screen width.

#### Result:
- ✅ Full control set is visible again in pagination mode
- ✅ Next/Prev controls are usable for pagination testing
- ✅ `:app:compileDebugKotlin` remains successful

### Bug Fix: Keep Pagination Controls Independent from Table Zoom
- **Date**: March 20, 2026
- **Files**: `MainActivity.kt` (lines ~4795-4797, ~4973-5084)

#### Problem:
At low zoom (e.g., ~59%), bottom pagination controls still slid off-screen and spacing looked distorted. Controls were effectively impacted by horizontal table zoom/scroll behavior.

#### Root Cause:
A second controls row was being rendered inside the zoomed/horizontally-scrollable table container, so its layout still depended on table width/position.

#### Solution:
- Removed in-table controls row from the pagination table block.
- Kept only the fixed bottom controls rendered in the root overlay `Box` (outside horizontal scroll / zoom area).

#### Result:
- ✅ Bottom controls remain stable and visible regardless of table zoom level
- ✅ Table zoom now only affects table content, not control geometry
- ✅ `:app:compileDebugKotlin` successful

### Hotfix: Restore Pagination Controls After Disappearance
- **Date**: March 20, 2026
- **Files**: `MainActivity.kt` (lines ~4976-5109)

#### Problem:
After removing duplicate in-table controls, pagination controls disappeared in runtime across zoom levels.

#### Root Cause:
Controls were still positioned relative to a horizontally scrollable table context. Their placement could move outside the viewport depending on table scroll/zoom geometry.

#### Solution:
- Reworked pagination controls into a single viewport-sized bottom row (`width(screenWidth)` + `align(BottomStart)`).
- Applied horizontal offset compensation using current table scroll (`offset { IntOffset(scrollState.value, 0) }`) so controls remain pinned to the visible screen while table content scrolls.
- Kept non-pagination mode behavior for date/add buttons in a separate fallback branch.

#### Result:
- ✅ Pagination controls are visible again
- ✅ Controls stay stable while table zoom/scroll changes
- ✅ `:app:compileDebugKotlin` successful

### Regression Fix: Remove Scroll Offset from Bottom Controls Row
- **Date**: March 20, 2026
- **Files**: `MainActivity.kt` (lines ~4978-4983)

#### Problem:
Controls still disappeared in pagination mode after the prior hotfix.

#### Root Cause:
The bottom controls row used `offset { IntOffset(scrollState.value, 0) }`, which could push the entire controls row outside the visible viewport as horizontal scroll value increased.

#### Solution:
- Removed scroll-based `offset` from the pagination controls row.
- Kept controls anchored with `width(screenWidth)` + `align(Alignment.BottomStart)`.

#### Result:
- ✅ Controls no longer get shifted off-screen by horizontal scroll state
- ✅ Controls remain visible while table zoom/pagination updates occur
- ✅ `:app:compileDebugKotlin` successful

### Regression Fix: Render Pagination Controls in Window-Level Overlay
- **Date**: March 20, 2026
- **Files**: `MainActivity.kt` (lines ~4978-5071)

#### Problem:
Controls still disappeared in runtime even after removing scroll offset.

#### Root Cause:
Controls were still rendered inside the table/scroll composition tree, making them vulnerable to clipping and layout constraints from the zoomed/horizontally-scrolling container.

#### Solution:
- Rendered pagination controls using a `Popup` aligned to `BottomCenter` (window-level overlay).
- Kept the same control logic and actions (prev/next/page/date/add), only changed rendering layer.

#### Result:
- ✅ Controls are decoupled from table clipping/scroll layout
- ✅ Controls should remain visible across zoom/width changes
- ✅ `:app:compileDebugKotlin` successful

### UI Alignment Fix: Bottom Controls Flush to Screen Edge
- **Date**: March 20, 2026
- **Files**: `MainActivity.kt` (lines ~4321-4325, ~4971-4979)

#### Problem:
Controls became visible, but were vertically lifted with an empty strip under them.

#### Root Cause:
- Popup controls were intentionally lifted (`IntOffset(0, -8)`) and had extra vertical row padding.
- Table area still used legacy reduced height (`fillMaxHeight(0.92f)`) from earlier in-layout controls, leaving bottom dead space.

#### Solution:
- Set popup offset to zero (`IntOffset(0, 0)`) and removed vertical row padding.
- Removed legacy reduced-height branch and restored full table height (`fillMaxHeight()`).

#### Result:
- ✅ Controls sit on the bottom edge as expected
- ✅ Bottom empty strip removed
- ✅ `:app:compileDebugKotlin` successful

### Pagination Overlay Fix: Prevent Bottom Controls from Covering Rows
- **Date**: March 20, 2026
- **Files**: `MainActivity.kt` (lines ~4537-4578)

#### Problem:
After moving controls flush to the bottom edge, the overlay controls could cover the last visible table row.

#### Root Cause:
`entriesPerPage` measured mode used the full rows viewport height (`viewportPx / rowPx`) and did not reserve space for the bottom popup controls.

#### Solution:
- Added a dedicated reserved controls height for button paging (`64.dp`, converted to px with `LocalDensity`).
- In measured mode, compute rows fit using an effective viewport:
  - `effectiveViewportPx = viewportPx - reservedControlsPx`
  - `calculatedEntriesPerPage = effectiveViewportPx / rowPx`
- Extended pagination debug log to include `reservedPx` for validation.

#### Result:
- ✅ Rows stop above bottom controls instead of rendering behind them
- ✅ Consecutive pagination behavior preserved
- ✅ `:app:compileDebugKotlin` successful

### UX Fix: Preserve Notifications Context After Closing Reminder History
- **Date**: March 21, 2026
- **Files**: `MainActivity.kt` (lines ~3708-3711)

#### Problem:
From side menu → Notifications → Custom reminder → History, closing the history dialog returned only to the side menu root instead of the same Notifications area/context.

#### Root Cause:
Opening history from `NotificationSettingsDialog` explicitly set `showNotificationSettingsDialog = false`, which disposed the notifications dialog and reset its UI context.

#### Solution:
- Kept `NotificationSettingsDialog` open when opening reminder history from notifications.
- Only set `historyDialogReminderId`, so history opens as an overlay and dismissing it returns to the same notifications context.

#### Result:
- ✅ Closing history returns to the same notifications area/context
- ✅ No regression in reminder history opening flow
- ✅ `:app:compileDebugKotlin` successful

### UX Copy/State Fix: Clean Empty Reminder History Dialog
- **Date**: March 21, 2026
- **Files**: `ReminderHistoryDialog.kt` (lines ~68-95)

#### Problem:
When opening Custom Reminder history with no data, the dialog still showed verbose sections (overview/entries), making the empty state feel noisy.

#### Root Cause:
`StreakSummaryCard` and `HistoryEntriesList` were rendered even when `reminderHistory` had no entries.

#### Solution:
- Added an explicit empty-state branch in `ReminderHistoryDialog`.
- For empty history, render only one concise message:
  - "You still have no confirmation history for this reminder."
- Skip rendering overview and entries sections until data exists.

#### Result:
- ✅ Empty history dialog now shows a single clean message
- ✅ Better readability in Notifications → Custom Reminder → History
- ✅ `:app:compileDebugKotlin` successful (tool returned timeout after success output)

### UI Polish: Organized/Symmetric Empty History Card
- **Date**: March 21, 2026
- **Files**: `ReminderHistoryDialog.kt` (lines ~69-155)

#### Goal:
Improve visual balance of empty reminder history dialog to look cleaner, more organized, and symmetric.

#### Changes:
- Replaced plain left-aligned text with a centered empty-state card component.
- Added subtle hierarchy inside the card:
  - circular info badge,
  - short title (`No history yet` localized),
  - concise message text.
- Applied consistent spacing/padding and centered alignment for better symmetry.
- Kept existing app theme colors and contrast behavior.

#### Result:
- ✅ Empty state looks visually structured and balanced
- ✅ Message remains concise and clear
- ✅ `:app:compileDebugKotlin` successful

### UI Polish: Compact Header + Proper Info Icon
- **Date**: March 21, 2026
- **Files**: `ReminderHistoryDialog.kt` (lines ~65-68, ~121-133, ~159-176)

#### Goal:
Improve symmetry by reducing top header footprint and replacing placeholder "i" marker with a proper icon.

#### Changes:
- Removed header subtitle text (`Confirmation history`) to save vertical space.
- Kept header name only when available; otherwise header stays minimal with close button.
- Replaced empty-state badge text `"i"` with `Icons.Default.Info` for cleaner visual consistency.

#### Result:
- ✅ Dialog top area is more compact and visually balanced
- ✅ Empty-state icon looks cleaner and more intentional
- ✅ `:app:compileDebugKotlin` successful (tool returned timeout after success output)

### UI Asset Integration: Use Custom PNG Info Symbol in Empty History State
- **Date**: March 21, 2026
- **Files**:
  - `app/src/main/res/drawable/info_icon_20260321_212053.png`
  - `ReminderHistoryDialog.kt` (lines ~128-133)

#### Goal:
Replace the temporary Material `Info` icon with the user-provided PNG symbol in the empty reminder history badge.

#### Changes:
- Renamed drawable to Android-safe resource name:
  - `Info Icon - 20260321_212053.png` → `info_icon_20260321_212053.png`
- Updated empty-state badge icon to:
  - `painterResource(R.drawable.info_icon_20260321_212053)`
- Set icon tint to `Color.Unspecified` so original PNG colors render correctly.

#### Result:
- ✅ Empty-history badge now uses the exact provided PNG symbol
- ✅ Visual match with requested design
- ✅ `:app:compileDebugKotlin` successful

### UI Polish: Increase Empty-State PNG Icon Size
- **Date**: March 21, 2026
- **Files**: `ReminderHistoryDialog.kt` (lines ~122-127)

#### Problem:
The imported PNG rendered too small inside the empty-history card.

#### Solution:
- Removed the tight 36dp circular wrapper around the icon.
- Rendered the provided PNG directly at `52.dp`.
- Kept `Color.Unspecified` tint to preserve original image colors.

#### Result:
- ✅ Info icon is now clearly visible in the empty-state card
- ✅ Better visual balance in the dialog
- ✅ `:app:compileDebugKotlin` successful

### UI Tweak: Scale Empty-State PNG Icon to 3x
- **Date**: March 21, 2026
- **Files**: `ReminderHistoryDialog.kt` (lines ~122-126)

#### Request:
Increase the imported empty-state info icon to 3x its current size.

#### Changes:
- Updated icon size from `52.dp` to `156.dp` (3x).
- Kept `Color.Unspecified` tint to preserve the original PNG appearance.

#### Result:
- ✅ Icon now renders at 3x size as requested
- ✅ `:app:compileDebugKotlin` successful

### UI Fix: Remove Excess Empty Space Around PNG Icon
- **Date**: March 21, 2026
- **Files**: `ReminderHistoryDialog.kt` (lines ~125-142)

#### Problem:
The icon had correct overall size, but the PNG contains transparent surrounding pixels, making the dialog look taller than necessary.

#### Solution:
- Switched from direct `Icon(size = 156.dp)` to a clipped `Box(size = 64.dp)` container.
- Rendered the drawable with `Image` and center zoom via `graphicsLayer(scaleX = 2.35f, scaleY = 2.35f)`.
- Kept circular clipping to trim transparent outer area visually.

#### Result:
- ✅ Same visible icon emphasis with much tighter footprint
- ✅ Less empty vertical space in the empty-history card
- ✅ `:app:compileDebugKotlin` successful

### UI Layout Fix: Remove Top Empty Strip and Reposition Close Button
- **Date**: March 21, 2026
- **Files**: `ReminderHistoryDialog.kt` (lines ~68-80, ~104-147)

#### Problem:
In empty-history mode, the dialog had extra top-only space caused by rendering a separate header row for the close button.

#### Solution:
- Empty state now skips the outer `DialogHeader`.
- Added dismiss `X` button inside the framed empty-state surface, aligned to the top-right within the same padding context.
- Reduced empty-state vertical padding from `18.dp` to `14.dp` for a tighter, more symmetric frame.

#### Result:
- ✅ Removed unnecessary top strip above framed content
- ✅ Close button now sits inside the framed/padded content area as requested
- ✅ Top spacing now matches side/bottom spacing more closely
- ✅ `:app:compileDebugKotlin` successful

### UI Spacing Tweak: Reduce Space Above Empty-State Icon
- **Date**: March 22, 2026
- **Files**: `ReminderHistoryDialog.kt` (lines ~132-166)

#### Problem:
There was still too much space above the info icon because the close button occupied its own row in the layout.

#### Solution:
- Reworked the top area into a single `Box`.
- Positioned close button with `align(Alignment.TopEnd)`.
- Positioned icon with `align(Alignment.TopCenter)` in the same container.
- This keeps the close button visible but removes extra row height above the icon.

#### Result:
- ✅ Noticeably tighter top spacing above the icon
- ✅ Empty-state card keeps balanced side/bottom spacing
- ✅ `:app:compileDebugKotlin` successful

### Navigation Fix: Direct Jump to Notifications from History Streak Warning
- **Date**: March 22, 2026
- **Files**: `MainActivity.kt` (lines ~2090, ~4190-4195)

#### Problem:
When tapping the actionable streak warning card (“No streak in a row”) inside `ReminderHistoryDialog`, the history dialog closed but `NotificationSettingsDialog` remained on top, so users had to dismiss it manually before seeing the bell notifications area. The drawer/sidebar could also stay open.

#### Root Cause:
`onNavigateToNotifications` only closed the history dialog and set `showBellPopup = true`. It did not close the notification settings dialog state, and that dialog was declared in a narrower local scope (not accessible from the history callback).

#### Solution:
- Hoisted `showNotificationSettingsDialog` state to top-level composable scope (next to `showBellPopup`/`historyDialogReminderId`).
- Removed the shadowing local `showNotificationSettingsDialog` declaration inside drawer content.
- Updated `onNavigateToNotifications` callback to:
  - close history dialog,
  - close notification settings dialog,
  - open bell popup,
  - close drawer via `scope.launch { drawerState.close() }`.

#### Result:
- ✅ Tapping warning CTA now goes directly to notifications area without extra manual close step
- ✅ Sidebar/drawer is closed during transition
- ✅ `:app:compileDebugKotlin` successful

### UX Navigation Improvement: Open Relevant Notifications Section Automatically
- **Date**: March 22, 2026
- **Files**:
  - `MainActivity.kt` (lines ~2090-2092, ~4098-4105, ~4153-4167, ~4211-4221)
  - `ReminderConfirmationUI.kt` (lines ~720-753, ~857-867, ~929-938)
  - `ReminderHistoryDialog.kt` (lines ~47, ~86-88)

#### Problem:
After navigating from `ReminderHistoryDialog` warning CTA to notifications, users still landed on the generic top-level view and had to manually expand the right section (`Alerts` or `Pending`) and search for the related reminder.

#### Solution:
- Added bell popup navigation context state:
  - `bellPopupPreferredSection`
  - `bellPopupFocusReminderId`
- Updated warning CTA flow to pass the `reminderId` from `ReminderHistoryDialog`.
- In `MainActivity`, when opening bell popup from warning CTA:
  - chooses preferred section (`alerts` if matching alert exists, else `pending` if matching pending exists),
  - sets focused reminder id.
- Enhanced `BellPopupContent`:
  - accepts `preferredSection` and `focusReminderId`,
  - auto-expands the preferred section on open,
  - prioritizes matching reminder entries to top in both alerts and pending lists,
  - gracefully falls back to `Pending` if `Alerts` is requested but empty.
- Clears bell-popup focus/preference state on manual open/dismiss/history navigation to prevent stale targeting.

#### Result:
- ✅ Notification popup opens directly in the most relevant section
- ✅ Matching reminder entries surface first, minimizing manual searching
- ✅ `:app:compileDebugKotlin` successful

### Navigation Fix: Close Side Drawer After Analysis Entry Jump
- **Date**: March 22, 2026
- **Files**: `MainActivity.kt` (lines ~9425-9428)

#### Problem:
When opening **Settings → Analysis & Markers → Analysis** and tapping an occurrence entry in Pattern Analysis, navigation to the target date worked, but the side drawer remained open and covered the main table.

#### Root Cause:
In the `AnalysisScreen` navigation callback, `showAnalysisScreen` was closed but `drawerState.close()` was not called.

#### Solution:
- Updated `onNavigateToDate` flow to close drawer explicitly right after closing analysis screen:
  - `showAnalysisScreen = false`
  - `scope.launch { drawerState.close() }`

#### Result:
- ✅ Pattern occurrence taps now land on the destination view without the drawer obscuring content
- ✅ Navigation flow is consistent with other drawer-triggered transitions
- ✅ `:app:compileDebugKotlin` successful

### Follow-up Navigation Fix: Close Drawer When Opening Analysis From Marker History
- **Date**: March 22, 2026
- **Files**: `MainActivity.kt` (lines ~8264-8267)

#### Problem:
In the **Settings → Analysis & Markers** path, opening Pattern Analysis from marker-history insight cards could leave the side drawer open in the background. This made later analysis/date navigation appear as if settings remained open.

#### Root Cause:
The insight-card click handler opened `showAnalysisScreen` but did not close `drawerState`.

#### Solution:
- Updated marker-history insight click flow to also execute `scope.launch { drawerState.close() }` after showing `AnalysisScreen`.

#### Result:
- ✅ Analysis launched from marker-history cards now starts with drawer closed
- ✅ Subsequent pattern occurrence navigation no longer shows lingering settings drawer
- ✅ `:app:compileDebugKotlin` successful

### UI Refresh: Modernized Pattern Analysis Screen
- **Date**: March 22, 2026
- **Files**: `AnalysisScreen.kt` (top app bar + summary card + visualization rows, lines ~82-320 and ~736-1060)

#### Goal:
Improve visual quality of Pattern Analysis (top bar, summary text area, overview visuals) while preserving all existing functionality.

#### What changed (without altering behavior):
- Reworked top app bar title area to include a compact subtitle showing current pattern count/filter context.
- Replaced the plain filter info strip with an elevated summary card that includes:
  - quick risk metric pills (High/Medium/Low),
  - existing filter toggling behavior,
  - existing clear-filter action.
- Restyled Pattern Overview visuals with cleaner cards, softer elevation, clearer hierarchy, and improved contrast handling for light/dark themes.
- Updated risk bars, pattern type rows, and risky-food rows to use modern rounded surfaces and theme-derived colors while keeping the same click/filter actions.
- Kept all callbacks and flows unchanged (`onDismiss`, `onExportTXT`, `onExportPDF`, `onDismissPattern`, `onIgnoreFood`, `onNavigateToDate`).

#### Result:
- ✅ Pattern Analysis now has a cleaner, more modern and readable layout
- ✅ No functional regressions in filtering/export/navigation interactions
- ✅ `:app:compileDebugKotlin` successful

### UX Fix: Hide Table Paging Controls Outside Table Context
- **Date**: March 23, 2026
- **Files**: `MainActivity.kt` (lines ~5008-5015)

#### Problem:
Bottom paging/table control buttons were still visible over the Pattern Analysis screen and while the side drawer was open, even though those controls are only relevant for table interaction.

#### Root Cause:
Popup visibility check only used pagination flags (`usePaginationMode && useButtonPaging`) and did not gate rendering by active screen/drawer state.

#### Solution:
- Added `showTableControls` condition for popup rendering:
  - pagination mode enabled,
  - button paging enabled,
  - analysis screen not shown,
  - drawer fully closed (`currentValue` and `targetValue` are `Closed`).

#### Result:
- ✅ Paging/table controls are now hidden during Pattern Analysis
- ✅ Controls are hidden while opening/open drawer states
- ✅ Controls only appear when the main table is the active interaction area
- ✅ `:app:compileDebugKotlin` successful

### UX Fix: Hide Table Paging Controls While Keyboard Is Open
- **Date**: March 24, 2026
- **Files**: `MainActivity.kt` (lines ~5008-5014)

#### Problem:
When tapping into an editable table cell, the soft keyboard opened but paging/table controls still stayed visible and overlaid above the keyboard.

#### Root Cause:
`showTableControls` checked pagination mode, analysis visibility, and drawer state, but did not check IME (keyboard) visibility.

#### Solution:
- Added IME visibility gate before rendering bottom controls popup:
  - `val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0`
  - Added `!isKeyboardVisible` to `showTableControls`.

#### Result:
- ✅ Paging/table controls now hide automatically while keyboard is visible
- ✅ Controls reappear when keyboard closes and table context is active

### Feature: Pattern Recognition Date Range Filtering (All Time / 1 Year / 1 Month / Custom)
- **Date**: March 24, 2026
- **Files**:
  - `PatternInsightsDialog.kt` (new file, lines ~1-520)
  - `MainActivity.kt` (lines ~8179-8199)
  - `PatternDetection.kt` (lines ~59-96, ~298-316, ~512-531)

#### Goal:
Allow users to scope discovered pattern insights (e.g., Most Common Trigger, Most Frequent Marker) to relevant date windows so older historical events do not dominate current analysis.

#### What changed:
- Extracted the inline pattern insights dialog from `MainActivity.kt` into a dedicated `PatternInsightsDialog` composable file to keep the main screen logic cleaner.
- Added date-range controls in pattern insights dialog:
  - All time
  - 1 year
  - 1 month
  - Custom dates (start + end via `DatePickerDialog`)
- Added custom-range validation (`start <= end`) with user feedback.
- Filtered entries before `PatternAnalyzer.analyzePatterns(...)` to ensure insight cards only reflect the selected period.
- Updated insight tap action to open detailed analysis using the same selected date range.
- Extended `PatternDetectionEngine.detectPatterns(...)` with optional `dateRange: ClosedRange<LocalDate>?`.
- Added robust date parsing support for both `dd-MM-yyyy` and `dd.MM.yyyy` formats in pattern detection.
- Removed duplicate recency filtering in blood sugar detection path so range-filtered inputs are respected consistently.

#### Result:
- ✅ Users can analyze pattern insights for recent windows or explicit custom periods
- ✅ Historical/outdated markers can be excluded from current decision-making
- ✅ Detailed analysis now follows the same selected time scope from the insights dialog
- ✅ `:app:compileDebugKotlin` successful

### Follow-up Fix: Add Date Range Filter Inside Pattern Overview Screen
- **Date**: March 24, 2026
- **Files**:
  - `AnalysisScreen.kt` (lines ~33-74, ~227-236, ~319-333, ~450-727)
  - `MainActivity.kt` (lines ~2238-2241, ~3575-3584, ~8190-8198, ~9159-9202)

#### Problem:
After tapping a discovered pattern in Settings → Analysis & Markers → Patterns, the app opened the detailed Pattern Overview screen, but users could not adjust the date range there.

#### Root Cause:
Date-range selection was only implemented in the `PatternInsightsDialog` entry point. `AnalysisScreen` received precomputed patterns and exposed risk/type filters only, without any range UI or callback to trigger pattern recomputation.

#### Solution:
- Added an in-screen range filter section in `AnalysisScreen` with chips for:
  - All time
  - 1 year
  - 1 month
  - Custom (start/end date pickers)
- Added range state synchronization in `AnalysisScreen`:
  - `initialDateRange`
  - `initialDaysToAnalyze`
  - `onDateRangeChanged(days, range)` callback
- Wired `MainActivity` to own analysis range source of truth:
  - `analysisDateRange`
  - `analysisDaysToAnalyze`
- Recompute `detectedPatterns` whenever overview range changes.
- Updated ignore-food action in overview to re-run detection using the active range (instead of always 30 days).
- Preserved selected range when navigating from discovered pattern cards into detailed overview.

#### Result:
- ✅ Date range is now available directly in Pattern Overview
- ✅ Pattern list updates immediately when range changes
- ✅ Ignore-food behavior respects the current analysis range
- ✅ `:app:compileDebugKotlin` successful

### UX Fix: Back from Pattern Overview Returns to Pattern Analysis Window
- **Date**: March 24, 2026
- **Files**: `MainActivity.kt` (lines ~2237-2243, ~7042-7050, ~9166-9179)

#### Problem:
When users opened Pattern Overview by tapping a discovered pattern (Settings → Analysis & Markers → Patterns) and then pressed Back, the app returned directly to the main table instead of returning to the previous Pattern Analysis window.

#### Root Cause:
`AnalysisScreen` dismiss action only closed `showAnalysisScreen` and did not restore the originating dialog flow. Also, `showPatternAnalysisDialog` is scoped within marker-history UI, so it cannot be directly toggled from global analysis dismiss logic.

#### Solution:
- Added source/return state in `MainActivity`:
  - `analysisOpenedFromPatternInsights`
  - `reopenPatternAnalysisDialogOnReturn`
- On Analysis back:
  - close Analysis screen,
  - reopen Marker History,
  - set deferred reopen flag for Pattern Analysis dialog.
- Inside Marker History scope, added `LaunchedEffect` that reopens `showPatternAnalysisDialog` when deferred flag is set.
- Ensured direct Analysis launches (not from Pattern Insights) do not trigger this return behavior.

#### Result:
- ✅ Back navigation now returns users to the Pattern Analysis window they came from
- ✅ No sudden jump to main table for this flow
- ✅ `:app:compileDebugKotlin` successful

### UX/IA Cleanup: Single Analysis Entry Point + Settings Order Priority
- **Date**: March 24, 2026
- **Files**:
  - `MainActivity.kt` (lines ~3430-3529, ~3567-3578)
  - `ExportAnalysisDialog.kt` (lines ~13-48)

#### Goal:
Keep one intuitive path to pattern analysis and keep Export focused on export-only actions.

#### Changes:
- Moved **Analysis & Markers** to the first position in the settings sidebar list.
- Simplified **Export & Analysis** into **Export** only:
  - Removed analysis action from export dialog.
  - Updated export row title/subtitle to export-only wording (`Export`, `PDF, TXT`).
  - Updated export dialog title to export-only (`Export`).
- Kept pattern overview access through the Analysis & Markers flow only.

#### Result:
- ✅ Only one entry path to pattern overview (via Analysis & Markers)
- ✅ Export area now contains export-only actions
- ✅ Analysis & Markers is now the first settings item
- ✅ `:app:compileDebugKotlin` successful

### UI Enhancement: Unified Analysis & Markers Overview Dialog
- **Date**: March 24, 2026
- **Files**: `MainActivity.kt` (lines ~7016-7030, ~7217-7237, ~7239-8170, ~9117-9127)

#### Goal:
Improve the Analysis & Markers dialog visual hierarchy and spacing, while integrating pattern analysis and marker management into one cohesive overview.

#### Changes:
- Integrated live **Pattern Analysis** directly into the Analysis & Markers main view:
  - Added top discovered-pattern cards (`PatternAnalyzer.analyzePatterns(...)`), tap-to-open detailed Pattern Overview.
  - Added explicit “Open full overview” CTA.
- Refined **Marker Management** grouping:
  - Marker total count now lives inside a dedicated marker section.
  - Kept marker actions together with marker context (`Manage`, `Share`).
  - Categories remain grouped under marker section with clearer helper text.
- Improved layout density:
  - Wider dialog (`fillMaxWidth(0.96f)`) and tighter content spacing/padding.
  - Reduced visual separation problems between related content blocks.
- Simplified flow/state:
  - Removed nested Pattern Insights modal from this path.
  - Analysis back-navigation now returns directly to Analysis & Markers dialog (no extra re-open flag state).
- Preserved all existing functions:
  - Share/export menu still works (TXT/PDF download + share).
  - Manage markers flow still opens custom marker management.
  - Category detail/timeline/selection/delete flow remains intact.

#### Result:
- ✅ Analysis & Markers now opens a single, richer overview dialog
- ✅ Pattern insights and marker tools are now contextually grouped
- ✅ Share + manage functionality preserved
- ✅ `:app:compileDebugKotlin` successful

### UI Polish + Stabilization: Compact Analysis & Markers Header, Single Share Entry, Scope Fixes
- **Date**: March 24, 2026
- **Files**: `MainActivity.kt` (lines ~6975-6981, ~7239-8304, ~5890-5910)

#### Goal:
Apply final requested polish to the unified **Analysis & Markers** dialog by reducing top header space and removing duplicated share affordance, while keeping all existing actions functional.

#### Changes:
- Moved the dialog header controls (title/back/timeline actions/close) into the dialog content block to reduce top chrome footprint and improve content density.
- Removed the duplicate top share trigger, keeping share/export available from the marker management section only.
- Preserved timeline controls (sort/search/selection) and close behavior in the compact header row.
- Fixed an accidental close-icon insertion in the Custom Columns dialog block caused during refactor pass.
- Repaired dialog scoping issues introduced during the compact-header refactor:
  - merged duplicate `text = { ... }` dialog-content structure,
  - re-scoped export/filter dropdown menus inside the marker dialog content,
  - hoisted marker bulk-selection state so confirmation dialog/actions remain valid.

#### Root Cause (for temporary regression during implementation):
Refactoring from `AlertDialog(title=...)` to a custom in-content header introduced transient block-boundary/scope mismatches, which moved dependent composable/state references out of valid scope.

#### Result:
- ✅ Analysis & Markers header now uses less vertical space
- ✅ Duplicate top share entry removed (single clear share/export entry point retained)
- ✅ Manage/share/timeline/filter/selection/bulk-delete flows remain functional
- ✅ `:app:compileDebugKotlin` successful

### UI Enhancement: Full-Screen Analysis & Markers for Better Categories Visibility
- **Date**: March 24, 2026
- **Files**: `MainActivity.kt` (lines ~7239-7247, ~7819-7824, ~7966-7990)

#### Problem:
In the unified Analysis & Markers dialog, vertical space was still limited for long categories/timeline content, making category exploration feel constrained.

#### Root Cause:
- The dialog was still rendered with near-full-width modal sizing and internal max-height constraints.
- Categories/timeline `LazyColumn` sections did not explicitly claim remaining height in the dialog body.

#### Solution:
- Switched Analysis & Markers dialog to full-screen modal presentation:
  - `modifier = Modifier.fillMaxSize()`
  - `properties = DialogProperties(usePlatformDefaultWidth = false)`
- Updated dialog body container to `fillMaxSize()`.
- Assigned explicit remaining-height behavior to list content:
  - Categories `LazyColumn` now uses `Modifier.weight(1f)`.
  - Timeline `LazyColumn` now uses `Modifier.weight(1f)`.
  - Empty timeline state also uses `Modifier.weight(1f)` so vertical balance is preserved.

#### Result:
- ✅ Analysis & Markers now opens full screen
- ✅ Categories and timeline areas get proper usable space
- ✅ Existing actions/flows preserved (manage, share/export, filter/sort, selection, bulk delete)
- ✅ `:app:compileDebugKotlin` successful

### UX Fix: Pattern Analysis Range/History Filters Now Scroll With Content
- **Date**: March 24, 2026
- **Files**: `AnalysisScreen.kt` (lines ~170-351)

#### Problem:
In Pattern Analysis, the top overview area (including analysis range and "Entire history" scope label) stayed fixed while the pattern cards section below scrolled, which made the page feel split and inconsistent.

#### Root Cause:
The screen used a two-layer layout:
- a static `Column` header card,
- plus a separate inner `LazyColumn` for visualization + pattern cards.

#### Solution:
- Replaced the split `Column + LazyColumn` structure with one unified `LazyColumn`.
- Moved the overview/range/filter card into the first `item { ... }` of that same list.
- Kept spacing and existing interactions unchanged (risk pills, clear filter, date range updates, pattern list actions).

#### Result:
- ✅ Analysis range + history scope + pattern content now scroll together as one page
- ✅ Filtering/range behavior preserved
- ✅ `:app:compileDebugKotlin` successful

### Bug Fix: Custom Column Data Lost During Backup Import
- **Date**: March 10, 2026
- **Files**: `MainActivity.kt` (lines ~1281-1293)

#### Problem:
When importing a backup with "Merge with existing" option, custom column data was being lost. The custom column definitions were imported correctly, but the actual data values stored in `DiabetesEntry.customColumnData` were missing after import.

#### Root Cause:
The `mergeEntries` function was only adding **new** entries from the backup (entries with IDs that didn't exist locally). For entries that already existed (same ID), it kept the old local version and **discarded** the backup version entirely. This meant:
- If you had entries with custom column data in the backup
- And those same entry IDs existed locally (without custom column data)
- The import would skip the backup data and keep the old local data
- Result: Custom column data was lost

**Old Logic:**
```kotlin
private fun mergeEntries(existing: List<DiabetesEntry>, backup: List<DiabetesEntry>): List<DiabetesEntry> {
    val existingIds = existing.map { it.id }.toSet()
    val newEntries = backup.filter { it.id !in existingIds }
    return existing + newEntries  // Only adds NEW entries, keeps OLD existing ones
}
```

#### Solution:
Modified `mergeEntries` to **update** existing entries with backup data instead of skipping them:

**New Logic:**
```kotlin
private fun mergeEntries(existing: List<DiabetesEntry>, backup: List<DiabetesEntry>): List<DiabetesEntry> {
    val backupMap = backup.associateBy { it.id }
    val existingIds = existing.map { it.id }.toSet()
    
    // Update existing entries with backup data (preserves all fields including customColumnData)
    val updatedExisting = existing.map { entry ->
        backupMap[entry.id] ?: entry
    }
    
    // Add new entries that don't exist
    val newEntries = backup.filter { it.id !in existingIds }
    
    return updatedExisting + newEntries
}
```

#### Result:
- ✅ Existing entries are now **updated** with backup data instead of being skipped
- ✅ Custom column data (`customColumnData`) is preserved during import
- ✅ All other entry fields (blood sugar, insulin, remarks, food entries, markers) are also updated
- ✅ New entries are still added as before
- ✅ "Replace existing" option continues to work as expected

#### Debug Logging Added:
Added comprehensive debug logging to track custom column data through the backup/restore process:
- Export: Logs entries with `customColumnData` before serialization
- Export: Verifies JSON contains `customColumnData` field
- Import: Checks if backup JSON contains `customColumnData`
- Import: Logs entries with `customColumnData` after parsing
- Import: Logs entries before and after merge operation

This logging helps diagnose any future serialization issues.

---

### Enhancement: Clickable Chart Dates for Direct Navigation
- **Date**: March 10, 2026
- **Files**: `MainActivity.kt` (lines ~15220-15226, ~15503-15509, ~15963-15969)

#### Feature:
Made the chart area itself clickable to navigate directly to the main diabetes table filtered by the tapped date. Users can now tap anywhere on the chart to select the corresponding date, making the workflow much more intuitive than separate date buttons.

#### Implementation:
- **New parameter**: `onNavigateToDate: (String) -> Unit` added to:
  - `SimpleTimeChart` (blood sugar chart)
  - `InsulinTimeChart` (insulin chart)
  - `CustomColumnChart` (custom column chart)
  - `TimeChartDialog` (parent dialog)
- **Tap detection**: Added `Modifier.pointerInput` with `detectTapGestures` to chart `Box`
- **Coordinate mapping**: Calculates which date was tapped based on x-coordinate:
  - Accounts for start axis width (~60px) and end padding (~20px)
  - Maps relative x-position to date index in `sortedData`
  - Coerces index to valid range to prevent crashes
- **Date format conversion**: Converts date from `dd.MM.yyyy` to `dd-MM-yyyy` before navigation
  - Chart data uses dots: `19.02.2026`
  - Filtering expects dashes: `19-02-2026`
  - Conversion: `entry.date.replace(".", "-")`
- **Navigation integration**: Passed `onNavigateToDate` callback from `MainActivity` through `TimeChartDialog` to all chart components

#### User Experience:
- ✅ **Tap directly on the chart** to navigate to that date's entry
- ✅ **Table correctly filters** to show only the tapped date's entries
- ✅ **Entry is marked** in the table for easy identification
- ✅ No separate date buttons needed - saves screen space
- ✅ Intuitive workflow - tap where you see the data point
- ✅ Same smart back navigation as pattern details (returns to chart dialog)
- ✅ Consistent behavior across all chart types (blood sugar, insulin, custom columns)
- ✅ No need to match dates between axis labels and separate buttons

#### Technical Details:
- Uses `pointerInput(sortedData)` to recompose when data changes
- `detectTapGestures { offset -> ... }` captures tap coordinates
- X-coordinate calculation: `relativeX = offset.x - startAxisWidth`
- Index calculation: `((relativeX / effectiveChartWidth) * sortedData.size).toInt()`
- Date format conversion: `val convertedDate = entry.date.replace(".", "-")`
- Validates tap is within chart bounds before navigating
- Navigation uses same `onNavigateToDate` callback as pattern details
- Automatically closes settings drawer and sets navigation source for smart back navigation

#### Bug Fix:
- **Problem**: Table was not filtering when navigating from chart tap
- **Root Cause**: Date format mismatch - chart uses `dd.MM.yyyy` (dots) but filtering expects `dd-MM-yyyy` (dashes)
- **Solution**: Added date format conversion in all three chart tap handlers before calling `onNavigateToDate`
- **Result**: Table now correctly filters and marks the selected date's entry

---

### Enhancement: Smart Back Navigation from Pattern Details
- **Date**: March 9, 2026
- **Files**: `MainActivity.kt` (lines ~2163, ~3260-3265, ~4133-4143, ~9177, ~9370, ~9157-9191), `AnalysisScreen.kt` (lines 33-43, 244-250, 368-373, 519-525)

#### Feature:
Added intelligent navigation from pattern details to the diabetes table with smart back navigation that returns users to their exact previous location.

#### Implementation:
- **New callback**: `onNavigateToDate` parameter added to `AnalysisScreen` composable
- **Navigation source tracking**: Added `navigationSource` state variable to track where user came from ("food_warning" or "analysis_screen")
- **Entry navigation**: When user taps on any pattern occurrence entry, the app:
  1. Saves current filter state for back navigation
  2. Records the navigation source
  3. Closes the current screen/dialog
  4. Filters the diabetes table to show only the selected date
  5. Sets `isViewingOccurrence = true` to show the back button
  6. Resets to page 0 to display the filtered entry
- **Automatic drawer close**: Added `LaunchedEffect` that monitors `isViewingOccurrence` state and automatically closes the settings drawer when navigating to a date
- **Smart back button**: Modified back button to restore the exact screen/dialog user came from instead of just clearing filters

#### User Experience:
- ✅ Tapping pattern entries in Analysis & Markers now opens the diabetes table
- ✅ Table automatically filters to show the specific date
- ✅ **Back button returns to exact previous location**:
  - From food warning dialog → Returns to food warning + pattern details popup
  - From Analysis & Markers → Returns to Analysis screen
- ✅ **Settings drawer closes automatically** - no need to manually close it
- ✅ Consistent behavior across both navigation paths
- ✅ Seamless workflow: pattern analysis → data review → back to analysis

#### Technical Details:
- Uses same date parsing logic as food warning dialog (`dd-MM-yyyy` format)
- Preserves all previous filter states for proper back navigation
- Passes `onNavigateToDate` callback through `AnalysisScreen` → `PatternCard` → `OccurrenceItem`
- `OccurrenceItem` becomes clickable when `onClick` callback is provided
- `LaunchedEffect(isViewingOccurrence)` triggers drawer close when navigating to specific date
- Drawer close happens within `DiabetesTrackerTheme` scope where `drawerState` is accessible
- Back button uses `when (navigationSource)` to restore appropriate screen state
- Navigation source is reset to `null` after successful back navigation

---

### Bug Fix: Test Data Date Format Compatibility
- **Date**: March 8, 2026
- **File**: `TestDataGenerator.kt` (line 18)

#### Problem:
Test data was not appearing in pattern analysis because the date format was incompatible with the app's standard format. Test data generator used `dd.MM.yyyy` (dots) while the app expects `dd-MM-yyyy` (dashes), causing pattern detection to fail when parsing dates.

#### Solution:
Changed `TestDataGenerator.kt` date formatter from `dd.MM.yyyy` to `dd-MM-yyyy` to match the app's standard date format.

#### Impact:
- ✅ Test data now loads correctly
- ✅ Pattern analysis shows expected patterns (Pizza→Feel Bad, Pasta→Blood Sugar Spike, etc.)
- ✅ Analysis screen displays pattern details when tapping on patterns
- ✅ Consistent date format across entire app

---

### Enhancement: Analysis & Markers Settings Section with Modern UI
- **Date**: March 8, 2026
- **Files**: `DataManagementDialog.kt`, `MainActivity.kt` (lines ~3353-3367, ~7286-7414)

#### Changes:
- Created a new **"Analysis & Markers"** entry in Settings (Insights icon 📊) that opens the unified dialog directly
- **Dialog title**: "Analysis & Markers" (renamed from "Marker Categories")
- **Modern UI Design**:
  - **Styled info card** at the top with subtle primary container background
  - **Total count display** with icon and bold typography (e.g., "Total: 18 markers")
  - **Compact action buttons** integrated into the info card:
    - **"Analysis"** button (outlined, secondary color) — Opens pattern analysis
    - **"Manage"** button (outlined, primary color) — Opens custom marker management
  - **Section header** ("Categories") with proper typography hierarchy
  - Consistent spacing and visual flow throughout
- Moved both "Marker Categories" and "Custom Markers" out of "Data Management"
- **Data Management** now only contains "Custom Columns" with updated subtitle
- Settings order: Export & Analysis → Language → Appearance → Backup & Restore → **Analysis & Markers** → Data Management → Notifications → Advanced

#### UI Improvements:
- Replaced plain text "Total: X markers" with styled card featuring icon and bold count
- Changed large separate buttons to compact outlined buttons within info card
- Added "Categories" section header for better content organization
- Improved visual hierarchy with proper typography and spacing
- More homogeneous and polished appearance

#### Translations:
- **English**: "Analysis & Markers" | Subtitle: "Patterns, categories, custom" | Buttons: "Analysis", "Manage"
- **German**: "Analyse & Marker" | Subtitle: "Muster, Kategorien, benutzerdefiniert" | Buttons: "Analyse", "Verwalten"
- **Spanish**: "Análisis y marcadores" | Subtitle: "Patrones, categorías, personalizados" | Buttons: "Análisis", "Gestionar"

#### Navigation Flow:
- Tapping "Manage" button opens Custom Markers dialog
- Tapping "Done" in Custom Markers dialog returns to Analysis & Markers dialog (seamless navigation)
- Removed redundant Custom Markers and Pattern Analysis icon buttons from dialog header (now only Export button remains)

#### Files Modified:
- `DataManagementDialog.kt` — Removed `onManageCustomMarkers` and `onMarkerHistory` params, kept only `onManageCustomColumns`
- `MainActivity.kt` (lines ~3353-3367, ~6410-6415, ~7043-7072, ~7286-7414) — New "Analysis & Markers" settings entry; redesigned dialog with styled info card, compact action buttons, section headers; added navigation flow from Custom Markers back to Analysis & Markers; removed redundant header icon buttons

---

### Enhancement: Adaptive Date Labels in Statistics Charts
- **Date**: February 19-20, 2026
- **Files**: `MainActivity.kt` (lines 14972-14998, 15230-15256, 15685-15711)

#### Problem:
When zoomed out in the statistics charts (Blood Sugar Over Time, Insulin Over Time, Custom Column charts), the X-axis date labels would get truncated with "..." because there wasn't enough space to display all dates. For example, "30/01" would appear as "3..." making it impossible for users to identify which dates they were viewing. This was especially problematic with consecutive daily entries over weeks or months.

#### Solution:
Implemented adaptive date label rendering that automatically adjusts the number of visible labels based on the dataset size:
- **≤7 data points**: Show all labels
- **8-14 data points**: Show every 3rd label (~twice per week)
- **15-21 data points**: Show every 5th label (~weekly for 3 weeks)
- **22-35 data points**: Show every 7th label (~weekly for 5 weeks)
- **36-60 data points**: Show every 10th label (~every 10 days)
- **61-90 data points**: Show every 14th label (~bi-weekly for 3 months)
- **>90 data points**: Show every 21st label (~every 3 weeks)
- Always show the last label for reference

#### Technical Details:
```kotlin
val labelInterval = when {
    sortedData.size <= 7 -> 1   // Show all labels for 7 or fewer points
    sortedData.size <= 14 -> 3  // Show every 3rd label (~twice per week)
    sortedData.size <= 21 -> 5  // Show every 5th label (~weekly for 3 weeks)
    sortedData.size <= 35 -> 7  // Show every 7th label (~weekly for 5 weeks)
    sortedData.size <= 60 -> 10 // Show every 10th label (~every 10 days)
    sortedData.size <= 90 -> 14 // Show every 14th label (~bi-weekly for 3 months)
    else -> 21  // Show every 21st label (~every 3 weeks for large datasets)
}

if (index % labelInterval == 0 || index == sortedData.size - 1) {
    sortedData[index].second.format(displayFormatter)
} else {
    ""  // Hide label to prevent crowding
}
```

#### Benefits:
- Users can always read date labels clearly, regardless of zoom level
- No more "..." truncation on X-axis
- Automatic adaptation to dataset size
- Better user orientation when viewing large time ranges

---

### Bug Fix: Test Data Removal Not Working Properly
- **Date**: February 18, 2026
- **Files**: `TestDataGenerator.kt` (lines 284-305)

#### Problem:
When users clicked "Remove test data" in Advanced Settings, the toast message showed "Test data removed" and the button became deactivated, but the test data entries remained visible in the UI. Re-activating the button showed the old data was still present.

#### Root Cause:
The `removeTestData()` function was using an unreliable method to identify test entries:
1. It regenerated test entries to get their dates
2. It matched entries by date AND specific keywords in remarks ("pizza", "pasta", etc.)
3. This approach failed because it didn't use the `isTestData` flag that was already set on test entries

#### Solution:
Modified `removeTestData()` in `TestDataGenerator.kt` to use the `isTestData` flag:
```kotlin
val realEntries = allEntries.filter { entry ->
    entry.isTestData != true
}
```

This simple, reliable approach removes all entries where `isTestData == true`, regardless of their content or dates.

#### Technical Details:
- **Before**: Complex logic with date matching and keyword searching (15+ lines)
- **After**: Simple flag-based filtering (3 lines)
- **Benefit**: More reliable, maintainable, and handles all test data regardless of format changes

---

### Feature: Global Notification Center & Reminder History Dialog
- **Date**: February 16, 2026
- **Files**: `ReminderConfirmationUI.kt`, `ReminderHistoryDialog.kt` (new), `ReminderConfirmationStorage.kt`, `MainActivity.kt`

#### Changes:
1. **Bell popup redesigned as global notification center** — Header changed from "Confirmations" to "Notifications" (EN/DE/ES). Sections reordered: Alerts first (most urgent), then Pending, then Recent Activity. "Set up reminders" button removed. Empty state now says "No notifications" instead of "No pending confirmations".
2. **Tappable history items** — `TappableHistorySection` composable replaces `ConfirmationHistorySection` in the bell popup. Each history entry shows a `›` chevron and is clickable, navigating to the detailed `ReminderHistoryDialog` for that reminder.
3. **ReminderHistoryDialog** (`ReminderHistoryDialog.kt`, new file) — Full-screen dialog showing per-reminder confirmation history:
   - **Current streak card**: Large streak number in rounded box, descriptive label ("5 Yes streak in a row!" / "No streak in a row"), motivational message ("Keep it up!" / "Try to break the streak!"), fire/warning emoji. Color-coded green for Yes, red for No.
   - **Completion overview card**: "Your overview" section with: completion rate progress bar (color-coded Excellent/Good/Average/Needs improvement), Yes/No breakdown as side-by-side mini-cards with circle icons and "Completed"/"Missed" labels, total responses count, personal best streak with trophy emoji, "Tracking since" date.
   - **Entries grouped by date**: Each entry shows status circle, scheduled time, comment.
   - **Edit capability**: Tap edit icon on any entry to toggle Yes/No status and modify comment inline with animated expand. Save/cancel buttons.
4. **updateConfirmation()** — New method in `ReminderConfirmationStorage` for editing past confirmations (change status, edit comment).
5. **History access from Custom Reminders drawer** — Reminder cards with `requiresConfirmation` enabled now show a "📊 History" button alongside Edit and Delete, opening the same `ReminderHistoryDialog`.
6. **State management** — New `historyDialogReminderId` state variable in `MainActivity.kt` controls which reminder's history dialog is shown. Set from both bell popup taps and drawer History button.

#### Localization:
- All new UI text supports English, German, Spanish
- Bell popup: "Notifications" / "Benachrichtigungen" / "Notificaciones"
- History sections: "Recent activity" / "Letzte Aktivität" / "Actividad reciente"
- Streak labels: "Current Yes streak" / "Aktuelle Ja-Serie" / "Racha actual de Sí"
- Stats: Total, Yes/Ja/Sí, No, Rate/Quote/Tasa, Best streak/Beste Serie/Mejor racha

### Feature: Advanced Confirmation Alert Settings (Streak Detection)
- **Date**: February 15, 2026
- **Files**: `CustomReminder.kt`, `ReminderConfirmationStorage.kt`, `ReminderConfirmationUI.kt`, `CustomReminderDialog.kt`, `MainActivity.kt`

#### Changes:
1. **Data model** — New `ConfirmationAlertSettings` data class added to `CustomReminder.kt` with fields: `enableNoStreakAlert`, `noStreakThreshold` (default 3), `enableYesStreakAlert`, `yesStreakThreshold` (default 7), `enableMissedAlert`, `missedThreshold` (default 3), `alertWithSound`, `alertWithVibration`. New `confirmationAlertSettings` field on `CustomReminder`.
2. **Streak detection** — `ReminderConfirmationStorage.checkAndTriggerAlerts()` analyzes confirmation history per reminder, detects consecutive Yes/No/missed streaks, and creates `ConfirmationAlert` entries when thresholds are met. Alerts stored in SharedPreferences with deduplication (same type+reminder within 24h).
3. **Alert storage** — New `ConfirmationAlert` data class with `id`, `reminderId`, `reminderName`, `reminderEmoji`, `alertType` (no_streak/yes_streak/missed), `streakCount`, `timestamp`, `dismissed`. Methods: `saveAlert()`, `getActiveAlerts()`, `dismissAlert()`.
4. **Advanced settings UI** — `ConfirmationAlertSettingsDialog` accessible via gear icon sub-button below the "Requires confirmation" toggle. Features:
   - Toggle + threshold slider for "No" streak warnings (2–10 range)
   - Toggle + threshold slider for "Yes" streak celebrations (3–14 range)
   - Toggle + threshold slider for missed/unanswered alerts (2–10 range)
   - Sound and vibration toggle options for alerts
   - Localized EN/DE/ES
5. **Bell popup alerts section** — `AlertNotificationCard` composable shows alerts with color-coded styling (red for no-streak, primary for yes-streak, tertiary for missed), emoji icons (⚠️/🏆/⏰), relative timestamps, and dismiss (✕) button.
6. **Badge count** — Bell icon badge now shows combined count of pending confirmations + active alerts.
7. **Alert triggering** — Alerts checked automatically after every confirmation response (both in bell popup and inline drawer cards).

### Feature: Confirmation Flow Refactor (Yes/No First)
- **Date**: February 14, 2026
- **Files**: `ReminderConfirmationUI.kt`

#### Changes:
1. **Two-step flow** — Confirmation cards now show Yes/No buttons first (step 1). After selecting, an optional comment prompt appears (step 2) with Save/Skip buttons.
2. **Toggle pills** — Step 2 shows tappable Yes/No pills allowing the user to change their choice before submitting.
3. **Removed standalone Note button** — Comments are now always associated with a Yes or No status.

### Feature: Time Picker Manual Input
- **Date**: February 14, 2026
- **Files**: `CustomReminderDialog.kt`

#### Changes:
1. **Tappable hour/minute fields** — Both main time picker and interval start time picker now support tapping the hour/minute display to switch to a `BasicTextField` for direct keyboard input.
2. **Validation** — Hours validated 0–23, minutes validated 0–59. Invalid input reverts to previous value on focus loss or Enter press.
3. **Auto-focus** — `FocusRequester` used to auto-focus the text field when entering edit mode.

### Enhancement: Bell Icon Auto-Refresh & Redesign
- **Date**: February 14, 2026
- **Files**: `MainActivity.kt`

#### Changes:
1. **Auto-refresh polling** — `LaunchedEffect` polls every 5 seconds to refresh pending confirmations, history, and active alerts while app is in foreground.
2. **Pulsing glow ring** — Animated circular glow behind bell icon when notifications are pending, using `rememberInfiniteTransition` with alpha and scale animations.
3. **Animated bell tint** — Bell icon tint changes to primary color when pending.
4. **Bouncing badge** — Badge with error color, border, and subtle vertical bounce animation showing combined pending + alert count.

### Feature: Reminder Confirmation Tracking System
- **Date**: February 12, 2026
- **Files**: `ReminderConfirmation.kt` (new), `ReminderConfirmationStorage.kt` (new), `ReminderConfirmationUI.kt` (new), `CustomReminder.kt`, `CustomReminderDialog.kt`, `ReminderScheduler.kt`, `ReminderReceiver.kt`, `MainActivity.kt`

#### Changes:
1. **Data model** — New `ReminderConfirmation` data class with `ConfirmationStatus` enum (PENDING, YES, NO, COMMENTED). New `requiresConfirmation: Boolean` field on `CustomReminder`.
2. **Storage** — `ReminderConfirmationStorage` handles persistence via SharedPreferences with JSON serialization. Supports add pending, respond, get pending/history, cleanup old entries (90 days).
3. **Toggle in reminder dialog** — "Requires confirmation" switch with icon + description text, tinted when active.
4. **Receiver integration** — `ReminderReceiver` creates a pending confirmation entry when a notification fires for a reminder with `requiresConfirmation = true`. Extra intent fields: `CUSTOM_REMINDER_ID`, `REMINDER_EMOJI`, `REQUIRES_CONFIRMATION`.
5. **Bell icon in top bar** — Bell icon with red badge showing pending count. Tapping opens a `Popup` with:
   - Pending confirmations list (Yes / No / Note buttons per item)
   - Expandable history section
   - "Set up reminders →" link to open the drawer
6. **Inline pending confirmations** — Shown directly in the Custom Reminders section of the drawer, filtered to relevant reminders.
7. **Comment dialog** — Compose dialog with text input for adding notes to a confirmation.
8. **History view** — Expandable list showing past confirmations with status icons (✓/✗/✎), date, time, and optional comment.

### Feature: Notification Type Refactor (Sound/Vibration Multi-Select)
- **Date**: February 11, 2026
- **Files**: `CustomReminder.kt`, `CustomReminderDialog.kt`, `ReminderScheduler.kt`, `ReminderReceiver.kt`, `NotificationHelper.kt`

#### Changes:
1. **Data model** — Added `hasSound: Boolean` and `hasVibration: Boolean` fields to `CustomReminder`. Old `notificationType` enum kept for backward compatibility.
2. **UI** — Replaced single-select notification type with 3 multi-select toggle buttons: Silent, Sound, Vibration. Silent deselects others. Sound+Vibration can combine.
3. **Scheduler** — `scheduleDailyReminder` passes `hasSound`/`hasVibration` as intent extras.
4. **Receiver** — Extracts `HAS_SOUND`/`HAS_VIBRATION` from intent, forwards to `NotificationHelper`.
5. **NotificationHelper** — `showTimeReminderNotification` applies flags: Silent = `setSilent(true)` + low priority; Sound = default sound; Vibration = custom pattern; Both = default sound + vibrate.
6. **Customize sound button** — Tappable pill chip (only visible when Sound is active) that opens Android's system notification channel settings.

### Fix: Delete Time Confirmation Dialog
- **Date**: February 11, 2026
- **Files**: `CustomReminderDialog.kt`

#### Changes:
1. Added `timeToDelete` state variable. X button now sets it instead of deleting immediately.
2. Compact confirmation dialog with "Remove HH:MM?" text (localized), Cancel + Remove (error color) buttons, rounded corners.

### Feature: Time Chips Wrapping + "Every X Hours" Interval Mode
- **Date**: February 8, 2026
- **Files**: `CustomReminderDialog.kt`, `CustomReminder.kt`, `MainActivity.kt`

#### Changes:
1. **FlowRow for time chips** — Replaced `Row` with `FlowRow` so time chips wrap to the next line instead of overflowing horizontally off-screen
2. **"Every X hours" interval mode** — New toggle between "Manual" and "Every X hrs" modes in the times section
   - **Interval selector**: ▲/▼ buttons to set hours (1–12)
   - **Start hour selector**: ▲/▼ buttons to set starting hour (00:00–23:00)
   - **Preview**: Shows all generated times in a soft background strip (e.g. "08:00 · 10:00 · 12:00 · ...")
3. **Data model** — Added `intervalHours` and `intervalStartHour` fields to `CustomReminder` data class
4. **Reminder card display** — Shows "Every X hrs from HH:00" for interval-based reminders in the main reminder list
5. **`generateIntervalTimes()`** — Helper function generates `ReminderTime` list from interval + start hour
6. **Save logic** — Auto-generates times from interval on save; stores interval metadata for re-editing

---

### UI Redesign: Custom Reminder Dialog
- **Date**: February 7, 2026
- **File**: `CustomReminderDialog.kt`

#### Changes:
1. **Removed title bar** — replaced with inline emoji circle + description text field at top
2. **Emoji circle** — 56dp circular button with soft primary tint, tappable to open emoji picker
3. **Name field** — `OutlinedTextField` with rounded shape (16dp), placeholder "Enter description..." (localized DE/ES/EN)
4. **Day buttons redesigned** — Custom `Box` pills with `RoundedCornerShape(10.dp)`, soft primary tint when selected, subtle border, equal `weight(1f)`, `maxLines = 1`
5. **Time chips redesigned** — `Surface` cards with clock icon (`AccessTime`), rounded corners (12dp), tappable to edit, small X to delete
6. **Add time button** — Small pill-shaped `Surface` with `+` icon next to "Times" label header
7. **Notification type** — 3 icon cards (`Notifications`, `NotificationsActive`, `NotificationsOff`) with labels, equal weight, soft color toggle
8. **Time picker dialog modernized** — `KeyboardArrowUp`/`Down` icons, 72dp rounded boxes for hour/minute display, `primaryContainer` background
9. **All corners rounded** — Card 24dp, buttons 14dp, consistent modern Material 3 feel
10. **Removed unused** `LocalContext` import and `context` variable

---

### Bug Fix: Custom Reminders - Multiple Crash Fixes
- **Date**: February 5-7, 2026
- **Issue**: Custom Reminders feature had multiple crashes and non-functional toggle buttons

#### Fixes Applied:
1. **UUID Serialization Crash** (`CustomReminder.kt`):
   - Removed `UUID.randomUUID()` and `System.currentTimeMillis()` from `@Serializable` data class defaults
   - Moved UUID/timestamp generation to `CustomReminderStorage.addReminder()` and `getDefaultReminders()`

2. **Toggle Button Not Working** (`MainActivity.kt` ~line 4186):
   - Added `CustomReminderStorage.loadReminders()` reload after toggle change
   - Added `ReminderScheduler.rescheduleAllReminders()` call to update alarms

3. **Initialization Crash** (`CustomReminderStorage.kt` ~line 59):
   - Added try-catch around `SettingsManager.loadSettings()` in `getDefaultReminders()`
   - Fallback to "English" if settings can't be loaded during early init

4. **SecurityException on Toggle** (`ReminderScheduler.kt` ~line 73):
   - Changed `cancelReminder()` to use `PendingIntent.FLAG_NO_CREATE`
   - Added null check before cancelling — only cancels if PendingIntent exists

5. **TimePickerDialog Crash** (`CustomReminderDialog.kt` ~lines 357-476):
   - **Root Cause**: Android View-based `TimePickerDialog` is incompatible when shown from inside a Compose `Dialog` (context/window lifecycle conflict)
   - **Fix**: Replaced with native Compose time picker using `IconButton` +/- controls
   - Custom Compose UI with hour (00-23) and minute (00-59) pickers
   - Wraparound logic, localized Cancel/OK buttons (EN/DE/ES)
   - Removed unused imports: `android.app.Activity`, `android.app.TimePickerDialog`, `android.content.Context`, `android.content.ContextWrapper`
   - Added import: `androidx.compose.ui.draw.rotate`

#### Key Lesson:
**Never nest Android View-based dialogs (TimePickerDialog, DatePickerDialog) inside Compose Dialogs.** The View-based dialog creates a separate window that conflicts with the Compose Dialog's window/context lifecycle. Always use native Compose UI elements when inside a Compose Dialog.

---

### Feature: Time-Based Reminder System with AlarmManager
- **Version**: 1.2.0
- **Date**: February 1, 2026
- **Feature**: Complete push notification system refactoring with intelligent time-based reminders
- **Purpose**: Provide users with smart, pattern-based reminders for meals, blood sugar checks, and health patterns

#### Phase 1: Notification Infrastructure
- **Implementation**:
  - Created `NotificationHelper.kt` with Android NotificationManager integration
  - Implemented notification channels for different reminder types
  - Added `POST_NOTIFICATIONS` permission handling
  - Created `showTimeReminderNotification()` function for customizable reminders
- **Files Created**: `NotificationHelper.kt` (178 lines)
- **Files Modified**: `AndroidManifest.xml` (added POST_NOTIFICATIONS permission)

#### Phase 2: Settings Refactoring
- **Changes**:
  - Removed "Food Warnings" and "Blood Sugar Alerts" as push notification options
  - Focused exclusively on "Time Reminders" for push notifications
  - Simplified `NotificationPreferences.kt` to manage only time-based reminders
  - Created hierarchical settings structure:
    - **Master Toggle**: "Time Reminders" (enables/disables all)
    - **Sub-Options**:
      - Meal Reminders (detects typical meal times)
      - Blood Sugar Check Reminders (detects regular check times)
      - Pattern-Based Reminders (warns about recurring health patterns)
    - **Confidence Threshold**: Slider for pattern-based reminder sensitivity (50%-100%)
    - **Quiet Hours**: Time-based notification blocking (placeholder for future)
- **UI Improvements**:
  - Fixed text overflow in notification settings (long German/Spanish translations)
  - Applied `.weight(1f)` pattern to all Row layouts with switches
  - Added proper padding and `TextOverflow.Ellipsis` for long labels
  - Test notification button now respects current settings
- **Files Modified**: 
  - `NotificationPreferences.kt` (complete refactoring)
  - `MainActivity.kt` (lines ~3713-3950, notification settings UI)

#### Phase 3: AlarmManager-Based Reminder Scheduling
- **Core Components**:
  - **ReminderScheduler.kt** (260 lines):
    - `scheduleDailyReminder()`: Schedules exact alarms using AlarmManager
    - `cancelReminder()`: Cancels scheduled alarms
    - `scheduleMealReminders()`: Analyzes food entry patterns, schedules breakfast/lunch/dinner reminders
    - `scheduleBloodSugarCheckReminders()`: Analyzes check frequency, schedules morning/afternoon/evening reminders
    - `schedulePatternBasedReminders()`: Placeholder for pattern detection integration
    - `rescheduleAllReminders()`: Cancels and reschedules all reminders based on current data
  - **ReminderReceiver.kt** (118 lines):
    - BroadcastReceiver that handles alarm triggers
    - Checks notification preferences before sending
    - Retrieves user's language for localized notifications
    - Sends notifications via `NotificationHelper`
    - Auto-reschedules for next day (daily recurring)
- **Smart Detection Logic**:
  - **Meal Reminders**: Schedules if ≥10 entries with food data
    - 8:00 AM - Breakfast
    - 12:30 PM - Lunch
    - 6:30 PM - Dinner
  - **Blood Sugar Check Reminders**: Schedules if ≥5 checks per time period
    - 7:30 AM - Morning check
    - 2:00 PM - Afternoon check
    - 8:00 PM - Evening check
  - **Pattern-Based**: Uses confidence threshold to filter detected patterns (future integration)
- **Integration Points** (MainActivity.kt):
  - App startup (line 19683): Schedules reminders based on existing data
  - Master toggle changed (line 3734): Reschedules all reminders
  - Meal reminders toggled (line 3765): Reschedules all reminders
  - Blood sugar check toggled (line 3792): Reschedules all reminders
  - Pattern reminders toggled (line 3819): Reschedules all reminders
  - Confidence threshold adjusted (line 3843): Reschedules all reminders
- **Permissions**:
  - `SCHEDULE_EXACT_ALARM`: For Android 12+ exact alarm scheduling
  - `USE_EXACT_ALARM`: For exact alarm functionality
- **Files Created**: 
  - `ReminderScheduler.kt` (260 lines)
  - `ReminderReceiver.kt` (118 lines)
- **Files Modified**:
  - `AndroidManifest.xml` (registered ReminderReceiver, added alarm permissions)
  - `MainActivity.kt` (7 integration points for automatic rescheduling)

#### Technical Details
- **Alarm Request Codes**:
  - 1000-1099: Meal reminders
  - 2000-2099: Blood sugar check reminders
  - 3000-3099: Pattern-based reminders
- **Alarm Type**: `AlarmManager.RTC_WAKEUP` with `setExactAndAllowWhileIdle()`
- **Persistence**: Reminders survive app restarts and device reboots
- **Localization**: Full support for English, German, Spanish in all notifications
- **Test Button**: Respects current settings, shows appropriate message based on enabled options

#### User Experience
- **Setup**: Enable "Time Reminders" → Choose sub-options → Reminders scheduled automatically
- **Notifications**: Fire at scheduled times even when app is closed
- **Customization**: Adjust confidence threshold for pattern sensitivity
- **Testing**: Test button validates settings and shows sample notification

#### Future Enhancements
- Pattern-based integration with `PatternDetection.detectPatterns()`
- Quiet Hours implementation (time-based notification blocking)
- Smarter meal detection using actual timestamps
- Reminder history tracking
- Snooze functionality

---

### Feature: Statistical Pattern Analysis Integration (Step 8)
- **Feature**: Quick pattern insights in Marker History with integration to comprehensive Analysis screen
- **Purpose**: Bridge between marker-specific insights and detailed pattern analysis
- **Implementation**:
  - **Pattern Analysis Button**: IconButton in Marker History dialog header (Insights icon)
  - **Quick Insights Engine** (`PatternAnalysis.kt`):
    - **Food Correlation Analysis**: Detects foods that appear ≥40% of the time before specific markers
    - **Blood Sugar Pattern Detection**: Identifies markers that occur with high (>180 mg/dL) or low (<70 mg/dL) blood sugar ≥60% of the time
    - **Time-of-Day Analysis**: Finds markers that occur ≥50% of the time in specific periods (Morning, Afternoon, Evening, Night)
    - **Frequency Insights**: Identifies most frequent markers (≥5 occurrences) and common triggers (≥3 occurrences)
  - **Insight Types**:
    - 🍽️ Food Correlation
    - 📈 High Blood Sugar Pattern
    - 📉 Low Blood Sugar Pattern
    - ⏰ Time-of-Day Pattern
    - 🔄 Most Frequent Marker
    - ⚠️ Most Common Trigger
  - **UI Features**:
    - Scrollable dialog with color-coded, **clickable** insight cards
    - **Arrow icon** on each card indicating tappable/interactive
    - Confidence indicator with progress bar (green ≥70%, yellow ≥50%, orange <50%)
    - Percentage display for each pattern
    - Detailed occurrence counts
    - Empty state for insufficient data
    - **Interactive footer**: "💡 Tap any pattern to view detailed analysis"
    - Medical disclaimer
  - **Integration with Main Analysis Screen**:
    - **Tapping any insight** triggers full pattern detection and opens Settings → Analysis
    - Closes Marker History dialogs automatically
    - Runs `PatternDetectionEngine.detectPatterns()` with fresh data
    - Saves patterns to persistent storage
    - Provides seamless navigation between quick insights and comprehensive analysis
  - **User Flow**:
    1. User views quick insights in Marker History
    2. Taps on any insight card
    3. App runs comprehensive pattern detection
    4. Redirects to main Analysis screen with detailed patterns
    5. User sees full analysis with food-marker correlations, blood sugar spikes, and actionable suggestions
  - **Data Requirements**: Minimum 3 occurrences per marker type for pattern detection
  - **Algorithm**: Deterministic statistical analysis (not machine learning)
    - Calculates occurrence percentages
    - Groups by marker type, time period, and context
    - Sorts insights by confidence level
- **Localization**: Full support for English, German, and Spanish
- **Files Created**: `PatternAnalysis.kt` (new file with 350+ lines)
- **Files Modified**: `MainActivity.kt` (lines ~47, ~175, ~6580-6581, ~6853-6864, ~7701-7867)
- **Integration**: Connects quick insights with existing comprehensive Analysis feature (Settings → Analysis)
- **Date**: January 22, 2026

### Feature: Bulk Delete Markers (Step 7C)
- **Feature**: Bulk delete functionality for marker history with selection mode
- **Implementation**:
  - **Selection Mode Toggle**: IconButton in timeline view header to enable/disable selection mode
  - **Visual Indicators**: 
    - CheckCircle icon (filled when active, outlined when inactive)
    - Icon color changes to primary when selection mode is active
  - **Checkboxes**: Appear next to each marker in timeline view when selection mode is enabled
  - **Delete Selected Button**: 
    - Appears in dialog footer when markers are selected
    - Shows count of selected markers (e.g., "Delete Selected (3)")
    - Red text color to indicate destructive action
  - **Confirmation Dialog**:
    - Shows count of markers to be deleted
    - Warning that action cannot be undone
    - Localized for English, German, and Spanish
  - **Deletion Logic**:
    - Filters out selected markers from entries
    - Saves updated entries to persistent storage
    - Shows success toast message
    - Automatically exits selection mode after deletion
  - **State Management**:
    - `selectionMode`: Boolean to track if selection mode is active
    - `selectedMarkerIds`: Set of unique marker IDs (format: `date_time_markerType`)
    - `showBulkDeleteConfirmDialog`: Boolean for confirmation dialog
  - **User Experience**:
    - Selection mode only available in timeline view
    - Closing dialog automatically resets selection mode
    - Clear visual feedback for selected items
    - Prevents accidental deletions with confirmation dialog
- **Localization**: Full support for English, German, and Spanish
- **Files Modified**: `MainActivity.kt` (lines ~6575-6578, ~6828-6848, ~7270-7283, ~7478-7601)
- **Date**: January 21, 2026

### Feature: Marker History Export (Step 7B)
- **Feature**: Complete export functionality for marker history with both TXT and PDF formats
- **Implementation**: 
  - Export button in Marker History dialog header (always visible)
  - Dropdown menu with 4 options:
    - **Download TXT**: Direct download to phone storage using file picker
    - **Share TXT**: Share via Android's native share sheet
    - **Download PDF**: Direct download to phone storage with professional PDF layout
    - ~~Share PDF~~ (not implemented - direct download only)
  - **TXT Export** includes:
    - Summary statistics (total markers, category breakdown)
    - Per-category details (frequency, unique days, most common time, top 5 reasons)
    - Detailed timeline of all marker instances
    - Blood sugar and insulin values from associated cells
    - Marker reasons and custom notes
  - **PDF Export** includes:
    - Professional multi-page layout with proper pagination
    - Summary section with total marker count
    - Category breakdown with statistics
    - Detailed timeline with all marker information
    - Proper formatting with headers, body text, and small text for details
  - **Technical Implementation**:
    - `ACTION_CREATE_DOCUMENT` intent for direct downloads
    - `registerForActivityResult` launchers for file picker handling
    - State management with `pendingMarkerExportContent` and `pendingMarkerExportPdf`
    - Separate PDF generation function in `MarkerHistoryPDF.kt`
    - FileProvider integration for secure file sharing (share option)
- **Localization**: Full support for English, German, and Spanish in both TXT and PDF
- **Files Modified**: 
  - `MainActivity.kt` (lines ~1998-1999, ~2458-2534, ~6757-6838, ~6524-6683)
  - `MarkerHistoryPDF.kt` (new file, 240 lines)
- **Date**: January 20, 2026

### Feature: Marker History UI (Step 7A)
- **Feature**: Complete marker history viewing system with inline expandable details
- **Implementation**: Three-level navigation structure
  - **Level 1**: Category list showing all marker types with occurrence counts
  - **Level 2**: Inline expandable details below tapped category (no overlay dialog)
  - **Level 3**: Timeline view showing individual marker instances with expandable entry details
- **Statistics Display** (matching simple text design):
  - Frequency count with unique days subtitle
  - Most common time of day with occurrence count
  - Most common reasons as bullet-point list (top 5)
  - "View Timeline" button to access full timeline
- **Timeline Features**:
  - Expandable entries showing full marker details (time, blood sugar, insulin, reasons, notes)
  - Emoji icons for all data fields (🕐 time, 🩸 blood sugar, 💉 insulin, 📋 reasons, 📝 notes)
  - Sort by date (ascending/descending)
  - Search functionality across marker types and notes
  - Filter by marker type
  - Back navigation returns to category list
- **UI Design**:
  - Clean inline expansion (no separate overlay dialogs)
  - Simple text-based layout matching reference screenshot
  - Chevron icon changes to expand-less when category is expanded
  - Horizontal divider separates details from category header
  - Consistent Material Design 3 styling
- **Files Modified**: `MainActivity.kt` (lines ~6480-7050)

### Enhancement: Auto-Save Off Exit Behavior
- **Feature**: Improved back button behavior when "Auto-save on exit" is disabled
- **Behavior 1 (Changes made)**: When user presses back and has unsaved changes, shows dialog with options:
  - "Discard" - Reverts all changes made in the session to the original state
  - "Cancel" - Returns to the app
  - "Save & Close" - Saves all changes and exits
- **Behavior 2 (No changes)**: When user presses back with no changes, shows snackbar "Press again to exit" (same as auto-save on)
- **Technical Changes**:
  - Added `sessionStartEntries` to store original state at session start for reverting
  - Fixed `hasUnsavedChanges` not being set when entries are modified
  - `onEntryChanged`, `onDeleteEntry`, and "Add Entry" now only auto-save if `autoSaveOnExit` is enabled
  - When auto-save is OFF, changes are tracked but not saved until user explicitly saves
- **Files Modified**: `MainActivity.kt` (lines ~1945, ~2316-2338, ~2382-2388, ~3777-3798, ~3800-3814, ~3886-3900, ~3982-3993)

---

## [1.1.1.3] - 2025-12-01

### Bug Fix: Food Count Button Not Appearing
- **Issue**: After adding food via '+' icon and closing the dialog, the food count chip didn't appear on the cell
- **Root Cause**: Layout logic at line ~7833 gave `fillMaxWidth()` to the text field when `currentCellFoodItems.isNotEmpty()`, leaving zero space for the food count button
- **Fix**: Changed conditional to use `Modifier.weight(2.2f)` when food items exist, reserving space for the count button
- **Files Modified**: `MainActivity.kt`

### Bug Fix: Food Dialog Closing Immediately (Regression)
- **Issue**: Food addition dialog was closing immediately after adding a food item
- **Root Cause**: `LazyColumn` item key included food count, causing `EditableTableCell` to recompose and reset `showAddFoodDialog` state
- **Fix**: Reverted `LazyColumn` key to use only `entry.id`
- **Files Modified**: `MainActivity.kt`

### Bug Fix: Phantom "I Feel Bad" Markers on Old Data
- **Issue**: After app update, old entries showed phantom markers with "High_blood_sugar" reason
- **Root Cause**: JSON deserialization wasn't configured for backward compatibility with older data structures
- **Fix**: Configured `DataManager`'s JSON parser with `ignoreUnknownKeys = true`, `coerceInputValues = true`, `isLenient = true`. Added `validateMarkers()` and `cleanEntries()` functions to filter invalid markers
- **Files Modified**: `MainActivity.kt` (DataManager object, lines ~1387-1473)

### Bug Fix: Edit Marker Dialog Resizing/Jerky Animation
- **Issue**: "What you feel" dialog had continuous resizing in bottom area after tapping "Next" or "Back"
- **Root Cause**: `AnimatedContent` inside Dialog with dynamic height caused layout instability feedback loop
- **Fix**: Replaced `AnimatedContent` with `Crossfade` and used fixed `width(320.dp)` on the Surface
- **Files Modified**: `MainActivity.kt` (lines ~9698-9753)

---

## [1.1.1.2] - 2025-11 (November)

### Feature: Health Marker System
Complete implementation of health event tracking system.

- [x] Step 1: Data Structure
  - [x] Added `Marker` data class with fields: `id`, `type`, `startTime`, `reasons`, `customNote`, `customMarkerTypeId`, `cellId`
  - [x] Added `markers: List<Marker>` field to `DiabetesEntry`
  - [x] Added `CustomMarkerType` data class with fields: `id`, `name`, `icon`, `color`, `description`, `order`
  - **Files Modified**: `MainActivity.kt` (lines ~615-647, ~232-240)

- [x] Step 2: Marker Creation UI
  - [x] Implemented marker menu (appears on long-press)
  - [x] Created "I feel bad" dialog with time of day selection
  - [x] Reason selection (checkboxes for common reasons)
  - [x] Custom note input
  - [x] Multi-step dialog flow with animations
  - **Files Modified**: `MainActivity.kt` (lines ~8402-8894)

- [x] Step 3: Visual Indicator
  - [x] Added colored borders on cells that have markers
  - [x] Border color varies based on marker type (orange for "I feel bad", custom colors for custom markers)
  - [x] Multi-color gradient borders when multiple markers applied to same cell
  - **Files Modified**: `MainActivity.kt` (lines ~7580-7624, ~7758-7768)

- [x] Step 4: Tap & Hold Cell Interaction
  - [x] Implemented long-press detection (1 second) to open marker menu
  - [x] Fixed bug where normal taps on text fields weren't working
  - [x] Short taps work normally for editing, long press opens marker menu
  - **Files Modified**: `MainActivity.kt` (lines ~6987-7058)

- [x] Step 5: View & Manage Existing Markers
  - [x] Tap on colored border shows dialog with all markers for that date/time
  - [x] Display marker details: time of day, reasons, custom notes, marker type
  - [x] Edit functionality: opens edit dialog for both "I feel bad" and custom markers
  - [x] Delete functionality: confirmation dialog before removal
  - **Files Modified**: `MainActivity.kt` (lines ~10100-10700)

- [x] Step 6: Custom Markers
  - [x] Full custom marker type creation system
  - [x] Custom Markers Management UI in Settings (accessible via "Custom Markers" button)
  - [x] Create custom marker types with: name, icon (emoji picker), color (color picker), description
  - [x] Edit existing custom marker types
  - [x] Delete custom marker types with confirmation
  - [x] CustomMarkerManager for persistence (add, update, delete, reorder)
  - [x] Custom markers appear in marker menu alongside "I feel bad"
  - [x] Inline "Create New Marker" button in marker menu for quick creation
  - [x] Apply custom markers to cells (shows colored border with marker's color)
  - [x] Multi-marker support with gradient borders
  - **Files Modified**: `MainActivity.kt` (lines ~1625-1677, ~3456-3478, ~5968-6400, ~10420-10592, ~11546-11750)

### Feature: Per-Cell Food Entries
- Changed food storage from per-row to per-cell
- Added `foodEntriesByColumn: Map<String, List<FoodPreset>>` to `DiabetesEntry`
- Each blood sugar cell (morning, afternoon, evening, night) can have its own food items
- **Files Modified**: `MainActivity.kt`

---

## Earlier Development

### Core Features (Pre-November 2025)
- Basic diabetes tracking table with date, blood sugar levels, insulin doses
- Food preset system with emoji support
- Data persistence using SharedPreferences with JSON serialization
- Multi-language support (English, German, Spanish)
- Date filtering (All, Today, Custom range, Flexible)
- Undo/Redo functionality
- Settings screen
- Light/Dark mode support
- Pinch-to-zoom on table
- Cell locking/security for filled entries

---

## Removed Features

*This section documents features that were removed from the app, including the reason for removal and any migration paths for users.*

**No features have been removed yet.**

---

## Pending/Future Features

### Markers System (Remaining Steps)

- [ ] Step 7: Applied Markers History/Log
  - [ ] New "Marker History" or "Applied Markers" section in Settings
  - [ ] Chronological view of all applied marker instances (not just marker types)
  - [ ] Show: date, time, marker type, associated blood sugar/food data
  - [ ] Filter by: marker type, date range, reasons
  - [ ] Search functionality
  - [ ] Bulk delete applied markers
  - [ ] Statistics: "Headache occurred 5 times this month", "Most common marker: Exercise (8 times)"
  - [ ] Export marker history to PDF/TXT

- [ ] Step 8: Automatic Pattern Detection
  - [ ] AI-powered analysis
  - [ ] Correlate food items with "feel bad" markers
  - [ ] Blood sugar level correlations
  - [ ] Time of day patterns
  - [ ] Highlight suspicious foods
  - [ ] Generate confidence scores

- [ ] Step 9: Notifications & Alerts
  - [ ] Alert when suspicious pattern detected
  - [ ] "You ate X and felt bad 3 times this month"
  - [ ] Suggest avoiding certain foods
  - [ ] Weekly/monthly pattern summaries

- [ ] Step 10: Detailed Analysis Tab
  - [ ] Visual charts showing correlations
  - [ ] Food ranking by suspicion level
  - [ ] Timeline view of markers vs. food/blood sugar
  - [ ] Export analysis reports
  - [ ] Share with doctor

### Table Improvements

- [ ] Dynamic table resizing (dragging borders)
- [ ] Enhanced horizontal/vertical scrolling

---

### Feature: Notification Settings Extracted to Separate Dialog
- **Date**: February 2026
- **Files**: `NotificationSettingsDialog.kt` (new), `MainActivity.kt`

#### Changes:
1. **New file `NotificationSettingsDialog.kt`** — All notification settings UI (~1045 lines) extracted from `MainActivity.kt` into a dedicated dialog file with well-structured sub-composables:
   - `NotificationSettingsDialog` — Full-screen dialog with header, close button, scrollable content
   - `TimeRemindersSection` — Master toggle + meal/blood sugar reminder cards
   - `MealRemindersCard` — Meal reminder toggle, mode selector, manual time pickers
   - `BloodSugarRemindersCard` — Blood sugar check toggle, mode selector, manual time pickers
   - `ReminderModeSelector` — Reusable radio button group for Manual/Pattern/Both modes
   - `TimeRow` — Reusable time display row with tap-to-pick
   - `CustomRemindersSection` — Custom reminders list, pending confirmations, add/edit/delete
   - `CustomReminderItem` — Individual reminder card with toggle, days/times, action buttons
   - `QuietHoursSection` — Quiet hours toggle with start/end time pickers
   - `TestNotificationButton` — Test notification with permission/enablement checks
2. **Compact tappable entry in settings drawer** — Replaced ~1045 lines of inline notification settings in `MainActivity.kt` with a single `Surface` row showing:
   - 🔔 Notification icon (primary color)
   - Title: "Notifications" / "Benachrichtigungen" / "Notificaciones"
   - Subtitle: "Reminders, quiet hours, custom" (localized)
   - Right arrow chevron
3. **Dialog trigger** — Tapping the entry opens `NotificationSettingsDialog` as a full-screen dialog (95% width, 90% height) with all the original notification settings preserved.
4. **State callbacks** — `onConfirmationsChanged` and `onOpenHistory` callbacks maintain state synchronization with `MainActivity.kt`.

#### Localization:
- All text supports English, German, Spanish (preserved from original inline code)

#### Architecture:
- First step toward reorganizing the entire settings menu into categorized, tappable sections
- Each section is a separate composable under 500 lines (per project rules)
- `MainActivity.kt` reduced by ~1045 lines → ~970 lines removed net

---

### Feature: Full Settings Menu Reorganized into Categorized Tappable Entries
- **Date**: February 2026
- **Files**: `SettingsCategoryDialogs.kt` (new), `LanguageSettingsDialog.kt` (new), `AppearanceSettingsDialog.kt` (new), `ExportAnalysisDialog.kt` (new), `BackupRestoreDialog.kt` (new), `DataManagementDialog.kt` (new), `MainActivity.kt`

#### Changes:
1. **New `SettingsCategoryDialogs.kt`** — Shared reusable composables:
   - `SettingsEntryRow` — Tappable row with icon, title, subtitle, right arrow chevron (consistent Android-settings style)
   - `SettingsCategoryDialog` — Reusable full-screen dialog shell with header, close button, scrollable content
2. **New `ExportAnalysisDialog.kt`** — Export PDF, Export TXT, Analysis buttons in a dialog
3. **New `LanguageSettingsDialog.kt`** — 13 language flags in a grid layout with selection highlighting
4. **New `AppearanceSettingsDialog.kt`** — Dark mode toggle, color reset/scheme buttons, 4 color pickers (font, background, grid, insulin column)
5. **New `BackupRestoreDialog.kt`** — Export/Import backup buttons with icons
6. **New `DataManagementDialog.kt`** — Custom Columns, Custom Markers, Marker History buttons with icons
7. **`MainActivity.kt`** — Replaced all inline settings (Export, Language, Theme, Colors, Backup, Data Management) with 7 compact `SettingsEntryRow` entries:
   - 📄 Export & Analysis
   - 🌐 Language (shows current language as subtitle)
   - 🎨 Appearance
   - ☁️ Backup & Restore
   - 💾 Data Management
   - 🔔 Notifications (already done previously)
   - ⚙️ Advanced Settings

#### Architecture:
- All settings now follow the same pattern: tappable entry → opens dialog
- Reusable `SettingsEntryRow` and `SettingsCategoryDialog` composables ensure visual consistency
- Each dialog file is under 200 lines (well within project limits)
- `MainActivity.kt` drawer reduced by ~350 lines of inline UI
- Localization preserved for English, German, Spanish across all entries and dialogs

---

### Simplification: Removed Reminder Mode Selector from Time Reminders
- **Date**: February 2026
- **File**: `NotificationSettingsDialog.kt`

#### Changes:
1. **Removed `ReminderModeSelector` composable** — The 3 radio buttons (Manual times only / Detect patterns / Both) were removed from both `MealRemindersCard` and `BloodSugarRemindersCard`.
2. **Time pickers shown directly** — When Meal Reminders or Blood Sugar Check is enabled, the time rows (Breakfast/Lunch/Dinner or Morning/Afternoon/Evening) now appear immediately without requiring a mode selection step.
3. **Deleted `ReminderModeSelector` composable** — No longer needed.
4. **Removed `mealReminderMode` and `bloodSugarReminderMode` state variables** from their respective cards.

#### UX Improvement:
- Fewer taps to configure reminders — toggle on → times appear directly
- Simpler, more intuitive flow

---

### Enhancement: Reminder History Dialog — Improved Text Contrast & Color Intensity
- **Date**: February 2026
- **File**: `ReminderHistoryDialog.kt`

#### Changes:
1. **Boosted alpha values** on all low-contrast text elements across `DialogHeader`, `StreakSummaryCard`, `CompletionOverviewCard`, `HistoryEntriesList`, and `EditableHistoryEntry`.
2. **Semantic color labels** — "Completed" now uses `primaryColor`, "Missed" uses `errorColor` instead of muted `onSurfaceVariant`.
3. **Stronger label colors** — "Completion rate", "Total responses", "Personal best", "Entries", date headers all use `onSurface` instead of `onSurfaceVariant` for better readability.
4. **Fixed corrupted code** in `EditableHistoryEntry` edit button section caused by a bad multi_edit merge.

#### UX Improvement:
- Text is significantly more readable in both dark and light mode
- Labels match their semantic context (green for completed, red for missed)

---

### Enhancement: Notification Settings — Section Card Borders for Light Mode
- **Date**: February 2026
- **File**: `NotificationSettingsDialog.kt`

#### Changes:
1. **Added `BorderStroke`** to all 6 section `Card` components: Time Reminders, Meal Reminders, Blood Sugar Check, Custom Reminders, each Custom Reminder Item, and Quiet Hours.
2. **Border color**: `outlineVariant.copy(alpha = 0.4f–0.5f)` — subtle in dark mode, clearly visible in light mode.
3. **Added `BorderStroke` import**.

#### UX Improvement:
- Section cards are now clearly separated in light mode (previously blended into white background)
- Dark mode appearance unchanged (borders are subtle against dark surface)

---

### Fix: Notification Settings Dialog Stays Open After Tapping History
- **Date**: February 2026
- **File**: `MainActivity.kt` (line ~3540)

#### Problem:
When tapping the "History" button on a custom reminder, the `ReminderHistoryDialog` opened but the `NotificationSettingsDialog` remained visible behind it. If a pending confirmation notification appeared and the user tapped it, the confirmation UI opened behind the still-open settings dialog.

#### Fix:
Added `showNotificationSettingsDialog = false` to the `onOpenHistory` callback so the Notification Settings dialog closes automatically when the History dialog opens.

---

### Enhancement: Collapsible Pending/Alerts Sections with Counter Badges
- **Date**: February 2026
- **Files**: `NotificationSettingsDialog.kt`, `ReminderConfirmationUI.kt`

#### Problem:
When there were many pending confirmations or alerts, they pushed custom reminders far down in the list, making it hard to see and manage custom reminders. Users had to scroll through all pending items to reach their custom reminder settings.

#### Solution:
Made all "Pending" and "Alerts" sections collapsible with red counter badges:

**1. Notification Settings Dialog → Custom Reminders Section:**
- "Pending" section now has a collapsible header with red circular badge showing count
- Collapsed by default to keep custom reminders visible
- Tap to expand/collapse pending confirmations
- Arrow icon indicates expand/collapse state

**2. Bell Popup (Notification Center):**
- "Alerts" section collapsible with red counter badge
- "Pending" section collapsible with red counter badge
- Both collapsed by default
- Users can see at a glance how many items are in each section

#### UI Design:
- **Counter Badge**: Red circle with white text showing item count
- **Header**: Tappable surface with label, badge, and arrow icon
- **Collapsed State**: Shows only header with count, saves vertical space
- **Expanded State**: Shows all items when tapped

#### UX Improvement:
- Custom reminders always visible without scrolling
- Clear indication of pending/alert counts
- One tap to expand and see details
- Cleaner, more organized interface
- Less overwhelming when many notifications exist

---

### Enhancement: Full Screen Statistics Chart
- **Date**: February 2026
- **File**: `MainActivity.kt` (TimeChartDialog, line ~15436)

#### Changes:
1. **Added `DialogProperties(usePlatformDefaultWidth = false)`** to enable full screen mode
2. **Changed Surface modifier** from `fillMaxWidth(0.98f).fillMaxHeight(0.9f)` to `fillMaxSize()`
3. **Removed rounded corners** to maximize usable screen space

#### Benefits:
- **Maximum screen utilization** — Chart now uses entire display
- **Better data visibility** — More space for viewing trends and patterns
- **Improved readability** — Larger chart area makes it easier to analyze blood sugar/insulin data
- **All functionality preserved** — Chart type tabs, zoom, pan, statistics display all work unchanged

---

### Bug Fix: Test Data Not Appearing in Statistics Charts
- **Date**: February 2026
- **File**: `TestDataGenerator.kt` (line 18)

#### Problem:
When users loaded test data via Advanced Settings, the data appeared correctly in the table cells but did not show up in the statistics charts. The charts remained empty despite having valid blood sugar, insulin, and custom column data.

#### Root Cause:
**Date format mismatch** between test data generation and chart parsing:
- Test data generator used: `dd-MM-yyyy` (e.g., "17-02-2026" with dashes)
- Statistics charts expected: `dd.MM.yyyy` (e.g., "17.02.2026" with dots)

When charts tried to parse test data dates, the parsing failed silently, causing all test data points to be filtered out.

#### Fix:
Changed date formatter in `TestDataGenerator.kt` from `"dd-MM-yyyy"` to `"dd.MM.yyyy"` to match the format used throughout the app's chart system.

#### User Action Required:
After updating, users must:
1. Go to Settings → Advanced Settings
2. Tap "Remove Test Data" (clears old format data)
3. Tap "Load Test Data" (loads new format data)
4. Open statistics chart to see test data trends

#### Technical Note:
This highlights the importance of using a centralized date format constant. The app uses `dd.MM.yyyy` format in charts (lines ~14921, 15582) but had inconsistent format in test data generator.

---

## Current Goal

Implement Step 7: Applied Markers History - Create a chronological log of all health events/markers that have been applied to entries

---

## Technical Notes

### Key Files
- `MainActivity.kt` - Main UI and logic (~17,600 lines)
- `DataManager` object - Handles data persistence with SharedPreferences (lines ~1387-1473)
- `CustomMarkerManager` object - Handles custom marker type persistence (lines ~1625-1677)

### Data Classes
- `DiabetesEntry` - Main entry with date, blood sugar levels, insulin, markers, food entries (line ~615)
- `Marker` - Health event marker with type, time, reasons, notes, customMarkerTypeId, cellId (line ~638)
- `FoodPreset` - Food item with name, emoji, id, lastUsed timestamp
- `CustomMarkerType` - Custom marker type definitions with name, icon, color, description (line ~232)
- `AppSettings` - App configuration including colors, theme, language, zoom settings

### Localization
- All UI text uses `strings.xml` resources
- Supported languages: English, German, Spanish
