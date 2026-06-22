package com.doximity.realtimewatchlist_krishna_doximity.di

import com.doximity.realtimewatchlist_krishna_doximity.BuildConfig
import com.doximity.realtimewatchlist_krishna_doximity.data.remote.FinnhubApi
import com.doximity.realtimewatchlist_krishna_doximity.data.repository.FinnhubMarketDataRepository
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.MarketDataRepository
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMarketDataRepository(
        impl: FinnhubMarketDataRepository
    ): MarketDataRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("finnhubApiKey")
    fun provideFinnhubApiKey(): String = BuildConfig.FINNHUB_API_KEY

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
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
