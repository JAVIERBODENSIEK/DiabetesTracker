#!/usr/bin/env python3
import re

# Read the file
with open('app/src/main/java/com/j4/diabetestracker/MainActivity.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Pattern to find (the code without title)
pattern = r'        val \(chartData, chartLabel, yAxisLabel\) = getChartData\(validEntries, chartType, context\)\n        drawChart\(canvas, chartData, chartLabel, yAxisLabel\)'

# Replacement (the code with title)
replacement = '''        val (chartData, chartLabel, yAxisLabel) = getChartData(validEntries, chartType, context)
        
        // Draw title
        val titlePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 18f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Diabetes Tracker - $chartLabel", 50f, 50f, titlePaint)
        
        drawChart(canvas, chartData, chartLabel, yAxisLabel)'''

# Replace all occurrences
content = re.sub(pattern, replacement, content)

# Write back
with open('app/src/main/java/com/j4/diabetestracker/MainActivity.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print("Chart titles added successfully!")
