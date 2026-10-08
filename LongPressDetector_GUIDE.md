# 📱 Long Press Detector - Complete Guide

A reusable, production-ready long-press detection system for Jetpack Compose that cleanly separates short taps from long presses.

---

## 🎯 What Problem Does This Solve?

In mobile apps, you often need different actions for:
- **Short tap** → Normal interaction (edit, select, navigate)
- **Long press** → Context menu, special action, delete confirmation

The challenge is preventing both actions from firing simultaneously. This component solves that problem elegantly.

---

## ✨ Features

- ✅ **Clean gesture separation** - No interference between short and long press
- ✅ **Blocks child interactions** during long press detection
- ✅ **Customizable duration** - Default 1 second, adjustable
- ✅ **TextField-friendly** - Prevents keyboard popup during long press
- ✅ **Visual feedback** - Exposes detection state to children
- ✅ **Zero dependencies** - Pure Jetpack Compose
- ✅ **Production-ready** - Tested and optimized

---

## 📦 Installation

### Step 1: Copy the File
Copy `LongPressDetector_REUSABLE.kt` to your project:
```
your-project/
  └── app/src/main/java/com/yourpackage/
      └── components/
          └── LongPressDetector.kt
```

### Step 2: Update Package Name
```kotlin
package com.yourpackage.components  // Change to your package
```

### Step 3: Start Using!
```kotlin
import com.yourpackage.components.LongPressDetector
```

---

## 🚀 Quick Start

### Basic Example
```kotlin
@Composable
fun MyScreen() {
    LongPressDetector(
        onShortPress = { 
            println("Quick tap!") 
        },
        onLongPress = { 
            println("Long press!") 
        }
    ) { isDetecting ->
        Text(
            text = "Press me!",
            modifier = Modifier
                .padding(16.dp)
                .background(if (isDetecting) Color.Yellow else Color.Blue)
        )
    }
}
```

**Result:**
- Tap quickly → "Quick tap!" printed
- Hold for 1 second → "Long press!" printed
- Background changes to yellow while detecting

---

## 📚 Real-World Examples

### Example 1: List Item with Context Menu
```kotlin
@Composable
fun TodoListItem(
    todo: Todo,
    onEdit: (Todo) -> Unit,
    onShowMenu: (Todo) -> Unit
) {
    LongPressDetector(
        onShortPress = { onEdit(todo) },
        onLongPress = { onShowMenu(todo) }
    ) { isDetecting ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .alpha(if (isDetecting) 0.7f else 1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.completed,
                onCheckedChange = { /* ... */ }
            )
            Text(
                text = todo.title,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
```

**Behavior:**
- **Short tap** → Edit todo
- **Long press** → Show context menu (delete, share, etc.)
- **Visual feedback** → Item fades while detecting

---

### Example 2: TextField with Long Press Menu
```kotlin
@Composable
fun SmartTextField() {
    var text by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var allowInteraction by remember { mutableStateOf(true) }
    
    LongPressDetectorWithInteractionControl(
        allowChildInteraction = allowInteraction,
        onGestureStateChanged = { isDetecting ->
            allowInteraction = !isDetecting
        },
        onShortPress = { 
            // Normal editing - do nothing, let TextField handle it
        },
        onLongPress = { 
            showMenu = true 
        }
    ) { isDetecting, canInteract ->
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            enabled = canInteract,  // ← Key: Controlled by detector
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, Color.Gray)
        )
    }
    
    if (showMenu) {
        DropdownMenu(
            expanded = true,
            onDismissRequest = { 
                showMenu = false
                allowInteraction = true  // Re-enable after menu closes
            }
        ) {
            DropdownMenuItem(
                text = { Text("Copy") },
                onClick = { /* ... */ }
            )
            DropdownMenuItem(
                text = { Text("Paste") },
                onClick = { /* ... */ }
            )
            DropdownMenuItem(
                text = { Text("Clear") },
                onClick = { /* ... */ }
            )
        }
    }
}
```

**Behavior:**
- **Short tap** → Cursor appears, keyboard shows, normal editing
- **Long press** → Context menu appears, NO keyboard
- **Clean separation** → No interference between actions

---

### Example 3: Image Gallery with Delete
```kotlin
@Composable
fun ImageGalleryItem(
    imageUrl: String,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LongPressDetector(
        longPressDurationMs = 800L,  // Faster trigger for images
        onShortPress = onView,
        onLongPress = { showDeleteDialog = true }
    ) { isDetecting ->
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(if (isDetecting) 0.95f else 1f)  // Shrink while detecting
            )
            
            if (isDetecting) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    color = Color.White
                )
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Image?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
```

**Behavior:**
- **Short tap** → View full image
- **Hold 0.8s** → Delete confirmation dialog
- **Visual feedback** → Image shrinks + loading indicator

---

