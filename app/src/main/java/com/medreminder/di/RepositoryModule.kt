package com.medreminder.di

import com.medreminder.data.repository.MedicationRepositoryImpl
import com.medreminder.data.repository.PreferencesRepositoryImpl
import com.medreminder.data.repository.ReminderLogRepositoryImpl
import com.medreminder.domain.repository.MedicationRepository
import com.medreminder.domain.repository.PreferencesRepository
import com.medreminder.domain.repository.ReminderLogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindMedicationRepository(impl: MedicationRepositoryImpl): MedicationRepository
    @Binds @Singleton abstract fun bindReminderLogRepository(impl: ReminderLogRepositoryImpl): ReminderLogRepository
    @Binds @Singleton abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}
