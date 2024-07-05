package com.seosh817.coroutines_ble.di

import android.content.Context
import com.seosh817.coroutines_ble.CoroutinesBle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class BLEModule {

    @Singleton
    @Provides
    fun provideCoroutinesBLE(@ApplicationContext context: Context): CoroutinesBle {
        return CoroutinesBle(context)
    }
}