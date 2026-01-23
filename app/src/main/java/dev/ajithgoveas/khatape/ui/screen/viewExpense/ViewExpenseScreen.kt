package dev.ajithgoveas.khatape.ui.screen.viewExpense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import dev.ajithgoveas.khatape.ui.components.AvatarIcon
import dev.ajithgoveas.khatape.ui.components.ErrorState
import dev.ajithgoveas.khatape.ui.components.LoadingState
import dev.ajithgoveas.khatape.ui.navigation.Screen
import kotlinx.coroutines.launch
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
    LaunchedEffect(expenseId) {
        viewModel.setExpenseId(expenseId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val layoutDirection = LocalLayoutDirection.current

    // Collect side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is ViewExpenseSideEffect.NavigateBack -> navController.popBackStack()
                is ViewExpenseSideEffect.ShowError -> {
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(
                            message = effect.message,
                            withDismissAction = true
                        )
                    }
                }

                is ViewExpenseSideEffect.NavigateToEdit -> {
                    navController.navigate(Screen.FriendsScreens.EditExpense.createRoute(effect.expenseId))
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = modifier
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(
                start = innerPadding.calculateStartPadding(layoutDirection),
                end = innerPadding.calculateEndPadding(layoutDirection),
                bottom = innerPadding.calculateBottomPadding()
            )
            .padding(horizontal = 16.dp, vertical = 24.dp)

        when {
            uiState.isLoading -> LoadingState(
                modifier = contentModifier,
                loadingText = "Loading expense..."
            )

            uiState.isError -> ErrorState(
                modifier = contentModifier,
                errorText = "Error loading expense."
            )

            uiState.transaction != null && uiState.friend != null -> {
                ExpenseDetailsContent(
                    uiState = uiState,
                    onDelete = { viewModel.onEvent(ViewExpenseEvent.DeleteClicked) },
                    onEdit = { viewModel.onEvent(ViewExpenseEvent.EditClicked) },
                    modifier = contentModifier
                )
            }
        }
    }
}

@Composable
private fun ExpenseDetailsContent(
    uiState: ViewExpenseUiState,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier
) {
    val transaction = uiState.transaction ?: return
    val friend = uiState.friend ?: return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Text(
            "Expense Details",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(thickness = 2.dp)

        // Friend info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AvatarIcon(name = friend.name)
            Column {
                Text(
                    text = friend.name,
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

        // Amount
        Text(
            text = "₹${transaction.amount}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        HorizontalDivider(thickness = 1.dp)

        // Transaction details
        InfoRow("Description", transaction.description.ifEmpty { "N/A" })
        InfoRow("Direction", transaction.direction.name)

        val localDate = Instant.ofEpochMilli(transaction.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        InfoRow("Date", localDate)

        val dueDate = transaction.dueDate?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        } ?: "N/A"
        InfoRow("Due Date", dueDate)

        // Action buttons
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onEdit,
                enabled = !uiState.isDeleting,
                modifier = Modifier.weight(1f)
            ) {
                Text("Edit")
            }
            Button(
                onClick = onDelete,
                enabled = !uiState.isDeleting,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}