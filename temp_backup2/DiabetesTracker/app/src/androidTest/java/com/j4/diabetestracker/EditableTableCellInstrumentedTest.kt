package com.j4.diabetestracker

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Instrumented test for EditableTableCell component.
 * These tests run on an Android device or emulator.
 */
@RunWith(AndroidJUnit4::class)
class EditableTableCellInstrumentedTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testBasicInput() {
        // Initial test value
        val initialText = "123"
        val focusRequester = FocusRequester()
        
        // Set up the EditableTableCell in the test environment with initial value
        composeTestRule.setContent {
            EditableTableCell(
                value = TextFieldValue(initialText),
                onValueChange = { },
                hint = "Enter value",
                isDateCell = false,
                isFoodCell = false,
                fontColor = Color.Black,
                gridColor = Color.Gray,
                onDelete = null,
                isFocused = false,
                focusRequester = focusRequester,
                onRequestFocus = { },
                onDateClick = null,
                foodPresets = emptyList(),
                setFoodPresets = { },
                currentCellFoodItems = emptyList(),
                onCurrentCellFoodItemsChange = { }
            )
        }
        
        // Verify the TextField exists and is displayed
        composeTestRule.onNodeWithTag("EditableTextField")
            .assertExists()
            .assertIsDisplayed()
    }
    
    @Test
    fun testDateCell() {
        // Test date
        val today = LocalDate.now()
        val formattedDate = today.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val focusRequester = FocusRequester()
        
        // Set up the EditableTableCell with isDateCell=true
        composeTestRule.setContent {
            EditableTableCell(
                value = TextFieldValue(formattedDate),
                onValueChange = { },
                hint = "",
                isDateCell = true,
                isFoodCell = false,
                fontColor = Color.Black,
                gridColor = Color.Gray,
                onDelete = null,
                isFocused = false,
                focusRequester = focusRequester,
                onRequestFocus = { },
                onDateClick = { },
                foodPresets = emptyList(),
                setFoodPresets = { },
                currentCellFoodItems = emptyList(),
                onCurrentCellFoodItemsChange = { }
            )
        }
        
        // Verify the date is displayed
        composeTestRule.onNodeWithText(formattedDate)
            .assertExists()
    }
    
    @Test
    fun testFoodCell() {
        // Create a test food preset
        val testFood = FoodPreset("🍎", "Apple")
        val focusRequester = FocusRequester()
        
        // Set up the EditableTableCell with a food item
        composeTestRule.setContent {
            EditableTableCell(
                value = TextFieldValue(""),
                onValueChange = { },
                hint = "",
                isDateCell = false,
                isFoodCell = true,
                fontColor = Color.Black,
                gridColor = Color.Gray,
                onDelete = null,
                isFocused = false,
                focusRequester = focusRequester,
                onRequestFocus = { },
                onDateClick = null,
                foodPresets = emptyList(),
                setFoodPresets = { },
                currentCellFoodItems = listOf(testFood),
                onCurrentCellFoodItemsChange = { }
            )
        }
        
        // Verify the food count chip is displayed
        composeTestRule.onNodeWithTag("FoodCountChip")
            .assertExists()
            .assertIsDisplayed()
        
        // Verify the add food button is displayed
        composeTestRule.onNodeWithTag("AddFoodButton")
            .assertExists()
            .assertIsDisplayed()
    }
}
