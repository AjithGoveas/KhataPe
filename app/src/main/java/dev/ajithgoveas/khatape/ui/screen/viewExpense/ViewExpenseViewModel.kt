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
import dev.ajithgoveas.khatape.domain.usecase.SettleTransactionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ViewExpenseSideEffect {
    data class ShowError(val message: String) : ViewExpenseSideEffect()
    object NavigateBack : ViewExpenseSideEffect()
    data class NavigateToEdit(val expenseId: Long) : ViewExpenseSideEffect()
    object TransactionSettled : ViewExpenseSideEffect()
}

data class ViewExpenseUiState(
    val transaction: Transaction? = null,
    val friend: Friend? = null,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isDeleting: Boolean = false
)

sealed class ViewExpenseEvent {
    object DeleteClicked : ViewExpenseEvent()
    object EditClicked : ViewExpenseEvent()
    object SettleClicked : ViewExpenseEvent()
}

@HiltViewModel
class ViewExpenseViewModel @Inject constructor(
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val getFriendByIdUseCase: GetFriendByIdUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val settleTransactionUseCase: SettleTransactionUseCase
) : ViewModel() {

    private val expenseId = MutableStateFlow<Long?>(null)

    private val _sideEffects = Channel<ViewExpenseSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ViewExpenseUiState> = expenseId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(ViewExpenseUiState(isLoading = false, isError = true))
            } else {
                // Step 1: Observe the Transaction Flow (Hot from Room)
                getTransactionByIdUseCase(id).flatMapLatest { transaction ->
                    if (transaction == null) {
                        flowOf(ViewExpenseUiState(isLoading = false, isError = true))
                    } else {
                        // Step 2: Directly map the Friend data into the state.
                        // By mapping here, we ensure that every time 'transaction' emits
                        // (like when isSettled changes), this block produces a NEW UiState.
                        getFriendByIdUseCase(transaction.friendId).map { friend ->
                            ViewExpenseUiState(
                                transaction = transaction,
                                friend = friend?.toDomain(),
                                isLoading = false,
                                isError = false
                            )
                        }
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Started eagerly so updates are never missed
            initialValue = ViewExpenseUiState(isLoading = true)
        )

    fun setExpenseId(id: Long) {
        expenseId.value = id
    }

    fun onEvent(event: ViewExpenseEvent) {
        when (event) {
            ViewExpenseEvent.DeleteClicked -> handleDelete()
            ViewExpenseEvent.EditClicked -> handleEdit()
            ViewExpenseEvent.SettleClicked -> handleSettle()
        }
    }

    private fun handleSettle() {
        val transaction = uiState.value.transaction ?: return
        if (transaction.isSettled) return

        viewModelScope.launch {
            try {
                // Result triggers the side effect, but the UI update comes
                // automatically through the reactive Flow above.
                val result = settleTransactionUseCase(transactionId = transaction.id)
                if (result > 0) {
                    _sideEffects.send(ViewExpenseSideEffect.TransactionSettled)
                } else {
                    _sideEffects.send(ViewExpenseSideEffect.ShowError("Could not settle transaction."))
                }
            } catch (e: Exception) {
                _sideEffects.send(ViewExpenseSideEffect.ShowError("Error: ${e.message}"))
            }
        }
    }

    private fun handleDelete() {
        val transaction = uiState.value.transaction ?: return
        viewModelScope.launch {
            try {
                deleteTransactionUseCase(transaction.id)
                _sideEffects.send(ViewExpenseSideEffect.NavigateBack)
            } catch (_: Exception) {
                _sideEffects.send(ViewExpenseSideEffect.ShowError("Failed to delete transaction."))
            }
        }
    }

    private fun handleEdit() {
        uiState.value.transaction?.id?.let { id ->
            viewModelScope.launch {
                _sideEffects.send(ViewExpenseSideEffect.NavigateToEdit(id))
            }
        }
    }
}