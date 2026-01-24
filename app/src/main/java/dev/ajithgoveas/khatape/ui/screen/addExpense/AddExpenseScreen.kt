package dev.ajithgoveas.khatape.ui.screen.addExpense

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.ajithgoveas.khatape.domain.model.TransactionDirection
import dev.ajithgoveas.khatape.ui.components.AvatarIcon
import dev.ajithgoveas.khatape.ui.components.ErrorState
import dev.ajithgoveas.khatape.ui.components.KhataDatePicker
import dev.ajithgoveas.khatape.ui.components.KhataPeAppTopBar
import dev.ajithgoveas.khatape.ui.components.LoadingState
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
fun AddExpenseScreen(
    friendId: Long,
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackBarHost = remember { SnackbarHostState() }

    LaunchedEffect(friendId) { viewModel.setFriendId(friendId) }

    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        KhataPeAppTopBar(
            title = "Add Expense",
            subtitle = uiState.friend?.let { "Transaction with ${it.name}" } ?: "New Entry",
            emoji = "💸",
            onBackClick = { navController.popBackStack() },
            scrollBehavior = scrollBehavior
        )
    }, snackbarHost = { SnackbarHost(snackBarHost) }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                uiState.isLoading && uiState.friend == null -> LoadingState(
                    loadingText = "Loading friend details...", modifier = Modifier.fillMaxSize()
                )

                uiState.friend == null -> ErrorState(
                    modifier = Modifier.fillMaxSize(), errorText = "Error loading friend details!!!"
                )

                else -> {
                    ExpenseContent(
                        uiState = uiState,
                        formState = formState,
                        onEvent = viewModel::onEvent,
                        onSave = {
                            viewModel.onEvent(AddExpenseEvent.SaveClicked)
                            navController.popBackStack()
                        },
                        onCancel = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun ExpenseContent(
    uiState: AddExpenseUiState,
    formState: FormState,
    onEvent: (AddExpenseEvent) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    // Using your new extensible DatePicker
    if (showDatePicker) {
        KhataDatePicker(initialDateMillis = formState.timestamp, onConfirm = { schedule ->
            onEvent(AddExpenseEvent.TimestampChanged(schedule.dateMillis))
        }, onDismiss = { showDatePicker = false })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Transaction Type Toggle (Segmented look)
        item {
            DirectionToggleCard(
                selectedDirection = formState.direction,
                onDirectionChange = { onEvent(AddExpenseEvent.DirectionChanged(it)) })
        }

        // 2. Main Form Card
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
                    // Amount with Large Text
                    AmountField(
                        value = formState.amount,
                        onValueChange = { onEvent(AddExpenseEvent.AmountChanged(it)) },
                        isError = formState.amountError != null
                    )

                    OutlinedTextField(
                        value = formState.description,
                        onValueChange = { onEvent(AddExpenseEvent.DescriptionChanged(it)) },
                        label = { Text("What was this for?") },
                        placeholder = { Text("e.g. Dinner, Movie tickets") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Compact Date Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Date", style = MaterialTheme.typography.labelSmall)
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

        // 3. Action Buttons
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
                    else Text("Save Transaction", style = MaterialTheme.typography.titleMedium)
                }

                TextButton(
                    onClick = onCancel, modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun AmountField(value: String, onValueChange: (String) -> Unit, isError: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        prefix = { Text("₹ ", style = MaterialTheme.typography.headlineMedium) },
        textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        placeholder = { Text("0", style = MaterialTheme.typography.headlineMedium) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        isError = isError,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    )
}

@Composable
fun DirectionToggleCard(
    selectedDirection: TransactionDirection, onDirectionChange: (TransactionDirection) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(4.dp)
    ) {
        val directions = listOf(
            TransactionDirection.CREDIT to "You Paid", TransactionDirection.DEBIT to "They Paid"
        )

        directions.forEach { (dir, label) ->
            val isSelected = selectedDirection == dir
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onDirectionChange(dir) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}