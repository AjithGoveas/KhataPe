package dev.ajithgoveas.khatape.ui.screen.viewExpense

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import dev.ajithgoveas.khatape.domain.model.TransactionDirection
import dev.ajithgoveas.khatape.ui.components.AvatarIcon
import dev.ajithgoveas.khatape.ui.components.ErrorState
import dev.ajithgoveas.khatape.ui.components.KhataPeAppTopBar
import dev.ajithgoveas.khatape.ui.components.LoadingState
import dev.ajithgoveas.khatape.ui.navigation.Route
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewExpenseScreen(
    expenseId: Long,
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ViewExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackBarHostState = remember { SnackbarHostState() }
    var showSuccessOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(expenseId) { viewModel.setExpenseId(expenseId) }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is ViewExpenseSideEffect.NavigateBack -> navController.popBackStack()
                is ViewExpenseSideEffect.ShowError -> snackBarHostState.showSnackbar(effect.message)
                is ViewExpenseSideEffect.NavigateToEdit -> navController.navigate(
                    Route.EditExpense(
                        effect.expenseId
                    )
                )

                is ViewExpenseSideEffect.TransactionSettled -> showSuccessOverlay = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                KhataPeAppTopBar(
                    title = "Transaction Details",
                    subtitle = uiState.transaction?.description?.ifBlank { "Statement Entry" }
                        ?: "Details",
                    emoji = "🧾",
                    onBackClick = { navController.popBackStack() },
                    scrollBehavior = scrollBehavior,
                    actions = {
                        IconButton(onClick = { viewModel.onEvent(ViewExpenseEvent.EditClicked) }) {
                            Icon(Icons.Default.Edit, "Edit")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackBarHostState) }
        ) { innerPadding ->
            Box(modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()) {
                when {
                    uiState.isLoading -> LoadingState(modifier = Modifier.fillMaxSize())
                    uiState.isError -> ErrorState(
                        modifier = Modifier.fillMaxSize(),
                        "Error loading details. Try again!"
                    )

                    uiState.transaction != null && uiState.friend != null -> {
                        ExpenseDetailsContent(
                            uiState = uiState,
                            onDelete = { viewModel.onEvent(ViewExpenseEvent.DeleteClicked) },
                            onSettle = { viewModel.onEvent(ViewExpenseEvent.SettleClicked) }
                        )
                    }
                }
            }
        }

        SettledSuccessOverlay(
            isVisible = showSuccessOverlay,
            onFinished = {
                showSuccessOverlay = false
                navController.popBackStack()
            }
        )
    }
}

@Composable
private fun ExpenseDetailsContent(
    uiState: ViewExpenseUiState,
    onDelete: () -> Unit,
    onSettle: () -> Unit
) {
    val txn = uiState.transaction!!
    val friend = uiState.friend!!
    val isCredit = txn.direction == TransactionDirection.CREDIT
    val accentColor = if (isCredit) Color(0xFF2E7D32) else Color(0xFFC62828)
    val containerBg = if (isCredit) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Transaction Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with status ring
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(0.1f),
                    modifier = Modifier.size(80.dp),
                    border = BorderStroke(2.dp, accentColor.copy(0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AvatarIcon(name = friend.name, modifier = Modifier.size(80.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = friend.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )

                // Direction Label Badge
                Surface(
                    color = containerBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = if (isCredit) "YOU PAID" else "THEY PAID",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Huge Amount Display
                Text(
                    text = "₹${txn.amount.toInt()}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )

                Spacer(Modifier.height(32.dp))

                // Details List
                DetailRow(
                    Icons.AutoMirrored.Filled.Notes,
                    "Remarks",
                    txn.description.ifBlank { "No remarks added" })
                DetailRow(
                    Icons.Default.Event,
                    "Transaction Date",
                    Instant.ofEpochMilli(txn.timestamp).atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"))
                )

                txn.dueDate?.let {
                    DetailRow(
                        Icons.Default.Timer,
                        "Expected Settlement",
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    )
                }

                // Settled Status Banner
                AnimatedVisibility(visible = txn.isSettled) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "FULLY SETTLED",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Actions
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!txn.isSettled) {
                Button(
                    onClick = onSettle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                ) {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text("MARK AS SETTLED", fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = onDelete,
                enabled = !uiState.isDeleting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(0.3f))
            ) {
                if (uiState.isDeleting) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "DELETE ENTRY",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.padding(8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}