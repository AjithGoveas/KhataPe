package dev.ajithgoveas.khatape.ui.screen.addExpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ajithgoveas.khatape.domain.mappers.toDomain
import dev.ajithgoveas.khatape.domain.model.Friend
import dev.ajithgoveas.khatape.domain.model.FriendSummary
import dev.ajithgoveas.khatape.domain.model.TransactionDirection
import dev.ajithgoveas.khatape.domain.usecase.AddTransactionUseCase
import dev.ajithgoveas.khatape.domain.usecase.GetFriendByIdUseCase
import dev.ajithgoveas.khatape.domain.usecase.GetFriendSummaryByIdUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddExpenseUiState(
    val friend: Friend? = null,
    val friendSummary: FriendSummary? = null,
    val amount: String = "",
    val amountError: String? = null,
    val direction: TransactionDirection = TransactionDirection.DEBIT,
    val description: String = "",
    val dueDate: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isError: Boolean
)

sealed class AddExpenseEvent {
    data class AmountChanged(val amount: String) : AddExpenseEvent()
    data class DirectionChanged(val direction: TransactionDirection) : AddExpenseEvent()
    data class DescriptionChanged(val description: String) : AddExpenseEvent()
    data class DueDateChanged(val dueDate: Long?) : AddExpenseEvent()
    data class TimestampChanged(val timestamp: Long) : AddExpenseEvent()
    object SaveClicked : AddExpenseEvent()
    object CancelClicked : AddExpenseEvent()
    data class Error(val message: String) : AddExpenseEvent()
}

data class FormState(
    val amount: String = "",
    val amountError: String? = null,
    val direction: TransactionDirection = TransactionDirection.DEBIT,
    val description: String = "",
    val dueDate: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val getFriendById: GetFriendByIdUseCase,
    private val getFriendSummaryById: GetFriendSummaryByIdUseCase,
    private val addTransaction: AddTransactionUseCase
) : ViewModel() {

    private val friendId = MutableStateFlow<Long?>(null)

    // Expose friendSummary directly for simple consumption
    val friendSummary = friendId.flatMapLatest { id ->
        if (id == null) flowOf(null) else getFriendSummaryById(id)
    }

    val uiState: StateFlow<AddExpenseUiState> = friendId
        .map { id ->
            if (id == null) {
                flowOf(AddExpenseUiState(isLoading = false, isError = true))
            } else {
                combine(
                    getFriendById(id),
                    getFriendSummaryById(id)
                ) { friendEntity, summary ->
                    AddExpenseUiState(
                        friend = friendEntity?.toDomain(),
                        friendSummary = summary,
                        isLoading = false,
                        isError = friendEntity == null
                    )
                }
            }
        }
        .flatMapLatest { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly, // keep hot, avoid restarts
            initialValue = AddExpenseUiState(isLoading = true, isError = false)
        )

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState

    private val _sideEffects = MutableSharedFlow<AddExpenseSideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    fun setFriendId(id: Long) {
        friendId.value = id
    }

    fun onEvent(event: AddExpenseEvent) {
        when (event) {
            is AddExpenseEvent.AmountChanged ->
                _formState.update { it.copy(amount = event.amount, amountError = null) }

            is AddExpenseEvent.DirectionChanged ->
                _formState.update { it.copy(direction = event.direction) }

            is AddExpenseEvent.DescriptionChanged ->
                _formState.update { it.copy(description = event.description) }

            is AddExpenseEvent.DueDateChanged ->
                _formState.update { it.copy(dueDate = event.dueDate) }

            is AddExpenseEvent.TimestampChanged ->
                _formState.update { it.copy(timestamp = event.timestamp) }

            is AddExpenseEvent.Error -> handleError(event.message)
            AddExpenseEvent.SaveClicked -> handleSave()
            AddExpenseEvent.CancelClicked -> handleCancel()
        }
    }

    private fun handleSave() {
        val friend = uiState.value.friend ?: return
        val state = formState.value
        val amount = state.amount.toDoubleOrNull()

        if (amount == null || amount <= 0.0) {
            _formState.update { it.copy(amountError = "Valid amount is required") }
            return
        }

        _formState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val txnId = addTransaction(
                    friendId = friend.id,
                    amount = amount,
                    direction = state.direction,
                    description = state.description,
                    dueDate = state.dueDate,
                    timeStamp = state.timestamp
                )
                _formState.update { it.copy(isSaving = false) }
                _sideEffects.emit(AddExpenseSideEffect.SaveSuccess(friend.id))
            } catch (e: Exception) {
                _sideEffects.emit(AddExpenseSideEffect.ShowError("Failed to save expense: ${e.message}"))
            } finally {
                _formState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun handleCancel() {
        viewModelScope.launch {
            _sideEffects.emit(AddExpenseSideEffect.Cancelled)
        }
    }

    private fun handleError(message: String) {
        viewModelScope.launch {
            _sideEffects.emit(AddExpenseSideEffect.ShowError(message))
        }
    }
}

// Side effects for one-time events
sealed class AddExpenseSideEffect {
    data class SaveSuccess(val friendId: Long) : AddExpenseSideEffect()
    data class ShowError(val message: String) : AddExpenseSideEffect()
    object Cancelled : AddExpenseSideEffect()
}
