package dev.ajithgoveas.khatape.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ajithgoveas.khatape.data.local.ThemePreferenceManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ThemePreferenceModule {
    @Provides
    @Singleton
    fun provideThemePreferenceManager(
        @ApplicationContext context: Context
    ): ThemePreferenceManager = ThemePreferenceManager(context)
}