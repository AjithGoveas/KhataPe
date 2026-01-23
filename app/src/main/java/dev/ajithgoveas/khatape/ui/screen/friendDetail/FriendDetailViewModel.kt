package dev.ajithgoveas.khatape.ui.screen.friendDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ajithgoveas.khatape.domain.mappers.toDomain
import dev.ajithgoveas.khatape.domain.model.Friend
import dev.ajithgoveas.khatape.domain.model.FriendSummary
import dev.ajithgoveas.khatape.domain.model.Transaction
import dev.ajithgoveas.khatape.domain.usecase.DeleteFriendUseCase
import dev.ajithgoveas.khatape.domain.usecase.GetFriendByIdUseCase
import dev.ajithgoveas.khatape.domain.usecase.GetFriendSummaryByIdUseCase
import dev.ajithgoveas.khatape.domain.usecase.GetTransactionsForFriendUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendDetailUiState(
    val friend: Friend? = null,
    val friendSummary: FriendSummary? = null,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val isError: Boolean = false
)

sealed class FriendDetailEvent {
    object DeleteClicked : FriendDetailEvent()
}

sealed class FriendDetailSideEffect {
    object NavigateBack : FriendDetailSideEffect()
    data class ShowError(val message: String) : FriendDetailSideEffect()
}

@HiltViewModel
class FriendDetailViewModel @Inject constructor(
    private val getFriendByIdUseCase: GetFriendByIdUseCase,
    private val getFriendSummaryById: GetFriendSummaryByIdUseCase,
    private val getTransactionsForFriend: GetTransactionsForFriendUseCase,
    private val deleteFriendUseCase: DeleteFriendUseCase
) : ViewModel() {

    private val friendId = MutableStateFlow<Long?>(null)
    private val _sideEffects = Channel<FriendDetailSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    // Build uiState directly from flows, no manual collect
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FriendDetailUiState> = friendId
        .map { id ->
            if (id == null) {
                flowOf(FriendDetailUiState(isLoading = false, isError = true))
            } else {
                combine(
                    getFriendByIdUseCase(id),
                    getFriendSummaryById(id),
                    getTransactionsForFriend(id)
                ) { friend, summary, transactions ->
                    FriendDetailUiState(
                        friend = friend?.toDomain(),
                        friendSummary = summary,
                        transactions = transactions,
                        isLoading = false,
                        isError = friend == null
                    )
                }
            }
        }
        .flatMapLatest { it } // flatten the inner flow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FriendDetailUiState(isLoading = true)
        )

    fun setFriendId(id: Long) {
        friendId.value = id
    }

    fun onEvent(event: FriendDetailEvent) {
        when (event) {
            FriendDetailEvent.DeleteClicked -> handleDelete()
        }
    }

    private fun handleDelete() {
        val friend = uiState.value.friend ?: return

        viewModelScope.launch {
            try {
                // Optimistically show deleting state
                _sideEffects.send(FriendDetailSideEffect.ShowError("Deleting friend..."))
                deleteFriendUseCase(friend)
                _sideEffects.send(FriendDetailSideEffect.NavigateBack)
            } catch (e: Exception) {
                _sideEffects.send(FriendDetailSideEffect.ShowError("Failed to delete friend: ${e.message}"))
            }
        }
    }
}