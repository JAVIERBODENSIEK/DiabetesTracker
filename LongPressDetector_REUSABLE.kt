/**
 * REUSABLE LONG PRESS DETECTOR FOR JETPACK COMPOSE
 * 
 * This file contains a generic, reusable long-press detection system that can be used
 * in any Jetpack Compose project. Copy this entire file to your project and use it
 * with any composable that needs to distinguish between short taps and long presses.
 * 
 * Author: Windsurf AI
 * Date: November 2025
 * License: Free to use in any project
 */

package com.yourpackage.components  // Change this to your package name

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.*
import kotlinx.coroutines.delay

/**
 * A composable that wraps content and detects long press gestures.
 * 
 * Features:
 * - Distinguishes between short taps (< threshold) and long presses (≥ threshold)
 * - Prevents child components from receiving events during long press
 * - Customizable press duration threshold
 * - Optional visual feedback during press detection
 * - Blocks text fields and other interactive elements during gesture detection
 * 
 * @param modifier Modifier to be applied to the wrapper Box
 * @param longPressDurationMs Duration in milliseconds to trigger long press (default: 1000ms)
 * @param onShortPress Callback invoked when user releases before threshold (short tap)
 * @param onLongPress Callback invoked when user holds for threshold duration (long press)
 * @param enabled Whether the detector is active (default: true)
 * @param consumeShortPress If true, prevents children from receiving short press events (default: false)
 * @param content The composable content to wrap
 * 
 * Example usage:
 * ```
 * LongPressDetector(
 *     onShortPress = { println("Short tap!") },
 *     onLongPress = { println("Long press!") }
 * ) {
 *     Text("Press me!")
 * }
 * ```
 */
