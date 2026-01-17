package dev.ajithgoveas.khatape.ui.screen.viewExpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ajithgoveas.khatape.domain.mappers.toDomain
import dev.ajithgoveas.khatape.domain.model.Friend
import dev.ajithgoveas.khatape.domain.model.Transaction
import dev.ajithgoveas.khatape.domain.usecase.DeleteTransactionUseCase
import dev.ajithgoveas.khatape.domain.usecase.GetFriendByIdUseCase
import dev.ajithgoveas.khatape.domain.usecase.GetTransactionByIdUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// A sealed class for all possible side effects or one-time events
sealed class ViewExpenseSideEffect {
    data class ShowError(val message: String) : ViewExpenseSideEffect()
    object NavigateBack : ViewExpenseSideEffect()

    // Add other side effects like a navigation event to the edit screen
    data class NavigateToEdit(val expenseId: Long) : ViewExpenseSideEffect()
}

// Data class to hold the UI state for ViewExpenseScreen
data class ViewExpenseUiState(
    val transaction: Transaction? = null,
    val friend: Friend? = null,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isDeleting: Boolean = false
)

// Sealed class for all user actions on the ViewExpenseScreen
sealed class ViewExpenseEvent {
    object DeleteClicked : ViewExpenseEvent()
    object EditClicked : ViewExpenseEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ViewExpenseViewModel @Inject constructor(
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val getFriendByIdUseCase: GetFriendByIdUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val expenseId = MutableStateFlow<Long?>(null)

    // A channel for one-time events like navigation or showing a snackbar
    private val _sideEffects = Channel<ViewExpenseSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    // Private backing property that is mutable
    private val _uiState = MutableStateFlow(ViewExpenseUiState(isLoading = true))

    // Public read-only StateFlow
    val uiState: StateFlow<ViewExpenseUiState> = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        _uiState.value
    )

    init {
        // Collect from the reactive stream and update the private _uiState
        viewModelScope.launch {
            expenseId
                .flatMapLatest { id ->
                    if (id == null) {
                        flowOf(ViewExpenseUiState(isLoading = false, isError = true))
                    } else {
                        getTransactionByIdUseCase(id)
                            .flatMapLatest { transaction ->
                                if (transaction == null) {
                                    flowOf(ViewExpenseUiState(isLoading = false, isError = true))
                                } else {
                                    getFriendByIdUseCase(transaction.friendId)
                                        .combine(flowOf(transaction)) { friend, transactionResult ->
                                            ViewExpenseUiState(
                                                transaction = transactionResult,
                                                friend = friend?.toDomain(),
                                                isLoading = false,
                                                isError = false
                                            )
                                        }
                                }
                            }
                    }
                }.collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    fun setExpenseId(id: Long) {
        expenseId.value = id
    }

    fun onEvent(event: ViewExpenseEvent) {
        when (event) {
            is ViewExpenseEvent.DeleteClicked -> handleDelete()
            is ViewExpenseEvent.EditClicked -> handleEdit()
        }
    }

    private fun handleDelete() {
        val transaction = _uiState.value.transaction ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)
            try {
                deleteTransactionUseCase(transaction.id)
                _sideEffects.send(ViewExpenseSideEffect.NavigateBack)
            } catch (_: Exception) {
                _sideEffects.send(ViewExpenseSideEffect.ShowError("Failed to delete transaction."))
            } finally {
                _uiState.value = _uiState.value.copy(isDeleting = false)
            }
        }
    }

    private fun handleEdit() {
        val transactionId = _uiState.value.transaction?.id ?: return
        viewModelScope.launch {
            _sideEffects.send(ViewExpenseSideEffect.NavigateToEdit(transactionId))
        }
    }
}