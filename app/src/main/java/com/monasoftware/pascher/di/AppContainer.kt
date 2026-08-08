package com.monasoftware.pascher.di

import android.content.Context
import androidx.room.Room
import com.monasoftware.pascher.data.local.PasCherDatabase
import com.monasoftware.pascher.data.preferences.UserPreferencesRepository
import com.monasoftware.pascher.data.remote.ArchiveApiService
import com.monasoftware.pascher.data.remote.PayPalService
import com.monasoftware.pascher.data.remote.StreamingApi
import com.monasoftware.pascher.data.repository.MovieRepository
import com.monasoftware.pascher.data.repository.MovieRepositoryImpl
import com.monasoftware.pascher.data.repository.SubscriptionRepository
import com.monasoftware.pascher.data.repository.SubscriptionRepositoryImpl
import com.monasoftware.pascher.data.watchtogether.FirebaseSignalingService
import com.monasoftware.pascher.data.watchtogether.SignalingService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

interface AppContainer {
    val movieRepository: MovieRepository
    val subscriptionRepository: SubscriptionRepository
    val userPreferencesRepository: UserPreferencesRepository
    val signalingService: SignalingService
}

class AppContainerImpl(private val context: Context) : AppContainer {

    private val database: PasCherDatabase by lazy {
        Room.databaseBuilder(
            context,
            PasCherDatabase::class.java,
            PasCherDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val streamingApiInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("X-API-Key", "motn-key-v4-U0R7BPcNzPkejm45U0yONd4x4CkPC5R4")
            .build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val streamingClient = OkHttpClient.Builder()
        .addInterceptor(streamingApiInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val streamingRetrofit = Retrofit.Builder()
        .baseUrl(StreamingApi.STREAMING_BASE_URL)
        .client(streamingClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val archiveRetrofit = Retrofit.Builder()
        .baseUrl(ArchiveApiService.BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    // PayPal Retrofit setup
    private val payPalClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val payPalRetrofit = Retrofit.Builder()
        .baseUrl("https://api-m.sandbox.paypal.com/") // Change to https://api-m.paypal.com/ for production
        .client(payPalClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val streamingApi: StreamingApi by lazy {
        streamingRetrofit.create(StreamingApi::class.java)
    }

    private val archiveApiService: ArchiveApiService by lazy {
        archiveRetrofit.create(ArchiveApiService::class.java)
    }

    private val payPalService: PayPalService by lazy {
        payPalRetrofit.create(PayPalService::class.java)
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }

    override val signalingService: SignalingService by lazy {
        FirebaseSignalingService()
    }

    override val movieRepository: MovieRepository by lazy {
        MovieRepositoryImpl(database.movieDao(), streamingApi, archiveApiService)
    }

    override val subscriptionRepository: SubscriptionRepository by lazy {
        SubscriptionRepositoryImpl(userPreferencesRepository, payPalService)
    }
}