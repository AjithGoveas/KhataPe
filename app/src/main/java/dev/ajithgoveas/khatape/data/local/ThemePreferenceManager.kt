package dev.ajithgoveas.khatape.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ajithgoveas.khatape.ui.screen.settings.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemePreferenceManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // The Flow MUST be updated manually because SharedPreferences
    // doesn't emit updates automatically like Room does.
    private val _themeFlow = MutableStateFlow(loadTheme())
    val themeFlow = _themeFlow.asStateFlow()

    fun saveTheme(theme: AppTheme) {
        prefs.edit().putString("theme_key", theme.name).apply()
        _themeFlow.value = theme // This line triggers the UI update!
    }

    private fun loadTheme(): AppTheme {
        val name = prefs.getString("theme_key", AppTheme.SYSTEM.name)
        return AppTheme.valueOf(name ?: AppTheme.SYSTEM.name)
    }
}