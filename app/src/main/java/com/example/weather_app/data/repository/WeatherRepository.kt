package com.example.weather_app.data.repository

import com.example.weather_app.data.remote.OpenMeteoApi
import com.example.weather_app.data.remote.dto.WeatherResponse

class WeatherRepository(
    private val api: OpenMeteoApi
) {
    suspend fun fetchCurrentWeather(
        latitude: Double,
        longitude: Double
    ): WeatherResponse {
        return try {
            api.getCurrentWeather(latitude, longitude)
        } catch (e: Exception) {
            throw e // Let the ViewModel handle the exception
        }
    }
}