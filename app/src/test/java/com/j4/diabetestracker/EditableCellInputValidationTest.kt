package com.j4.diabetestracker

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EditableCellInputValidationTest {

    @Test
    fun glucoseInput_allowsTwoAndThreeDigits_andRejectsFourthDigit() {
        assertEquals(
            "12",
            normalizeEditableCellValue(TextFieldValue("12"), "number", maxDigitCount = 3)?.text
        )
        assertEquals(
            "123",
            normalizeEditableCellValue(TextFieldValue("123"), "number", maxDigitCount = 3)?.text
        )
        assertNull(
            normalizeEditableCellValue(TextFieldValue("1234"), "number", maxDigitCount = 3)
        )
    }

    @Test
    fun insulinInput_allowsOneAndTwoDigits_andRejectsThirdDigit() {
        assertEquals(
            "1",
            normalizeEditableCellValue(TextFieldValue("1"), "number", maxDigitCount = 2)?.text
        )
        assertEquals(
            "12",
            normalizeEditableCellValue(TextFieldValue("12"), "number", maxDigitCount = 2)?.text
        )
        assertNull(
            normalizeEditableCellValue(TextFieldValue("123"), "number", maxDigitCount = 2)
        )
    }

    @Test
    fun decimalSeparators_doNotCountAsDigits() {
        assertEquals(
            "12.3",
            normalizeEditableCellValue(TextFieldValue("12.3"), "number", maxDigitCount = 3)?.text
        )
        assertNull(
            normalizeEditableCellValue(TextFieldValue("12.34"), "number", maxDigitCount = 3)
        )
        assertEquals(
            "1.2",
            normalizeEditableCellValue(TextFieldValue("1,2"), "number", maxDigitCount = 2)?.text
        )
        assertNull(
            normalizeEditableCellValue(TextFieldValue("1.23"), "number", maxDigitCount = 2)
        )
    }

    @Test
    fun overlongPasteAndSelectionReplacement_areRejectedAsWholeProposal() {
        // BasicTextField supplies pasted and selection-replacement text as one proposed value.
        assertNull(
            normalizeEditableCellValue(
                TextFieldValue("1234", selection = TextRange(4)),
                "number",
                maxDigitCount = 3
            )
        )
        assertNull(
            normalizeEditableCellValue(
                TextFieldValue("1293", selection = TextRange(3)),
                "number",
                maxDigitCount = 3
            )
        )
    }

    @Test
    fun existingValues_canBeReplacedAndDeletedWithinTheLimit() {
        assertEquals(
            "193",
            normalizeEditableCellValue(
                TextFieldValue("193", selection = TextRange(2)),
                "number",
                maxDigitCount = 3
            )?.text
        )
        // Deleting a digit from a pre-existing overlong value remains possible.
        assertEquals(
            "123",
            normalizeEditableCellValue(
                TextFieldValue("123", selection = TextRange(3)),
                "number",
                maxDigitCount = 3
            )?.text
        )
    }

    @Test
    fun decimalFormattingAndSelection_arePreserved_andOtherNumericFieldsStayUnlimited() {
        val normalized = normalizeEditableCellValue(
            TextFieldValue("1, 2", selection = TextRange(4)),
            "number",
            maxDigitCount = 3
        )

        assertEquals("1.2", normalized?.text)
        assertEquals(TextRange(3), normalized?.selection)
        assertEquals(
            "1234",
            normalizeEditableCellValue(TextFieldValue("1234"), "number")?.text
        )
    }
}
