package dev.ajithgoveas.khatape.ui.screen.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ajithgoveas.khatape.domain.usecase.CreateFriendUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CreateFriendViewModel @Inject constructor(
    private val createFriend: CreateFriendUseCase
) : ViewModel() {

    // Corrected type from Int? to Long?
    private val _createdFriendId = MutableStateFlow<Long?>(null)
    val createdFriendId: StateFlow<Long?> = _createdFriendId

    fun create(name: String) {
        viewModelScope.launch {
            val id = createFriend(name)
            _createdFriendId.value = id
        }
    }

    fun reset() {
        _createdFriendId.value = null
    }
}