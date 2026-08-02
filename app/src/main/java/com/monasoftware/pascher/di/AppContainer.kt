package com.monasoftware.pascher.di

import android.content.Context
import androidx.room.Room
import com.monasoftware.pascher.data.local.PasCherDatabase
import com.monasoftware.pascher.data.preferences.UserPreferencesRepository
import com.monasoftware.pascher.data.remote.ArchiveApiService
import com.monasoftware.pascher.data.remote.TraktApiService
import com.monasoftware.pascher.data.repository.MovieRepository
import com.monasoftware.pascher.data.repository.MovieRepositoryImpl
import com.monasoftware.pascher.data.repository.SubscriptionRepository
import com.monasoftware.pascher.data.repository.SubscriptionRepositoryImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
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

    private val traktInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("trakt-api-version", TraktApiService.API_VERSION)
            .addHeader("trakt-api-key", TraktApiService.CLIENT_ID)
            .build()
        chain.proceed(request)
    }

    private val traktClient = OkHttpClient.Builder()
        .addInterceptor(traktInterceptor)
        .build()

    private val traktRetrofit = Retrofit.Builder()
        .baseUrl(TraktApiService.BASE_URL)
        .client(traktClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val archiveRetrofit = Retrofit.Builder()
        .baseUrl(ArchiveApiService.BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val traktApiService: TraktApiService by lazy {
        traktRetrofit.create(TraktApiService::class.java)
    }

    private val archiveApiService: ArchiveApiService by lazy {
        archiveRetrofit.create(ArchiveApiService::class.java)
    }

    private val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }

    override val movieRepository: MovieRepository by lazy {
        MovieRepositoryImpl(database.movieDao(), traktApiService, archiveApiService)
    }

    override val subscriptionRepository: SubscriptionRepository by lazy {
        SubscriptionRepositoryImpl(userPreferencesRepository)
    }
}
