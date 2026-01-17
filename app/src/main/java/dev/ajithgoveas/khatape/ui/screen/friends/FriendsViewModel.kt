package dev.ajithgoveas.khatape.ui.screen.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ajithgoveas.khatape.domain.model.FriendSummary
import dev.ajithgoveas.khatape.domain.usecase.GetFriendSummariesUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class FriendsViewModel @Inject constructor(
    getFriendSummaries: GetFriendSummariesUseCase
) : ViewModel() {

    val friends: StateFlow<List<FriendSummary>> = getFriendSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
