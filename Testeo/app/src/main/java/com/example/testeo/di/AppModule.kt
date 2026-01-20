package com.example.testeo.di

import android.content.Context
import androidx.room.Room
import com.example.testeo.data.local.AppDatabase
import com.example.testeo.data.local.WordDao
import com.example.testeo.data.repository.TranslationRepositoryImpl
import com.example.testeo.domain.repository.TranslationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lazarus_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideWordDao(database: AppDatabase): WordDao {
        return database.wordDao()
    }

    @Provides
    @Singleton
    fun provideTranslationRepository(
        repositoryImpl: TranslationRepositoryImpl
    ): TranslationRepository {
        return repositoryImpl
    }
}
