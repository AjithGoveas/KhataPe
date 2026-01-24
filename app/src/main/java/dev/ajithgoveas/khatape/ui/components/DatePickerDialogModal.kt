package dev.ajithgoveas.khatape.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class TransactionSchedule(
    val dateMillis: Long,
    val includeTime: Boolean = false, // Future toggle for notifications
    val reminderEnabled: Boolean = false // Future toggle for local notifications
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhataDatePicker(
    initialDateMillis: Long = System.currentTimeMillis(),
    onConfirm: (TransactionSchedule) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )

    // Default Material 3 formatter
    val dateFormatter = remember { DatePickerDefaults.dateFormatter() }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onConfirm(TransactionSchedule(dateMillis = millis))
                    }
                    onDismiss()
                },
                enabled = datePickerState.selectedDateMillis != null
            ) {
                Text("Set Date", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp)
    ) {
        Column {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                title = null,
                headline = {
                    // Fixed the TODO() with proper parameter passing
                    DatePickerDefaults.DatePickerHeadline(
                        selectedDateMillis = datePickerState.selectedDateMillis,
                        displayMode = datePickerState.displayMode,
                        dateFormatter = dateFormatter,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }
            )

            /* FUTURE EXTENSION AREA:
               Since this is inside the DatePickerDialog's Column,
               you can add a Row with a Switch here later:

               Row(modifier = Modifier.padding(16.dp)) {
                   Text("Remind me later")
                   Switch(checked = reminderEnabled, onCheckedChange = { ... })
               }
            */
        }
    }
}