package dev.ajithgoveas.khatape.ui.screen.editExpense

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.ajithgoveas.khatape.ui.components.ErrorState
import dev.ajithgoveas.khatape.ui.components.KhataDatePicker
import dev.ajithgoveas.khatape.ui.components.KhataPeAppTopBar
import dev.ajithgoveas.khatape.ui.components.LoadingState
import dev.ajithgoveas.khatape.ui.screen.addExpense.AmountField
import dev.ajithgoveas.khatape.ui.screen.addExpense.DirectionToggleCard
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
fun EditExpenseScreen(
    expenseId: Long,
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: EditExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(expenseId) { viewModel.setTransactionId(expenseId) }

    // Preload form when data is ready
    LaunchedEffect(uiState.friend, uiState.amount) {
        if (uiState.friend != null && uiState.amount.isNotBlank()) {
            viewModel.preloadForm()
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            KhataPeAppTopBar(
                title = "Edit Expense",
                subtitle = uiState.friend?.let { "Updating entry for ${it.name}" } ?: "Edit Entry",
                emoji = "✏️",
                onBackClick = { navController.popBackStack() },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                uiState.isLoading && uiState.friend == null -> LoadingState(
                    modifier = Modifier.fillMaxSize(),
                    loadingText = "Loading friend details..."
                )

                uiState.friend == null -> ErrorState(
                    modifier = Modifier.fillMaxSize(),
                    errorText = "Error loading friend!!!"
                )

                else -> {
                    EditExpenseContent(
                        uiState = uiState,
                        formState = formState,
                        onEvent = viewModel::onEvent,
                        onSave = {
                            viewModel.onEvent(ExpenseEvent.SaveClicked)
                            navController.popBackStack()
                        },
                        onCancel = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditExpenseContent(
    uiState: ExpenseUiState,
    formState: FormState,
    onEvent: (ExpenseEvent) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        KhataDatePicker(
            initialDateMillis = formState.timestamp,
            onConfirm = { schedule ->
                onEvent(ExpenseEvent.TimestampChanged(schedule.dateMillis))
            },
            onDismiss = { showDatePicker = false }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Transaction Direction (Segmented UI)
        item {
            DirectionToggleCard(
                selectedDirection = formState.direction,
                onDirectionChange = { onEvent(ExpenseEvent.DirectionChanged(it)) }
            )
        }

        // Main Input Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AmountField(
                        value = formState.amount,
                        onValueChange = { onEvent(ExpenseEvent.AmountChanged(it)) },
                        isError = formState.amountError != null
                    )

                    OutlinedTextField(
                        value = formState.description,
                        onValueChange = { onEvent(ExpenseEvent.DescriptionChanged(it)) },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Compact Date Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Transaction Date", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = Instant.ofEpochMilli(formState.timestamp)
                                    .atZone(ZoneId.systemDefault())
                                    .format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = formState.amount.isNotBlank() && !formState.isSaving
                ) {
                    if (formState.isSaving) CircularProgressIndicator(Modifier.size(24.dp))
                    else Text("Update Expense", style = MaterialTheme.typography.titleMedium)
                }

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}