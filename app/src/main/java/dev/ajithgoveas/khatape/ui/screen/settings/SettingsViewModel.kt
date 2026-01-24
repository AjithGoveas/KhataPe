package dev.ajithgoveas.khatape.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ajithgoveas.khatape.data.local.ThemePreferenceManager
import dev.ajithgoveas.khatape.domain.usecase.DeleteAllUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val deleteAllUseCase: DeleteAllUseCase,
    private val themePreferenceManager: ThemePreferenceManager
) : ViewModel() {

    val themeState: StateFlow<AppTheme> = themePreferenceManager.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
        )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            themePreferenceManager.saveTheme(theme)
        }
    }

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
