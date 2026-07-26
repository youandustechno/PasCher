package com.monasoftware.pascher.di

import android.content.Context
import androidx.room.Room
import com.monasoftware.pascher.data.local.PasCherDatabase
import com.monasoftware.pascher.data.preferences.UserPreferencesRepository
import com.monasoftware.pascher.data.remote.ArchiveApiService
import com.monasoftware.pascher.data.remote.MovieApiService
import com.monasoftware.pascher.data.repository.MovieRepository
import com.monasoftware.pascher.data.repository.MovieRepositoryImpl
import com.monasoftware.pascher.data.repository.SubscriptionRepository
import com.monasoftware.pascher.data.repository.SubscriptionRepositoryImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

interface AppContainer {
    val movieRepository: MovieRepository
    val subscriptionRepository: SubscriptionRepository
}

class AppContainerImpl(private val context: Context) : AppContainer {

    private val database: PasCherDatabase by lazy {
        Room.databaseBuilder(
            context,
            PasCherDatabase::class.java,
            PasCherDatabase.DATABASE_NAME
        ).build()
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ArchiveApiService.BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val archiveApiService: ArchiveApiService by lazy {
        retrofit.create(ArchiveApiService::class.java)
    }

    private val movieApiService: MovieApiService by lazy {
        // Keeping it for potential legacy, but ArchiveApiService is main
        retrofit.newBuilder()
            .baseUrl(MovieApiService.BASE_URL)
            .build()
            .create(MovieApiService::class.java)
    }

    private val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }

    override val movieRepository: MovieRepository by lazy {
        MovieRepositoryImpl(database.movieDao(), archiveApiService)
    }

    override val subscriptionRepository: SubscriptionRepository by lazy {
        SubscriptionRepositoryImpl(userPreferencesRepository)
    }
}
