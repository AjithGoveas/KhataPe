package dev.ajithgoveas.khatape.ui.screen.editExpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ajithgoveas.khatape.domain.mappers.toDomain
import dev.ajithgoveas.khatape.domain.model.Friend
import dev.ajithgoveas.khatape.domain.model.FriendSummary
import dev.ajithgoveas.khatape.domain.model.TransactionDirection
import dev.ajithgoveas.khatape.domain.usecase.GetFriendByIdUseCase
import dev.ajithgoveas.khatape.domain.usecase.GetTransactionByIdUseCase
import dev.ajithgoveas.khatape.domain.usecase.UpdateTransactionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenseUiState(
    val friend: Friend? = null,
    val friendSummary: FriendSummary? = null,
    val transactionId: Long? = null, // null for new, non-null for edit
    val amount: String = "",
    val amountError: String? = null,
    val direction: TransactionDirection = TransactionDirection.DEBIT,
    val description: String = "",
    val dueDate: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false
)

sealed class ExpenseEvent {
    data class AmountChanged(val amount: String) : ExpenseEvent()
    data class DirectionChanged(val direction: TransactionDirection) : ExpenseEvent()
    data class DescriptionChanged(val description: String) : ExpenseEvent()
    data class TimestampChanged(val timestamp: Long) : ExpenseEvent()
    data class DueDateChanged(val dueDate: Long?) : ExpenseEvent()
    object SaveClicked : ExpenseEvent()
    object CancelClicked : ExpenseEvent()
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
class EditExpenseViewModel @Inject constructor(
    private val getTransactionById: GetTransactionByIdUseCase,
    private val getFriendById: GetFriendByIdUseCase,
    private val updateTransaction: UpdateTransactionUseCase
) : ViewModel() {

    private val transactionId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<ExpenseUiState> = transactionId
        .flatMapLatest { id ->
            if (id == null) flowOf(ExpenseUiState(isLoading = false))
            else getTransactionById(id).flatMapLatest { transaction ->
                if (transaction == null) {
                    flowOf(ExpenseUiState(isLoading = false))
                } else {
                    getFriendById(transaction.friendId).map { friend ->
                        ExpenseUiState(
                            friend = friend?.toDomain(),
                            transactionId = transaction.id,
                            amount = transaction.amount.toString(),
                            direction = transaction.direction,
                            description = transaction.description,
                            dueDate = transaction.dueDate,
                            timestamp = transaction.timestamp,
                            isLoading = false
                        )
                    }
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ExpenseUiState(isLoading = true)
        )

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState

    fun setTransactionId(id: Long) {
        transactionId.value = id
    }

    fun preloadForm() {
        val state = uiState.value
        _formState.update {
            it.copy(
                amount = state.amount,
                direction = state.direction,
                description = state.description,
                dueDate = state.dueDate,
                timestamp = state.timestamp
            )
        }
    }

    fun onEvent(event: ExpenseEvent) {
        when (event) {
            is ExpenseEvent.AmountChanged -> {
                val isValid = event.amount.toDoubleOrNull() != null || event.amount.isEmpty()
                _formState.update {
                    it.copy(
                        amount = event.amount,
                        amountError = if (!isValid && event.amount.isNotEmpty()) "Invalid amount" else null
                    )
                }
            }

            is ExpenseEvent.DirectionChanged -> _formState.update { it.copy(direction = event.direction) }
            is ExpenseEvent.DescriptionChanged -> _formState.update { it.copy(description = event.description) }
            is ExpenseEvent.TimestampChanged -> _formState.update { it.copy(timestamp = event.timestamp) }
            is ExpenseEvent.DueDateChanged -> _formState.update { it.copy(dueDate = event.dueDate) }
            ExpenseEvent.SaveClicked -> handleSave()
            ExpenseEvent.CancelClicked -> handleCancel()
        }
    }

    private fun handleSave() {
        val id = transactionId.value ?: return
        val state = formState.value
        val amount = state.amount.toDoubleOrNull()

        if (amount == null || amount <= 0.0) {
            _formState.update { it.copy(amountError = "Valid amount is required") }
            return
        }

        _formState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            updateTransaction(
                id,
                amount,
                state.direction,
                state.description,
                state.dueDate,
                state.timestamp
            )
            _formState.update { it.copy(isSaving = false) }
        }
    }

    private fun handleCancel() {
        // Optional: emit cancel effect
    }
}