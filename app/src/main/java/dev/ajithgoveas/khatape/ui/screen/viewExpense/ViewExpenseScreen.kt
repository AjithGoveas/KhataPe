package dev.ajithgoveas.khatape.ui.screen.viewExpense

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    LaunchedEffect(expenseId) { viewModel.setExpenseId(expenseId) }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is ViewExpenseSideEffect.NavigateBack -> navController.popBackStack()
                is ViewExpenseSideEffect.ShowError -> snackBarHostState.showSnackbar(effect.message)
                is ViewExpenseSideEffect.NavigateToEdit -> {
                    navController.navigate(Route.EditExpense(effect.expenseId))
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            KhataPeAppTopBar(
                title = "Transaction",
                subtitle = uiState.transaction?.description?.ifBlank { "Expense Details" }
                    ?: "Details",
                emoji = "🧾",
                onBackClick = { navController.popBackStack() },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { viewModel.onEvent(ViewExpenseEvent.EditClicked) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                uiState.isLoading -> LoadingState(
                    modifier = Modifier.fillMaxSize(),
                    loadingText = "Loading details..."
                )

                uiState.isError -> ErrorState(
                    modifier = Modifier.fillMaxSize(),
                    errorText = "Error loading details!!!"
                )

                uiState.transaction != null -> {
                    ExpenseDetailsContent(
                        uiState = uiState,
                        onDelete = { viewModel.onEvent(ViewExpenseEvent.DeleteClicked) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseDetailsContent(
    uiState: ViewExpenseUiState,
    onDelete: () -> Unit
) {
    val txn = uiState.transaction!!
    val friend = uiState.friend!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Main Receipt Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AvatarIcon(name = friend.name, modifier = Modifier.size(64.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(friend.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = if (txn.direction == TransactionDirection.CREDIT) "You paid" else "They paid",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "₹${txn.amount}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = if (txn.direction == TransactionDirection.CREDIT)
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Metadata Grid
                DetailItem(
                    Icons.Default.Notes,
                    "Description",
                    txn.description.ifBlank { "No description provided" })
                DetailItem(
                    Icons.Default.Event,
                    "Date",
                    Instant.ofEpochMilli(txn.timestamp).atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy"))
                )
                if (txn.dueDate != null) {
                    DetailItem(
                        Icons.Default.Timer,
                        "Due Date",
                        Instant.ofEpochMilli(txn.dueDate).atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Delete Action
        OutlinedButton(
            onClick = onDelete,
            enabled = !uiState.isDeleting,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (uiState.isDeleting) CircularProgressIndicator(Modifier.size(24.dp))
            else {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(8.dp))
                Text("Remove Transaction", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}