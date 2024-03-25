package com.blanccone.core.di

import android.app.Application
import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application

    @Provides
    @Singleton
    fun provideFirebaseDb() = FirebaseDatabase.getInstance().getReference("tickets")

    @Provides
    @Singleton
    fun provideStorage() = FirebaseStorage.getInstance().getReference("tickets")
}