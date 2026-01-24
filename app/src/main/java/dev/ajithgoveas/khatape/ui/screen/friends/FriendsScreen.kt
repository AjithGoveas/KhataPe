package dev.ajithgoveas.khatape.ui.screen.friends

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.ajithgoveas.khatape.domain.model.FriendSummary
import dev.ajithgoveas.khatape.ui.components.AvatarIcon
import dev.ajithgoveas.khatape.ui.components.CreateKhataDialog
import dev.ajithgoveas.khatape.ui.components.KhataPeAppTopBar
import dev.ajithgoveas.khatape.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    friendsViewModel: FriendsViewModel = hiltViewModel(),
    createFriendViewModel: CreateFriendViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) } // State for dropdown
    var currentSort by remember { mutableStateOf(FriendSort.NAME) }
    var currentFilter by remember { mutableStateOf(FriendFilter.ALL) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val summaries by friendsViewModel.friends.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    // Logic: Apply Filter and Sort locally
    val processedSummaries = remember(summaries, currentSort, currentFilter) {
        val filtered = summaries.filter {
            when (currentFilter) {
                FriendFilter.SETTLED -> it.totalDebit == 0.0 && it.totalCredit == 0.0
                FriendFilter.PENDING -> it.totalDebit > 0 || it.totalCredit > 0
                FriendFilter.ALL -> true
            }
        }

        // Apply sorting
        when (currentSort) {
            FriendSort.NAME -> filtered.sortedBy { it.name }
            FriendSort.HIGHEST_DEBT -> filtered.sortedByDescending { it.totalDebit }
            FriendSort.HIGHEST_CREDIT -> filtered.sortedByDescending { it.totalCredit }
        }
    }

    LaunchedEffect(Unit) {
        createFriendViewModel.sideEffects.collect { effect ->
            when (effect) {
                is CreateFriendSideEffect.FriendCreated -> {
                    navController.navigate(Route.FriendDetail(effect.id))
                }

                is CreateFriendSideEffect.ShowError -> snackBarHostState.showSnackbar(effect.message)
            }
        }
    }

    if (showDialog) {
        CreateKhataDialog(onDismiss = { showDialog = false }, onConfirm = { name ->
            showDialog = false
            createFriendViewModel.create(name)
        })
    }

    Scaffold(
        topBar = {
            KhataPeAppTopBar(
                title = "Khata List",
                subtitle = "Track who owes what.",
                emoji = "📒",
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter and Sort")
                    }
                    FriendsActionMenu(
                        expanded = showMenu,
                        onDismiss = { showMenu = false },
                        onSortChange = { currentSort = it },
                        onFilterChange = { currentFilter = it })
                })
        },
        floatingActionButton = { AddKhataFAB(onClick = { showDialog = true }) },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackBarHostState) }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (processedSummaries.isEmpty()) {
                item { EmptyState() }
            } else {
                items(processedSummaries) { summary ->
                    FriendCard(summary = summary) {
                        navController.navigate(
                            Route.FriendDetail(friendId = summary.friendId)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Text(
        "No friends yet. Add one to get started!",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun FriendCard(
    summary: FriendSummary,
    onClick: () -> Unit
) {
    // Calculate net balance for visual cues
    val netBalance = summary.totalCredit - summary.totalDebit

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarIcon(name = summary.name, modifier = Modifier.size(48.dp))

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when {
                        netBalance > 0 -> "Owes you ₹${netBalance.toInt()}"
                        netBalance < 0 -> "You owe ₹${(-netBalance).toInt()}"
                        else -> "Settled up"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        netBalance > 0 -> MaterialTheme.colorScheme.primary
                        netBalance < 0 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun AddKhataFAB(onClick: () -> Unit, modifier: Modifier = Modifier) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier.padding(16.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add new Khata")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add Khata")
    }
}

@Composable
private fun FriendsActionMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSortChange: (FriendSort) -> Unit,
    onFilterChange: (FriendFilter) -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        Text(
            "Sort By",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        DropdownMenuItem(
            text = { Text("Name") },
            onClick = { onSortChange(FriendSort.NAME); onDismiss() },
            leadingIcon = { Icon(Icons.Default.SortByAlpha, null) })
        DropdownMenuItem(
            text = { Text("Highest Debt") },
            onClick = { onSortChange(FriendSort.HIGHEST_DEBT); onDismiss() },
            leadingIcon = { Icon(Icons.Default.ArrowDownward, null) })

        HorizontalDivider()

        Text(
            "Filter",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        DropdownMenuItem(
            text = { Text("Show All") },
            onClick = { onFilterChange(FriendFilter.ALL); onDismiss() })
        DropdownMenuItem(
            text = { Text("Pending Only") },
            onClick = { onFilterChange(FriendFilter.PENDING); onDismiss() })
    }
}