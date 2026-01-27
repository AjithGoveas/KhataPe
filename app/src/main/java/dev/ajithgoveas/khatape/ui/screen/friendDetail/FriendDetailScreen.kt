package dev.ajithgoveas.khatape.ui.screen.friendDetail

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.ajithgoveas.khatape.domain.model.FriendSummary
import dev.ajithgoveas.khatape.domain.model.Transaction
import dev.ajithgoveas.khatape.domain.model.TransactionDirection
import dev.ajithgoveas.khatape.ui.components.ErrorState
import dev.ajithgoveas.khatape.ui.components.KhataPeAppTopBar
import dev.ajithgoveas.khatape.ui.components.LoadingState
import dev.ajithgoveas.khatape.ui.navigation.Route
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    modifier: Modifier = Modifier,
    friendId: Long,
    navController: NavController,
    viewModel: FriendDetailViewModel = hiltViewModel()
) {
    // Initialize data
    LaunchedEffect(friendId) { viewModel.setFriendId(friendId) }

    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackBarHostState = remember { SnackbarHostState() }

    // Handle Side Effects (Navigation & Errors)
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is FriendDetailSideEffect.NavigateBack -> navController.popBackStack()
                is FriendDetailSideEffect.ShowError -> snackBarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            KhataPeAppTopBar(
                title = uiState.friend?.name ?: "Loading...",
                subtitle = "Statement of Account",
                emoji = "👤",
                scrollBehavior = scrollBehavior,
                onBackClick = { navController.popBackStack() },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(FriendDetailEvent.DeleteClicked) },
                        enabled = !uiState.isDeleting // Disable while deleting
                    ) {
                        if (uiState.isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Delete,
                                "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Only show FAB if friend exists
            if (uiState.friend != null) {
                AddExpenseFAB {
                    navController.navigate(Route.AddExpense(friendId = friendId))
                }
            }
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
                uiState.isLoading -> LoadingState(modifier = Modifier.fillMaxSize())
                uiState.isError || uiState.friend == null -> ErrorState(
                    errorText = "Could not load this Khata.",
                    modifier = Modifier.fillMaxSize()
                )

                else -> {
                    TransactionList(
                        summary = uiState.friendSummary,
                        transactions = uiState.transactions,
                        navController = navController,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionList(
    summary: FriendSummary?, // Passed from ViewModel
    transactions: List<Transaction>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Use summary if available, otherwise fallback to list calculation
        item {
            BalanceSummaryHeader(
                totalCredit = summary?.totalCredit ?: 0.0,
                totalDebit = summary?.totalDebit ?: 0.0
            )
        }

        item {
            Text(
                text = "TRANSACTION HISTORY",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                letterSpacing = 1.2.sp
            )
        }

        if (transactions.isEmpty()) {
            item { EmptyState() }
        } else {
            items(transactions, key = { it.id }) { txn ->
                TransactionCard(txn = txn, navController = navController)
            }
        }
    }
}

@Composable
private fun BalanceSummaryHeader(totalCredit: Double, totalDebit: Double) {
    val netBalance = totalCredit - totalDebit
    val positiveColor = Color(0xFF1B5E20)
    val negativeColor = Color(0xFFB71C1C)
    val cardBg = when {
        netBalance > 0 -> Color(0xFFE8F5E9)
        netBalance < 0 -> Color(0xFFFFEBEE)
        else -> Color.Transparent
    }
    val borderColor = when {
        netBalance > 0 -> Color(0xFFE8F5E9)
        netBalance < 0 -> Color(0xFFFFEBEE)
        else -> Color.Gray
    }

    Surface(
        color = cardBg,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            borderColor.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (netBalance >= 0) "YOU PAID" else "THEY PAID",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = borderColor.copy(alpha = 0.7f)
            )
            Text(
                text = "₹${abs(netBalance).toInt()}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = borderColor
            )

            // Helpful breakdown
            Row(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    "Gave: ₹${totalCredit.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = positiveColor
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    "Got: ₹${totalDebit.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = negativeColor
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📒", fontSize = 48.sp)
        Text("No entries found", fontWeight = FontWeight.Bold)
        Text("Tap the button below to add an entry.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun AddExpenseFAB(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = Color(0xFFFF9800), // High-visibility Saffron
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        elevation = FloatingActionButtonDefaults.elevation(6.dp)
    ) {
        Icon(Icons.Default.Add, null)
        Spacer(Modifier.width(8.dp))
        Text("ADD NEW ENTRY", fontWeight = FontWeight.Black)
    }
}

@Composable
fun TransactionCard(txn: Transaction, navController: NavController) {
    val isCredit = txn.direction == TransactionDirection.CREDIT
    val accentColor = if (isCredit) Color(0xFF2E7D32) else Color(0xFFC62828)

    val formattedDate = Instant.ofEpochMilli(txn.timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))

    Surface(
        onClick = { navController.navigate(Route.ViewExpense(txn.id)) },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle Icon Indicator
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = txn.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${txn.amount.toInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
                Text(
                    text = if (isCredit) "You Paid" else "They Paid",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}