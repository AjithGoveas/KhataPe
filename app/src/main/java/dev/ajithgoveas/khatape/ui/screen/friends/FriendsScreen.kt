package dev.ajithgoveas.khatape.ui.screen.friends

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
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
    var showMenu by remember { mutableStateOf(false) }
    var currentSort by remember { mutableStateOf(FriendSort.NAME) }
    var currentFilter by remember { mutableStateOf(FriendFilter.ALL) }

    val summaries by friendsViewModel.friends.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackBarHostState = remember { SnackbarHostState() }

    // RESTORED: Listen for creation success to navigate
    LaunchedEffect(Unit) {
        createFriendViewModel.sideEffects.collect { effect ->
            when (effect) {
                is CreateFriendSideEffect.FriendCreated -> {
                    navController.navigate(Route.FriendDetail(effect.id))
                }

                is CreateFriendSideEffect.ShowError -> {
                    snackBarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    val processedSummaries = remember(summaries, currentSort, currentFilter) {
        summaries.filter {
            when (currentFilter) {
                FriendFilter.SETTLED -> it.totalDebit == 0.0 && it.totalCredit == 0.0
                FriendFilter.PENDING -> it.totalDebit > 0 || it.totalCredit > 0
                FriendFilter.ALL -> true
            }
        }.let { filtered ->
            when (currentSort) {
                FriendSort.NAME -> filtered.sortedBy { it.name }
                FriendSort.HIGHEST_DEBT -> filtered.sortedByDescending { it.totalDebit }
                FriendSort.HIGHEST_CREDIT -> filtered.sortedByDescending { it.totalCredit }
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            KhataPeAppTopBar(
                title = "Hisab-Kitab",
                subtitle = "Manage your active khatas",
                emoji = "📒",
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.FilterList,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Filter"
                        )
                    }
                    FriendsActionMenu(
                        expanded = showMenu,
                        onDismiss = { showMenu = false },
                        onSortChange = { currentSort = it },
                        onFilterChange = { currentFilter = it }
                    )
                }
            )
        },
        floatingActionButton = { AddKhataFAB(onClick = { showDialog = true }) },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = 100.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { KhataOverviewStrip(summaries) }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(4.dp, 16.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "YOUR KHATAS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            if (processedSummaries.isEmpty()) {
                item { EmptyState() }
            } else {
                items(processedSummaries, key = { it.friendId }) { summary ->
                    FriendCard(summary = summary) {
                        navController.navigate(Route.FriendDetail(summary.friendId))
                    }
                }
            }
        }
    }

    if (showDialog) {
        CreateKhataDialog(onDismiss = { showDialog = false }, onConfirm = { name ->
            showDialog = false
            createFriendViewModel.create(name)
        })
    }
}

@Composable
fun KhataOverviewStrip(summaries: List<FriendSummary>) {
    val totalGet = summaries.sumOf { it.totalCredit }
    val totalGive = summaries.sumOf { it.totalDebit }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BalanceItem("Lene Hai", totalGet, Color(0xFF1B5E20)) // Hindi term for "Get"
            VerticalDivider(
                modifier = Modifier
                    .height(32.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            BalanceItem("Dene Hai", totalGive, Color(0xFFB71C1C)) // Hindi term for "Give"
        }
    }
}

@Composable
private fun BalanceItem(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            "₹${amount.toInt()}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun FriendCard(summary: FriendSummary, onClick: () -> Unit) {
    val netBalance = summary.totalCredit - summary.totalDebit
    val indicatorColor = when {
        netBalance > 0 -> Color(0xFF43A047)
        netBalance < 0 -> Color(0xFFE53935)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with a "User Ring"
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    shape = CircleShape,
                    border = BorderStroke(2.dp, indicatorColor.copy(alpha = 0.2f)),
                    modifier = Modifier.size(54.dp)
                ) {
                    AvatarIcon(name = summary.name)
                }
                // Small dot for active/unsettled status
                if (netBalance != 0.0) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .background(indicatorColor, CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A1A)
                )

                val statusLabel = when {
                    netBalance > 0 -> "You'll get back"
                    netBalance < 0 -> "You owe them"
                    else -> "All clear"
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // High impact amount display
            if (netBalance != 0.0) {
                Text(
                    text = "₹${Math.abs(netBalance).toInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = indicatorColor
                )
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outlineVariant
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
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🫂", fontSize = 60.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "No contacts found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Tap '+' to start a new Hisab",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun AddKhataFAB(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = Color(0xFFFF9800), // Saffron
        contentColor = Color.White,
        shape = RoundedCornerShape(18.dp),
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("ADD NEW HISAB", fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun FriendsActionMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSortChange: (FriendSort) -> Unit,
    onFilterChange: (FriendFilter) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        DropdownMenuItem(
            text = { Text("Sort by Name") },
            onClick = { onSortChange(FriendSort.NAME); onDismiss() },
            leadingIcon = { Icon(Icons.Default.SortByAlpha, null) }
        )
        DropdownMenuItem(
            text = { Text("Filter Pending") },
            onClick = { onFilterChange(FriendFilter.PENDING); onDismiss() },
            leadingIcon = { Icon(Icons.Default.FilterList, null) }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Show All") },
            onClick = { onFilterChange(FriendFilter.ALL); onDismiss() }
        )
    }
}