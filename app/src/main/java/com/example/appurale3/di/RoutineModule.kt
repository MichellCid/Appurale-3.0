package com.example.appurale3.di

import com.example.appurale3.data.repositories.RoutineRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoutineModule {

    @Provides
    @Singleton
    fun provideRoutineRepository(
        firestore: FirebaseFirestore
    ): RoutineRepository {
        return RoutineRepository(firestore)
    }
}