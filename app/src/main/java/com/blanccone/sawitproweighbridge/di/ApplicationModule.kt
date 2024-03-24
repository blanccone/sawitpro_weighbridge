package com.blanccone.sawitproweighbridge.di

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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
    fun provideFirebaseDb() = FirebaseDatabase.getInstance().getReference("tickets")

    @Provides
    @Singleton
    fun provideStorage() = FirebaseStorage.getInstance().getReference("tickets")
}