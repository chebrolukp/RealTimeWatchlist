package com.doximity.realtimewatchlist_krishna_doximity.di

import android.content.Context
import androidx.room.Room
import com.doximity.realtimewatchlist_krishna_doximity.BuildConfig
import com.doximity.realtimewatchlist_krishna_doximity.data.alert.SystemPriceAlertNotifier
import com.doximity.realtimewatchlist_krishna_doximity.data.local.WatchlistDatabase
import com.doximity.realtimewatchlist_krishna_doximity.data.remote.FinnhubApi
import com.doximity.realtimewatchlist_krishna_doximity.data.repository.FakeMarketDataRepository
import com.doximity.realtimewatchlist_krishna_doximity.data.repository.FinnhubMarketDataRepository
import com.doximity.realtimewatchlist_krishna_doximity.data.repository.RoomWatchlistRepository
import javax.inject.Provider
import com.doximity.realtimewatchlist_krishna_doximity.domain.alert.PriceAlertNotifier
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.MarketDataRepository
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.WatchlistRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMarketDataRepository(
        @Named("demoMode") demoMode: Boolean,
        finnhubRepository: Provider<FinnhubMarketDataRepository>,
        fakeRepository: Provider<FakeMarketDataRepository>,
    ): MarketDataRepository = if (demoMode) {
        fakeRepository.get()
    } else {
        finnhubRepository.get()
    }

    @Provides
    @Singleton
    fun provideWatchlistRepository(
        impl: RoomWatchlistRepository,
    ): WatchlistRepository = impl
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("applicationScope")
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    @Named("finnhubApiKey")
    fun provideFinnhubApiKey(): String = BuildConfig.FINNHUB_API_KEY

    @Provides
    @Named("demoMode")
    fun provideDemoMode(): Boolean = BuildConfig.DEMO_MODE

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): WatchlistDatabase = Room.databaseBuilder(
        context,
        WatchlistDatabase::class.java,
        "watchlist.db",
    )
        .addMigrations(WatchlistDatabase.MIGRATION_1_2)
        .build()

    @Provides
    fun provideWatchlistDao(database: WatchlistDatabase) = database.watchlistDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AlertModule {
    @Binds
    @Singleton
    abstract fun bindPriceAlertNotifier(
        impl: SystemPriceAlertNotifier,
    ): PriceAlertNotifier
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @Named("finnhubApiKey") apiKey: String,
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val urlBuilder = original.url.newBuilder()
                if (apiKey.isNotBlank() && original.url.queryParameter("token").isNullOrBlank()) {
                    urlBuilder.addQueryParameter("token", apiKey)
                }
                val request = original.newBuilder()
                    .url(urlBuilder.build())
                    .header("X-Finnhub-Token", apiKey)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @Named("finnhubWebSocket")
    fun provideWebSocketOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .pingInterval(20, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideFinnhubApi(
        okHttpClient: OkHttpClient,
        json: Json,
    ): FinnhubApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(FinnhubApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(FinnhubApi::class.java)
    }
}
