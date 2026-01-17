package dev.ajithgoveas.khatape.ui.screen.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.ajithgoveas.khatape.domain.model.FriendSummary
import dev.ajithgoveas.khatape.ui.components.CreateKhataDialog
import dev.ajithgoveas.khatape.ui.navigation.Screen

@Composable
private fun Header() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("📒 Khata List", style = MaterialTheme.typography.headlineMedium)
        Text("Track who owes what, with clarity and trust.")
    }
}

@Composable
private fun FriendList(
    summaries: List<FriendSummary>,
    onFriendClick: (Long) -> Unit
) {
    if (summaries.isEmpty()) {
        Text("No friends yet. Add one to get started!", style = MaterialTheme.typography.bodyMedium)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            summaries.forEach { summary ->
                FriendCard(summary = summary) {
                    onFriendClick(summary.friendId)
                }
            }
        }
    }
}

@Composable
fun FriendCard(
    summary: FriendSummary,
    onClick: () -> Unit
) {
    val statusText = buildString {
        when {
            summary.totalDebit > 0 && summary.totalCredit == 0.0 -> {
                append("You owe ₹${summary.totalDebit.toInt()}") // DEBIT
            }

            summary.totalCredit > 0 && summary.totalDebit == 0.0 -> {
                append("${summary.name} owes you ₹${summary.totalCredit.toInt()}") // CREDIT
            }

            summary.totalCredit > 0 && summary.totalDebit > 0 -> {
                append("You owe ₹${summary.totalDebit.toInt()} | ${summary.name} owes you ₹${summary.totalCredit.toInt()}")
            }

            else -> {
                append("${summary.name} is settled")
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = summary.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium
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
        Text("Khata")
    }
}

@Composable
fun FriendsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    friendsViewModel: FriendsViewModel = hiltViewModel(),
    createFriendViewModel: CreateFriendViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(false) }

    val summaries by friendsViewModel.friends.collectAsState()
    val createdFriendId by createFriendViewModel.createdFriendId.collectAsState()

    LaunchedEffect(createdFriendId) {
        createdFriendId?.let {
            navController.navigate(Screen.FriendsScreens.FriendDetail.createRoute(it))
            createFriendViewModel.reset()
        }
    }

    if (showDialog) {
        CreateKhataDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name ->
                showDialog = false
                createFriendViewModel.create(name)
            }
        )
    }

    val layoutDirection = LocalLayoutDirection.current
    Scaffold(
        floatingActionButton = { AddKhataFAB(onClick = { showDialog = true }) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Header()
            FriendList(summaries = summaries) {
                navController.navigate(Screen.FriendsScreens.FriendDetail.createRoute(it))
            }
        }
    }
}
