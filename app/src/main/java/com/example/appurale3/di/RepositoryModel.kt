package com.example.appurale3.di

import android.content.Context
import com.example.appurale3.data.repositories.RoutineRepository
import com.example.appurale3.data.repositories.SoundRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRoutineRepository(
        firestore: FirebaseFirestore
    ): RoutineRepository {
        return RoutineRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideSoundRepository(
        @ApplicationContext context: Context  // ← AGREGAR CONTEXTO
    ): SoundRepository {
        return SoundRepository(context)  // ← PASAR CONTEXTO
    }
}