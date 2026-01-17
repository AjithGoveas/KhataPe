package dev.ajithgoveas.khatape.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ajithgoveas.khatape.domain.usecase.DeleteAllUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val deleteAllUseCase: DeleteAllUseCase
) : ViewModel() {

    fun clearAllData(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                deleteAllUseCase()
                onComplete(true)
            } catch (_: Exception) {
                onComplete(false)
            }
        }
    }
}