### Example 4: Chat Message with Actions
```kotlin
@Composable
fun ChatMessage(
    message: Message,
    onReply: () -> Unit,
    onShowActions: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }
    
    LongPressDetector(
        onShortPress = onReply,
        onLongPress = { showActions = true }
    ) { isDetecting ->
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (isDetecting) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else 
                MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
    
    if (showActions) {
        ModalBottomSheet(
            onDismissRequest = { showActions = false }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TextButton(onClick = { /* Reply */ }) {
                    Text("Reply")
                }
                TextButton(onClick = { /* Forward */ }) {
                    Text("Forward")
                }
                TextButton(onClick = { /* Copy */ }) {
                    Text("Copy")
                }
                TextButton(onClick = { /* Delete */ }) {
                    Text("Delete", color = Color.Red)
                }
            }
        }
    }
}
```

**Behavior:**
- **Short tap** → Reply to message
- **Long press** → Show action sheet (reply, forward, copy, delete)
- **Visual feedback** → Message background changes

---

### Example 5: Custom Table Cell (Like Your App!)
```kotlin
@Composable
fun EditableTableCell(
    value: String,
    onValueChange: (String) -> Unit,
    onAddMarker: () -> Unit
) {
    var allowInteraction by remember { mutableStateOf(true) }
    
    LongPressDetectorWithInteractionControl(
        allowChildInteraction = allowInteraction,
        onGestureStateChanged = { isDetecting ->
            allowInteraction = !isDetecting
        },
        onShortPress = { 
            // Normal editing
        },
        onLongPress = { 
            onAddMarker() 
        }
    ) { isDetecting, canInteract ->
        Surface(
            modifier = Modifier
                .size(80.dp, 50.dp)
                .border(
                    width = if (isDetecting) 2.dp else 1.dp,
                    color = if (isDetecting) Color.Blue else Color.Gray
                )
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = canInteract,
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }
}
```

**Behavior:**
- **Short tap** → Edit value, keyboard appears
- **Long press** → Add marker dialog, NO keyboard
- **Border feedback** → Thicker blue border while detecting

---

## ⚙️ Configuration Options

### Basic Version: `LongPressDetector`

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `modifier` | Modifier | `Modifier` | Applied to wrapper Box |
| `longPressDurationMs` | Long | `1000L` | Duration to trigger long press (ms) |
| `onShortPress` | `(() -> Unit)?` | `null` | Callback for short tap |
| `onLongPress` | `(() -> Unit)?` | `null` | Callback for long press |
| `enabled` | Boolean | `true` | Whether detector is active |
| `consumeShortPress` | Boolean | `false` | Block children from short press |
| `content` | `@Composable` | Required | Content with `isDetecting` param |

### Advanced Version: `LongPressDetectorWithInteractionControl`

Includes all basic parameters plus:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `allowChildInteraction` | Boolean | `true` | External control of child interaction |
| `onGestureStateChanged` | `((Boolean) -> Unit)?` | `null` | Notified when detection state changes |

---

## 🎨 Visual Feedback Patterns

### Pattern 1: Color Change
```kotlin
LongPressDetector(
    onLongPress = { /* ... */ }
) { isDetecting ->
    Box(
        modifier = Modifier.background(
            if (isDetecting) Color.Yellow else Color.Blue
        )
    )
}
```

### Pattern 2: Scale Animation
```kotlin
LongPressDetector(
    onLongPress = { /* ... */ }
) { isDetecting ->
    Box(
        modifier = Modifier.scale(
            if (isDetecting) 0.95f else 1f
        )
    )
}
```

### Pattern 3: Opacity
```kotlin
LongPressDetector(
    onLongPress = { /* ... */ }
) { isDetecting ->
    Box(
        modifier = Modifier.alpha(
            if (isDetecting) 0.7f else 1f
        )
    )
}
```

### Pattern 4: Border
```kotlin
LongPressDetector(
    onLongPress = { /* ... */ }
) { isDetecting ->
    Box(
        modifier = Modifier.border(
            width = if (isDetecting) 3.dp else 1.dp,
            color = if (isDetecting) Color.Blue else Color.Gray
        )
    )
}
```

### Pattern 5: Loading Indicator
```kotlin
LongPressDetector(
    onLongPress = { /* ... */ }
) { isDetecting ->
    Box {
        // Your content
        
        if (isDetecting) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
```

---

## 🔧 Advanced Techniques

### Technique 1: Conditional Enable/Disable
```kotlin
var isEditMode by remember { mutableStateOf(false) }

LongPressDetector(
    enabled = !isEditMode,  // Disable in edit mode
    onLongPress = { /* ... */ }
) { isDetecting ->
    // Content
}
```

### Technique 2: Custom Duration Per Item
```kotlin
LazyColumn {
    items(listItems) { item ->
        LongPressDetector(
            longPressDurationMs = if (item.isImportant) 500L else 1000L,
            onLongPress = { /* ... */ }
        ) { isDetecting ->
            // Item content
        }
    }
}
```

