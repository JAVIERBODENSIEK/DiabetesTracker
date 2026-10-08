# 🚀 Long Press Detector - Quick Reference

## 📦 Installation (3 Steps)

1. **Copy** `LongPressDetector_REUSABLE.kt` to your project
2. **Change** package name: `package com.yourpackage.components`
3. **Import** and use: `import com.yourpackage.components.LongPressDetector`

---

## ⚡ Quick Start

```kotlin
LongPressDetector(
    onShortPress = { /* Quick tap action */ },
    onLongPress = { /* Long press action */ }
) { isDetecting ->
    Text("Press me!")
}
```

---

## 🎯 Choose Your Version

### Basic Version
**Use for:** Buttons, list items, images, cards

```kotlin
LongPressDetector(
    onShortPress = { /* ... */ },
    onLongPress = { /* ... */ }
) { isDetecting ->
    // Your content
}
```

### Advanced Version
**Use for:** TextFields, forms, complex interactions

```kotlin
var allowInteraction by remember { mutableStateOf(true) }

LongPressDetectorWithInteractionControl(
    allowChildInteraction = allowInteraction,
    onGestureStateChanged = { isDetecting ->
        allowInteraction = !isDetecting
    },
    onLongPress = { /* ... */ }
) { isDetecting, canInteract ->
    BasicTextField(
        value = text,
        onValueChange = { text = it },
        enabled = canInteract  // ← Important!
    )
}
```

---

## 📋 Common Patterns

### Pattern 1: List Item with Menu
```kotlin
LongPressDetector(
    onShortPress = { onItemClick(item) },
    onLongPress = { showMenu(item) }
) { isDetecting ->
    ListItem(
        text = { Text(item.name) },
        modifier = Modifier.alpha(if (isDetecting) 0.7f else 1f)
    )
}
```

### Pattern 2: TextField with Context Menu
```kotlin
var allowInteraction by remember { mutableStateOf(true) }

LongPressDetectorWithInteractionControl(
    allowChildInteraction = allowInteraction,
    onGestureStateChanged = { allowInteraction = !it },
    onLongPress = { showContextMenu = true }
) { _, canInteract ->
    BasicTextField(
        value = text,
        onValueChange = { text = it },
        enabled = canInteract
    )
}
```

### Pattern 3: Image with Delete
```kotlin
LongPressDetector(
    longPressDurationMs = 800L,  // Faster
    onShortPress = { viewImage() },
    onLongPress = { showDeleteDialog = true }
) { isDetecting ->
    AsyncImage(
        model = imageUrl,
        modifier = Modifier.scale(if (isDetecting) 0.95f else 1f)
    )
}
```

---

## ⚙️ Parameters

| Parameter | Type | Default | Use Case |
|-----------|------|---------|----------|
| `longPressDurationMs` | Long | 1000 | Adjust speed: 500 (fast), 1500 (slow) |
| `enabled` | Boolean | true | Disable in edit mode |
| `consumeShortPress` | Boolean | false | Block all child interactions |
| `allowChildInteraction` | Boolean | true | External control (advanced) |

---

## 🎨 Visual Feedback

```kotlin
LongPressDetector(onLongPress = { /* ... */ }) { isDetecting ->
    Box(
        modifier = Modifier
            // Choose one or combine:
            .background(if (isDetecting) Color.Yellow else Color.Blue)
            .scale(if (isDetecting) 0.95f else 1f)
            .alpha(if (isDetecting) 0.7f else 1f)
            .border(
                width = if (isDetecting) 3.dp else 1.dp,
                color = if (isDetecting) Color.Blue else Color.Gray
            )
    )
}
```

---

## 🐛 Common Issues & Fixes

### Issue: TextField activates during long press
```kotlin
// ✅ Fix: Control enabled parameter
BasicTextField(
    enabled = canInteract,  // From detector
    // ...
)
```

### Issue: Both actions fire
```kotlin
// ❌ Wrong
LongPressDetector(onLongPress = { /* ... */ }) {
    Button(onClick = { /* ... */ }) { }  // Remove onClick
}

// ✅ Correct
LongPressDetector(
    onShortPress = { /* ... */ },
    onLongPress = { /* ... */ }
) {
    Button(onClick = {}) { }  // Empty or remove
}
```

### Issue: Too slow/fast
```kotlin
LongPressDetector(
    longPressDurationMs = 500L,  // Faster
    // or
    longPressDurationMs = 1500L,  // Slower
)
```

---

## 📊 Decision Tree

```
Do you need long press detection?
│
├─ YES → Is it a TextField or form input?
│        │
│        ├─ YES → Use LongPressDetectorWithInteractionControl
│        │        + Set enabled = canInteract
│        │
│        └─ NO → Use basic LongPressDetector
│
└─ NO → Use normal onClick/clickable
```

---

## ✅ Checklist

Before deploying:
- [ ] Package name updated
- [ ] TextField has `enabled = canInteract`
- [ ] Visual feedback implemented
- [ ] Tested on real device
- [ ] Duration feels right (default 1s)
- [ ] Dialogs re-enable interaction on dismiss

---

## 🎯 Real-World Use Cases

| Use Case | Version | Duration | Pattern |
|----------|---------|----------|---------|
| List item menu | Basic | 1000ms | Alpha fade |
| Image delete | Basic | 800ms | Scale down |
| TextField menu | Advanced | 1000ms | Border highlight |
| Chat message actions | Basic | 1000ms | Background color |
| Table cell edit | Advanced | 1000ms | Border + disable |
| Card options | Basic | 1000ms | Elevation |

---

## 💡 Pro Tips

1. **Duration:** 1000ms is standard, 800ms for images, 1200ms for destructive actions
2. **Feedback:** Always provide visual feedback using `isDetecting`
3. **TextFields:** Always use advanced version + control `enabled`
4. **Dialogs:** Re-enable interaction when dialog closes
5. **Testing:** Test on real device, emulator touch is different
6. **Haptics:** Add `performHapticFeedback()` for better UX

---

## 📱 Copy-Paste Templates

### Template 1: Simple Button
```kotlin
LongPressDetector(
    onShortPress = { /* TODO */ },
    onLongPress = { /* TODO */ }
) { isDetecting ->
    Button(onClick = {}) {
        Text("Button")
    }
}
```

### Template 2: TextField
```kotlin
var text by remember { mutableStateOf("") }
var allowInteraction by remember { mutableStateOf(true) }

LongPressDetectorWithInteractionControl(
    allowChildInteraction = allowInteraction,
    onGestureStateChanged = { allowInteraction = !it },
    onLongPress = { /* TODO: Show menu */ }
) { _, canInteract ->
    BasicTextField(
        value = text,
        onValueChange = { text = it },
        enabled = canInteract
    )
}
```

### Template 3: List Item
```kotlin
LongPressDetector(
    onShortPress = { /* TODO: Navigate */ },
    onLongPress = { /* TODO: Show menu */ }
) { isDetecting ->
    ListItem(
        headlineContent = { Text("Item") },
        modifier = Modifier.alpha(if (isDetecting) 0.7f else 1f)
    )
}
```

---

## 🔗 Files

- `LongPressDetector_REUSABLE.kt` - Main component (copy this)
- `LongPressDetector_GUIDE.md` - Full documentation
- `LongPressDetector_QUICKREF.md` - This file

---

## 🎓 Learn More

See `LongPressDetector_GUIDE.md` for:
- Detailed examples
- Advanced techniques
- Performance tips
- Troubleshooting
- Technical deep dive

---

**Quick Reference v1.0** | Created by Windsurf AI