@Composable
fun LongPressDetector(
    modifier: Modifier = Modifier,
    longPressDurationMs: Long = 1000L,
    onShortPress: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    enabled: Boolean = true,
    consumeShortPress: Boolean = false,
    content: @Composable (isDetectingGesture: Boolean) -> Unit
) {
    var pressStartTime by remember { mutableLongStateOf(0L) }
    var isLongPressHandled by remember { mutableStateOf(false) }
    var isPressing by remember { mutableStateOf(false) }
    
    // LaunchedEffect to check for long press
    LaunchedEffect(isPressing) {
        if (isPressing && enabled) {
            delay(longPressDurationMs)
            if (isPressing && !isLongPressHandled) {
                // Long press detected!
                isLongPressHandled = true
                onLongPress?.invoke()
            }
        }
    }
    
    Box(
        modifier = modifier
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                
                awaitPointerEventScope {
                    while (true) {
                        // Intercept at Initial pass to get events before children
                        val down = awaitPointerEvent(PointerEventPass.Initial)
                        
                        if (down.type == PointerEventType.Press) {
                            pressStartTime = System.currentTimeMillis()
                            isLongPressHandled = false
                            isPressing = true
                            
                            var isPressed = true
                            
                            // Monitor events while pressed
                            while (isPressed) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                
                                when (event.type) {
                                    PointerEventType.Release -> {
                                        isPressed = false
                                        isPressing = false
                                        
                                        val pressDuration = System.currentTimeMillis() - pressStartTime
                                        
                                        // If it was a long press, consume the release to prevent click
                                        if (isLongPressHandled) {
                                            event.changes.forEach { it.consume() }
                                        }
                                        // If short press
                                        else if (pressDuration < longPressDurationMs) {
                                            onShortPress?.invoke()
                                            if (consumeShortPress) {
                                                event.changes.forEach { it.consume() }
                                            }
                                        }
                                    }
                                    PointerEventType.Move -> {
                                        // If long press was handled, consume move events
                                        if (isLongPressHandled) {
                                            event.changes.forEach { it.consume() }
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
    ) {
        // Pass the gesture detection state to children
        // Children can use this to disable themselves during gesture detection
        content(isPressing)
    }
}

/**
 * ADVANCED VERSION: LongPressDetectorWithInteractionControl
 * 
 * This version provides more control over child interactions during gesture detection.
 * Use this when you need to block text fields or other interactive elements.
 * 
 * @param allowChildInteraction State that controls whether children can receive interactions
 * @param onGestureStateChanged Callback invoked when gesture detection state changes
 * 
 * Example usage with TextField:
 * ```
 * var allowInteraction by remember { mutableStateOf(true) }
 * 
 * LongPressDetectorWithInteractionControl(
 *     allowChildInteraction = allowInteraction,
 *     onGestureStateChanged = { isDetecting ->
 *         allowInteraction = !isDetecting
 *     },
 *     onLongPress = { println("Long press!") }
 * ) { isDetecting ->
 *     BasicTextField(
 *         value = text,
 *         onValueChange = { text = it },
 *         enabled = allowInteraction  // Controlled by detector
 *     )
 * }
 * ```
 */
@Composable
fun LongPressDetectorWithInteractionControl(
    modifier: Modifier = Modifier,
    longPressDurationMs: Long = 1000L,
    onShortPress: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    enabled: Boolean = true,
    allowChildInteraction: Boolean = true,
    onGestureStateChanged: ((isDetecting: Boolean) -> Unit)? = null,
    content: @Composable (isDetectingGesture: Boolean, allowInteraction: Boolean) -> Unit
) {
    var pressStartTime by remember { mutableLongStateOf(0L) }
    var isLongPressHandled by remember { mutableStateOf(false) }
    var isPressing by remember { mutableStateOf(false) }
    var internalAllowInteraction by remember { mutableStateOf(true) }
    
    // Notify parent of gesture state changes
    LaunchedEffect(isPressing) {
        onGestureStateChanged?.invoke(isPressing)
        if (isPressing) {
            internalAllowInteraction = false
        }
    }
    
    // LaunchedEffect to check for long press
    LaunchedEffect(isPressing) {
        if (isPressing && enabled) {
            delay(longPressDurationMs)
            if (isPressing && !isLongPressHandled) {
                // Long press detected!
                isLongPressHandled = true
                onLongPress?.invoke()
            }
        }
    }
    
    // Re-enable interaction after gesture completes
    LaunchedEffect(isPressing, isLongPressHandled) {
        if (!isPressing && !isLongPressHandled) {
            delay(100)
            internalAllowInteraction = true
        }
    }
    
    Box(
        modifier = modifier
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent(PointerEventPass.Initial)
                        
                        if (down.type == PointerEventType.Press) {
                            pressStartTime = System.currentTimeMillis()
                            isLongPressHandled = false
                            isPressing = true
                            internalAllowInteraction = false
                            
                            var isPressed = true
                            
                            while (isPressed) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                
                                when (event.type) {
                                    PointerEventType.Release -> {
                                        isPressed = false
                                        isPressing = false
                                        
                                        val pressDuration = System.currentTimeMillis() - pressStartTime
                                        
                                        if (isLongPressHandled) {
                                            event.changes.forEach { it.consume() }
                                        } else if (pressDuration < longPressDurationMs) {
                                            onShortPress?.invoke()
                                            internalAllowInteraction = true
                                        }
                                    }
                                    PointerEventType.Move -> {
                                        if (isLongPressHandled) {
                                            event.changes.forEach { it.consume() }
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
    ) {
        content(isPressing, internalAllowInteraction && allowChildInteraction)
    }
}

/**
 * USAGE EXAMPLES
 * ==============
 * 
 * Example 1: Simple Button with Long Press
 * -----------------------------------------
 * ```
 * LongPressDetector(
 *     onShortPress = { println("Quick tap - normal action") },
 *     onLongPress = { println("Long press - special action") }
 * ) { isDetecting ->
 *     Button(
 *         onClick = { /* This won't fire if long press is detected */ }
 *     ) {
 *         Text("Press me!")
 *     }
 * }
 * ```
 * 
 * Example 2: TextField with Long Press Menu
 * ------------------------------------------
 * ```
 * var text by remember { mutableStateOf("") }
 * var showMenu by remember { mutableStateOf(false) }
 * var allowInteraction by remember { mutableStateOf(true) }
 * 
 * LongPressDetectorWithInteractionControl(
 *     allowChildInteraction = allowInteraction,
 *     onGestureStateChanged = { isDetecting ->
 *         allowInteraction = !isDetecting
 *     },
 *     onShortPress = { /* Normal text editing */ },
 *     onLongPress = { showMenu = true }
 * ) { isDetecting, canInteract ->
 *     BasicTextField(
 *         value = text,
 *         onValueChange = { text = it },
 *         enabled = canInteract
 *     )
 * }
 * 
 * if (showMenu) {
 *     ContextMenu(onDismiss = { 
 *         showMenu = false
 *         allowInteraction = true
 *     })
 * }
 * ```
 * 
 * Example 3: Custom Duration and Visual Feedback
 * -----------------------------------------------
 * ```
 * LongPressDetector(
 *     longPressDurationMs = 500L,  // Faster trigger
 *     onLongPress = { println("Half second press!") }
 * ) { isDetecting ->
 *     Box(
 *         modifier = Modifier
 *             .size(100.dp)
 *             .background(
 *                 if (isDetecting) Color.Yellow else Color.Blue
 *             )
 *     ) {
 *         Text("Hold me")
 *     }
 * }
 * ```
 * 
 * Example 4: List Item with Context Menu
 * ---------------------------------------
 * ```
 * LazyColumn {
 *     items(listItems) { item ->
 *         LongPressDetector(
 *             onShortPress = { onItemClick(item) },
 *             onLongPress = { showContextMenu(item) }
 *         ) { isDetecting ->
 *             ListItem(
 *                 text = { Text(item.name) },
 *                 modifier = Modifier.alpha(if (isDetecting) 0.7f else 1f)
 *             )
 *         }
 *     }
 * }
 * ```
 * 
 * Example 5: Disable Detector Conditionally
 * ------------------------------------------
 * ```
 * var isEditMode by remember { mutableStateOf(false) }
 * 
 * LongPressDetector(
 *     enabled = !isEditMode,  // Disable long press in edit mode
 *     onLongPress = { println("Long press!") }
 * ) { isDetecting ->
 *     Text("Content")
 * }
 * ```
 */

/**
 * INTEGRATION GUIDE
 * =================
 * 
 * 1. Copy this entire file to your project
 * 2. Change the package name at the top
 * 3. Use LongPressDetector or LongPressDetectorWithInteractionControl
 * 4. Wrap your content in the detector
 * 5. Handle onShortPress and onLongPress callbacks
 * 
 * Key Concepts:
 * -------------
 * - The detector intercepts touch events at the Initial pass
 * - Children receive events normally for short taps
 * - Long press events are consumed to prevent child activation
 * - The isDetectingGesture parameter lets children react to detection state
 * - Use allowChildInteraction for fine-grained control over child components
 * 
 * Performance Notes:
 * ------------------
 * - Minimal overhead - only active during touch events
 * - No continuous polling or timers
 * - Efficient coroutine-based detection
 * - Properly cancels detection on release
 * 
 * Customization:
 * --------------
 * - Adjust longPressDurationMs for faster/slower detection
 * - Use consumeShortPress to block all child interactions
 * - Add visual feedback using isDetectingGesture state
 * - Combine with other modifiers for complex behaviors
 */