### Technique 3: Chaining Actions
```kotlin
var step by remember { mutableStateOf(0) }

LongPressDetector(
    onShortPress = { step = 1 },
    onLongPress = { step = 2 }
) { isDetecting ->
    when (step) {
        0 -> Text("Press me")
        1 -> Text("Short pressed! Press again")
        2 -> Text("Long pressed! Done")
    }
}
```

### Technique 4: Haptic Feedback
```kotlin
val view = LocalView.current

LongPressDetector(
    onLongPress = { 
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        // Your action
    }
) { isDetecting ->
    // Content
}
```

---

## 🐛 Troubleshooting

### Problem: Short tap still triggers child action during long press

**Solution:** Use `LongPressDetectorWithInteractionControl` and disable children:
```kotlin
var allowInteraction by remember { mutableStateOf(true) }

LongPressDetectorWithInteractionControl(
    allowChildInteraction = allowInteraction,
    onGestureStateChanged = { isDetecting ->
        allowInteraction = !isDetecting
    }
) { isDetecting, canInteract ->
    Button(
        onClick = { /* ... */ },
        enabled = canInteract  // ← Add this
    ) {
        Text("Button")
    }
}
```

---

### Problem: TextField activates during long press

**Solution:** Control `enabled` parameter:
```kotlin
LongPressDetectorWithInteractionControl(
    onLongPress = { /* ... */ }
) { isDetecting, canInteract ->
    BasicTextField(
        value = text,
        onValueChange = { text = it },
        enabled = canInteract  // ← Add this
    )
}
```

---

### Problem: Long press too slow/fast

**Solution:** Adjust duration:
```kotlin
LongPressDetector(
    longPressDurationMs = 500L,  // Faster (0.5s)
    // or
    longPressDurationMs = 1500L,  // Slower (1.5s)
    onLongPress = { /* ... */ }
)
```

---

### Problem: Both callbacks fire

**Solution:** Check that you're using the callbacks correctly:
```kotlin
// ✅ Correct
LongPressDetector(
    onShortPress = { println("Short") },
    onLongPress = { println("Long") }
)

// ❌ Wrong - don't add onClick to children
LongPressDetector(
    onLongPress = { println("Long") }
) {
    Button(onClick = { println("Short") }) {  // ← Remove this
        Text("Button")
    }
}
```

---

## 📊 Performance Considerations

### Memory
- ✅ Minimal state (4-5 variables)
- ✅ No heavy objects
- ✅ Properly cleaned up

### CPU
- ✅ Only active during touch
- ✅ No continuous polling
- ✅ Efficient coroutines

### Best Practices
```kotlin
// ✅ Good: Reuse detector for similar items
@Composable
fun MyList(items: List<Item>) {
    LazyColumn {
        items(items) { item ->
            LongPressDetector(
                onLongPress = { handleLongPress(item) }
            ) {
                ItemContent(item)
            }
        }
    }
}

// ❌ Bad: Creating new detector for each recomposition
@Composable
fun MyItem() {
    if (someCondition) {
        LongPressDetector { /* ... */ }
    } else {
        LongPressDetector { /* ... */ }
    }
}
```

---

## 🎓 How It Works (Technical Deep Dive)

### 1. Event Interception
```kotlin
.pointerInput(Unit) {
    awaitPointerEventScope {
        // Intercept at Initial pass - BEFORE children see events
        val event = awaitPointerEvent(PointerEventPass.Initial)
    }
}
```

### 2. Gesture Detection
```kotlin
LaunchedEffect(isPressing) {
    if (isPressing) {
        delay(1000)  // Wait for threshold
        if (isPressing) {
            onLongPress()  // Still pressing → long press!
        }
    }
}
```

### 3. Event Consumption
```kotlin
if (isLongPress) {
    event.changes.forEach { it.consume() }  // Block children
} else {
    // Let event pass through to children
}
```

### 4. State Management
```kotlin
Press → isPressing = true → Start timer
Release (< 1s) → onShortPress() → Allow children
Release (≥ 1s) → onLongPress() → Block children
```

---

## 📝 Summary

### When to Use Basic Version
- Simple buttons
- List items
- Cards
- Images
- Any non-interactive content

### When to Use Advanced Version
- TextFields
- Forms
- Complex interactive components
- When you need fine-grained control

### Key Takeaways
1. **Always** control `enabled` parameter for TextFields
2. **Use** `isDetecting` for visual feedback
3. **Adjust** `longPressDurationMs` for your use case
4. **Test** on real devices for best UX
5. **Remember** to re-enable interactions after dialogs close

---

## 🚀 Next Steps

1. Copy the reusable file to your project
2. Try the Quick Start example
3. Adapt one of the real-world examples
4. Customize for your needs
5. Share with your team!

---

## 📄 License

Free to use in any project, commercial or personal.

---

## 🙏 Credits

Created by Windsurf AI for the Diabetes Tracker project.
Extracted and generalized for community use.

---

**Happy Coding! 🎉**
