package io.github.zapofnakamoto.design

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import whitewallet.brand.ui.BrandDesign

/** Registers Zap of Nakamoto's design with the app. */
@Module
@InstallIn(SingletonComponent::class)
object ZapModule {
    @Provides
    fun design(): BrandDesign = ZapDesign
}
