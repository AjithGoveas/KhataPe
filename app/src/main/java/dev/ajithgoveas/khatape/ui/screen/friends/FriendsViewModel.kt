package dev.ajithgoveas.khatape.ui.screen.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ajithgoveas.khatape.domain.model.FriendSummary
import dev.ajithgoveas.khatape.domain.usecase.GetFriendSummariesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    getFriendSummaries: GetFriendSummariesUseCase
) : ViewModel() {

    // Keep the upstream flow hot while ViewModel is alive
    val friends: StateFlow<List<FriendSummary>> = getFriendSummaries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // start immediately, no restart overhead
            initialValue = emptyList()
        )
}

