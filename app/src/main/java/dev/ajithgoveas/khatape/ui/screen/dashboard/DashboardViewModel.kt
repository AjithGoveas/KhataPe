package dev.ajithgoveas.khatape.ui.screen.dashboard

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
class DashboardViewModel @Inject constructor(
    getFriendSummariesUseCase: GetFriendSummariesUseCase
) : ViewModel() {

    val summaries: StateFlow<List<FriendSummary>> =
        getFriendSummariesUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

