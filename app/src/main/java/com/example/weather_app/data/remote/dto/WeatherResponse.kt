package com.example.weather_app.data.remote.dto

import com.squareup.moshi.Json

data class WeatherResponse(
    val latitude: Double?,
    val longitude: Double?,
    val timezone: String?,
    val elevation: Double?,
    val current: Current?
)


data class Current(
    val time: String?,
    @Json(name = "temperature_2m") val temperature2m: Double?,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int?,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double?
)
