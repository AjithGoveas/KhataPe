package dev.ajithgoveas.khatape.ui.screen.addExpense

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.ajithgoveas.khatape.ui.components.DirectionToggleCard
import dev.ajithgoveas.khatape.ui.components.ErrorState
import dev.ajithgoveas.khatape.ui.components.KhataDatePicker
import dev.ajithgoveas.khatape.ui.components.KhataPeAppTopBar
import dev.ajithgoveas.khatape.ui.components.LoadingState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
            subtitle = uiState.friend?.let { "Recording entry for ${it.name}" } ?: "New Entry",
            emoji = "💸",
            onBackClick = { navController.popBackStack() },
            scrollBehavior = scrollBehavior
        )
    }, snackbarHost = { SnackbarHost(snackBarHost) }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when {
                uiState.isLoading && uiState.friend == null -> LoadingState(modifier = Modifier.fillMaxSize())
                uiState.friend == null -> ErrorState(
                    modifier = Modifier.fillMaxSize(), "Error loading khata. Try again!"
                )

                else -> {
                    ExpenseContent(formState = formState, onEvent = viewModel::onEvent, onSave = {
                        viewModel.onEvent(AddExpenseEvent.SaveClicked)
                        navController.popBackStack()
                    }, onCancel = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun ExpenseContent(
    formState: FormState,
    onEvent: (AddExpenseEvent) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        KhataDatePicker(initialDateMillis = formState.timestamp, onConfirm = { schedule ->
            onEvent(AddExpenseEvent.TimestampChanged(schedule.dateMillis))
            showDatePicker = false
        }, onDismiss = { showDatePicker = false })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Transaction Type Toggle (Premium Segmented Control)
        item {
            DirectionToggleCard(
                selectedDirection = formState.direction,
                onDirectionChange = { onEvent(AddExpenseEvent.DirectionChanged(it)) })
        }

        // 2. Main Input Card
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
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

                    AmountField(
                        value = formState.amount,
                        onValueChange = { onEvent(AddExpenseEvent.AmountChanged(it)) },
                        isError = formState.amountError != null
                    )

                    Spacer(Modifier.height(24.dp))

                    // Description Input
                    OutlinedTextField(
                        value = formState.description,
                        onValueChange = { onEvent(AddExpenseEvent.DescriptionChanged(it)) },
                        label = { Text("What was this for?") },
                        placeholder = { Text("Dinner, Shopping, etc.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    // Tappable Date Surface
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
                                    "Transaction Date",
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

        // 3. Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    enabled = formState.amount.isNotBlank() && !formState.isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1B5E20) // Consistent Success Green
                    )
                ) {
                    if (formState.isSaving) {
                        CircularProgressIndicator(
                            Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "SAVE TRANSACTION", fontWeight = FontWeight.Black, letterSpacing = 1.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(0.3f))
                ) {
                    Text(
                        "CANCEL",
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold
                    )
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
        prefix = {
            Text(
                "₹ ",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.primary
            )
        },
        textStyle = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.Black, letterSpacing = (-1).sp
        ),
        placeholder = {
            Text(
                "0.0",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        isError = isError,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    )
}