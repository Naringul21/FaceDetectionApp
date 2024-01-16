package com.example.facedetectionapp.data.local.modul

import android.content.Context
import androidx.room.Room
import com.example.facedetectionapp.data.local.dao.EmotionDatabase
import com.example.facedetectionapp.data.local.dao.EmotionResultDao
import com.example.facedetectionapp.data.repository.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModul {

    @Singleton
    @Provides
    fun provideRoomDataBase(@ApplicationContext context: Context): EmotionDatabase =
        Room.databaseBuilder(
            context,
            EmotionDatabase::class.java, "emotiondatabase"
        ).build()


    @Singleton
    @Provides
    fun provideEmotionDao(emotionDB: EmotionDatabase): EmotionResultDao = emotionDB.emotionResultDao()

    @Singleton
    @Provides
    fun provideRepository(db:EmotionResultDao): Repository =Repository(db)
}
