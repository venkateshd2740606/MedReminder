package com.medreminder.di

import android.content.Context
import androidx.room.Room
import com.medreminder.data.local.database.MedReminderDatabase
import com.medreminder.data.local.database.dao.MedicationDao
import com.medreminder.data.local.database.dao.ReminderLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MedReminderDatabase =
        Room.databaseBuilder(context, MedReminderDatabase::class.java, "medreminder.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideMedicationDao(db: MedReminderDatabase): MedicationDao = db.medicationDao()
    @Provides fun provideReminderLogDao(db: MedReminderDatabase): ReminderLogDao = db.reminderLogDao()
}
