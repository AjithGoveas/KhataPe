package dev.ajithgoveas.khatape.ui.screen.editExpense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.ajithgoveas.khatape.domain.model.TransactionDirection
import dev.ajithgoveas.khatape.ui.components.AvatarIcon
import dev.ajithgoveas.khatape.ui.components.DatePickerDialogModal
import dev.ajithgoveas.khatape.ui.components.ErrorState
import dev.ajithgoveas.khatape.ui.components.LoadingState
import dev.ajithgoveas.khatape.ui.navigation.Screen
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/*
@Composable
fun FriendContextCard(friend: Friend, friendSummary: FriendSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(friend.name, style = MaterialTheme.typography.titleLarge)
            Text(
                text = buildString {
                    val owesYou =
                        friendSummary.totalCredit - friendSummary.totalDebit
                    when {
                        owesYou > 0 -> append("${friend.name} owes you ₹${"%.2f".format(owesYou)}")
                        owesYou < 0 -> append("You owe ₹${"%.2f".format(-owesYou)}")
                        else -> append("Settled up")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditExpenseForm(
    uiState: ExpenseUiState,
    formState: FormState,
    onEvent: (ExpenseEvent) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var showDateToday by remember { mutableStateOf(false) }
    var showDateDue by remember { mutableStateOf(false) }

    if (showDateToday) {
        DatePickerDialogModal(
            initialDateMillis = formState.timestamp,
            onDateSelected = { millis ->
                onEvent(ExpenseEvent.TimestampChanged(millis ?: formState.timestamp))
            },
            onDismiss = { showDateToday = false }
        )
    }

    if (showDateDue) {
        DatePickerDialogModal(
            initialDateMillis = formState.dueDate ?: System.currentTimeMillis(),
            onDateSelected = { millis ->
                if (millis != null && millis >= formState.timestamp) {
                    onEvent(ExpenseEvent.DueDateChanged(millis))
                } else {
                    onEvent(ExpenseEvent.Error("Due date cannot be before transaction date"))
                }
            },
            onDismiss = { showDateDue = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Friend header
        uiState.friend?.let { friend ->
            FriendHeader(friend.name)
        }

        SectionTitle("Expense Details")

        // Amount field
        OutlinedTextField(
            value = formState.amount,
            onValueChange = { onEvent(ExpenseEvent.AmountChanged(it)) },
            label = { Text("Amount (₹)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = formState.amountError != null,
            supportingText = {
                formState.amountError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            trailingIcon = {
                if (formState.amountError != null) {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        DirectionToggle(
            selectedDirection = formState.direction,
            onDirectionChange = { onEvent(ExpenseEvent.DirectionChanged(it)) }
        )

        // Description
        OutlinedTextField(
            value = formState.description,
            onValueChange = { onEvent(ExpenseEvent.DescriptionChanged(it)) },
            label = { Text("Description (Optional)") },
            singleLine = false,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        // Date pickers
        DateButton(
            label = "Transaction Date",
            dateMillis = formState.timestamp,
            onClick = { showDateToday = true }
        )

        DateButton(
            label = "Due Date",
            dateMillis = formState.dueDate,
            onClick = { showDateDue = true }
        )

        // Action buttons
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onSave,
                enabled = formState.amount.isNotBlank() &&
                        formState.amountError == null &&
                        !formState.isSaving,
                modifier = Modifier.weight(1f)
            ) {
                if (formState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Expense")
                }
            }

            OutlinedButton(
                onClick = onCancel,
                enabled = !formState.isSaving,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun FriendHeader(name: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AvatarIcon(name = name)
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Friend",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Column {
        HorizontalDivider(thickness = 2.dp, modifier = Modifier.padding(vertical = 8.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun DateButton(
    label: String,
    dateMillis: Long?,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            Icons.Filled.CalendarToday,
            contentDescription = label,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(
            "$label: ${
                dateMillis?.let {
                    Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE)
                } ?: "Not set"
            }"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DirectionToggle(
    selectedDirection: TransactionDirection,
    onDirectionChange: (TransactionDirection) -> Unit
) {
    Column {
        Text(
            text = "Who paid?",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedDirection == TransactionDirection.CREDIT,
                onClick = { onDirectionChange(TransactionDirection.CREDIT) },
                label = { Text("You Paid") }
            )
            FilterChip(
                selected = selectedDirection == TransactionDirection.DEBIT,
                onClick = { onDirectionChange(TransactionDirection.DEBIT) },
                label = { Text("They Paid") }
            )
        }
    }
}

@Composable
fun EditExpenseScreen(
    expenseId: Long,
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: EditExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(expenseId) {
        viewModel.setTransactionId(expenseId)
    }

    // Preload form once when transaction data is ready
    LaunchedEffect(uiState.friend, uiState.amount) {
        if (uiState.friend != null && uiState.amount.isNotBlank()) {
            viewModel.preloadForm()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect {
            when (it) {
                is EditExpenseSideEffect.Cancelled -> {
                    snackBarHostState.showSnackbar("Edit cancelled")
                }

                is EditExpenseSideEffect.ShowError -> {
                    snackBarHostState.showSnackbar(it.message)
                }

                is EditExpenseSideEffect.SaveSuccess -> {
                    snackBarHostState.showSnackbar("Expense updated successfully")
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(
                start = innerPadding.calculateStartPadding(layoutDirection),
                end = innerPadding.calculateEndPadding(layoutDirection),
                bottom = innerPadding.calculateBottomPadding()
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)

        when {
            uiState.isLoading && uiState.friend == null -> LoadingState(
                modifier = contentModifier,
                loadingText = "Loading expense details..."
            )

            uiState.friend == null -> ErrorState(
                modifier = contentModifier,
                errorText = "Error loading expense details."
            )

            else -> ExpenseContent(
                uiState = uiState,
                formState = formState,
                modifier = contentModifier,
                onEvent = viewModel::onEvent,
                onSave = onSave@{
                    viewModel.onEvent(ExpenseEvent.SaveClicked)
                    val friendId = uiState.friend?.id ?: return@onSave
                    navController.navigate(Screen.FriendsScreens.FriendDetail.createRoute(friendId)) {
                        popUpTo(Screen.FriendsScreens.FriendDetail.route) { inclusive = true }
                    }
                },
                onCancel = {
                    viewModel.onEvent(ExpenseEvent.CancelClicked)
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun ExpenseContent(
    uiState: ExpenseUiState,
    formState: FormState,
    modifier: Modifier,
    onEvent: (ExpenseEvent) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            ScreenHeader("✏️ Edit Expense")
        }
        item {
            EditExpenseForm(
                uiState = uiState,
                formState = formState,
                onEvent = onEvent,
                onSave = onSave,
                onCancel = onCancel
            )
        }
        item {
            InfoNote(
                text = "Transactions are auto-marked as settled when balances reach zero."
            )
        }
    }
}

@Composable
private fun ScreenHeader(title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(thickness = 2.dp, modifier = Modifier.padding(vertical = 4.dp))
    }
}

@Composable
private fun InfoNote(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    )
}