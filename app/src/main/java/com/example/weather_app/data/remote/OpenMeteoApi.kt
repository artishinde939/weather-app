package com.example.weather_app.data.remote

import com.example.weather_app.data.remote.dto.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        // Ask only for a few current variables to keep payload small
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,wind_speed_10m"
    ): WeatherResponse

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
    }
}
