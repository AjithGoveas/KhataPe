package dev.ajithgoveas.khatape.ui.screen.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ajithgoveas.khatape.domain.usecase.CreateFriendUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateFriendViewModel @Inject constructor(
    private val createFriend: CreateFriendUseCase
) : ViewModel() {

    // Use a Channel for one-time events (navigation, snackbars, etc.)
    private val _sideEffects = Channel<CreateFriendSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    fun create(name: String) {
        viewModelScope.launch {
            try {
                val id = createFriend(name)
                _sideEffects.send(CreateFriendSideEffect.FriendCreated(id))
            } catch (e: Exception) {
                _sideEffects.send(CreateFriendSideEffect.ShowError("Failed to create friend: ${e.message}"))
            }
        }
    }
}

// Side effects for one-time events
sealed class CreateFriendSideEffect {
    data class FriendCreated(val id: Long) : CreateFriendSideEffect()
    data class ShowError(val message: String) : CreateFriendSideEffect()
}
