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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    val isSaving: Boolean = false
)

sealed class AddExpenseEvent {
    data class AmountChanged(val amount: String) : AddExpenseEvent()
    data class DirectionChanged(val direction: TransactionDirection) : AddExpenseEvent()
    data class DescriptionChanged(val description: String) : AddExpenseEvent()
    data class DueDateChanged(val dueDate: Long?) : AddExpenseEvent()
    data class TimestampChanged(val timestamp: Long) : AddExpenseEvent()
    object SaveClicked : AddExpenseEvent()
    object CancelClicked : AddExpenseEvent()
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

    val uiState: StateFlow<AddExpenseUiState> = friendId
        .flatMapLatest { id ->
            if (id == null) flowOf(AddExpenseUiState(isLoading = false))
            else combine(
                getFriendById(id),
                getFriendSummaryById(id)
            ) { friendEntity, summary ->
                AddExpenseUiState(
                    friend = friendEntity?.toDomain(),
                    friendSummary = summary,
                    isLoading = false
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AddExpenseUiState(isLoading = true)
        )

    // Local UI-only state
    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState

    fun setFriendId(id: Long) {
        friendId.value = id
    }

    fun onEvent(event: AddExpenseEvent) {
        when (event) {
            is AddExpenseEvent.AmountChanged -> {
                val isValid = event.amount.toDoubleOrNull() != null || event.amount.isEmpty()
                _formState.update {
                    it.copy(
                        amount = event.amount,
                        amountError = if (!isValid && event.amount.isNotEmpty()) "Invalid amount" else null
                    )
                }
            }

            is AddExpenseEvent.DirectionChanged -> _formState.update { it.copy(direction = event.direction) }
            is AddExpenseEvent.DescriptionChanged -> _formState.update { it.copy(description = event.description) }
            is AddExpenseEvent.DueDateChanged -> _formState.update { it.copy(dueDate = event.dueDate) }
            is AddExpenseEvent.TimestampChanged -> _formState.update { it.copy(timestamp = event.timestamp) }
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
            addTransaction(
                friendId = friend.id,
                amount = amount,
                direction = state.direction,
                description = state.description,
                dueDate = state.dueDate,
                timeStamp = state.timestamp
            )
            _formState.update { it.copy(isSaving = false) }
            // TODO: emit save success effect
        }
    }

    private fun handleCancel() {
        // TODO: emit cancel effect or reset form
    }
}