package com.example.weather_app.di

import com.example.weather_app.data.remote.OpenMeteoApi
import com.example.weather_app.data.repository.WeatherRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())   // <-- IMPORTANT
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(OpenMeteoApi.BASE_URL)
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api: OpenMeteoApi = retrofit.create(OpenMeteoApi::class.java)

    val weatherRepository = WeatherRepository(api)
}
