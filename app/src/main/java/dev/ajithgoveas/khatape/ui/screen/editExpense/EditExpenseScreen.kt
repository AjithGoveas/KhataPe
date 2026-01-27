package dev.ajithgoveas.khatape.ui.screen.editExpense

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.ajithgoveas.khatape.ui.components.DirectionToggleCard
import dev.ajithgoveas.khatape.ui.components.ErrorState
import dev.ajithgoveas.khatape.ui.components.KhataDatePicker
import dev.ajithgoveas.khatape.ui.components.KhataPeAppTopBar
import dev.ajithgoveas.khatape.ui.components.LoadingState
import dev.ajithgoveas.khatape.ui.screen.addExpense.AmountField
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

    // Side Effects Handler
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is EditExpenseSideEffect.SaveSuccess -> navController.popBackStack()
                is EditExpenseSideEffect.Cancelled -> navController.popBackStack()
                is EditExpenseSideEffect.ShowError -> snackBarHostState.showSnackbar(effect.message)
            }
        }
    }

    // Preload form when initial data is fetched from DB
    LaunchedEffect(uiState.friend, uiState.amount) {
        if (uiState.friend != null && uiState.amount.isNotBlank()) {
            viewModel.preloadForm()
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            KhataPeAppTopBar(
                title = "Edit Entry",
                subtitle = uiState.friend?.let { "Updating ${it.name}'s Khata" }
                    ?: "Modify Transaction",
                emoji = "📝",
                onBackClick = { viewModel.onEvent(ExpenseEvent.CancelClicked) },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when {
                uiState.isLoading && uiState.friend == null -> LoadingState(modifier = Modifier.fillMaxSize())
                uiState.isError -> ErrorState(
                    modifier = Modifier.fillMaxSize(),
                    "Error loading details. Try again!"
                )

                else -> {
                    EditExpenseContent(
                        formState = formState,
                        onEvent = viewModel::onEvent
                    )
                }
            }
        }
    }
}

@Composable
private fun EditExpenseContent(
    formState: FormState,
    onEvent: (ExpenseEvent) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        KhataDatePicker(
            initialDateMillis = formState.timestamp,
            onConfirm = { schedule ->
                onEvent(ExpenseEvent.TimestampChanged(schedule.dateMillis))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Direction Switcher (Mirroring the "You Gave / You Got" logic)
        item {
            DirectionToggleCard(
                selectedDirection = formState.direction,
                onDirectionChange = { onEvent(ExpenseEvent.DirectionChanged(it)) }
            )
        }

        // 2. Main Input Card
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "TRANSACTION DETAILS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.outline,
                        letterSpacing = 1.sp
                    )

                    Spacer(Modifier.height(20.dp))

                    // Reusing your AmountField component
                    AmountField(
                        value = formState.amount,
                        onValueChange = { onEvent(ExpenseEvent.AmountChanged(it)) },
                        isError = formState.amountError != null
                    )

                    if (formState.amountError != null) {
                        Text(
                            text = formState.amountError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Modern Description Field
                    OutlinedTextField(
                        value = formState.description,
                        onValueChange = { onEvent(ExpenseEvent.DescriptionChanged(it)) },
                        label = { Text("What was this for?") },
                        placeholder = { Text("e.g. Dinner, Rent, Movie") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Default.EditNote, null) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    // Date Selector Surface
                    Surface(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(0.1f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    null,
                                    modifier = Modifier.padding(10.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Entry Date",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = Instant.ofEpochMilli(formState.timestamp)
                                        .atZone(ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Persistent Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onEvent(ExpenseEvent.SaveClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    enabled = formState.amount.isNotBlank() && !formState.isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1B5E20), // Forest Green for success/save
                        contentColor = Color.White
                    )
                ) {
                    if (formState.isSaving) {
                        CircularProgressIndicator(
                            Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("UPDATE ENTRY", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }

                OutlinedButton(
                    onClick = { onEvent(ExpenseEvent.CancelClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(0.3f))
                ) {
                    Text(
                        "DISCARD CHANGES",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}