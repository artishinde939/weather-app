package com.example.weather_app.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather_app.data.repository.WeatherRepository
import com.example.weather_app.utils.DateUtils.getCityFromCoordinates
import com.example.weather_app.utils.NetworkUtils.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

sealed interface WeatherUiState {
    object Idle : WeatherUiState
    object Loading : WeatherUiState
    data class Success(
        val cityLabel: String,
        val temperatureC: String,
        val humidity: String,
        val wind: String,
        val time: String
    ) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState
    // In WeatherViewModel.kt
    fun loadWeather(lat: Double?, lon: Double?, label: String = "Selected Location") {
        _uiState.value = WeatherUiState.Loading
        if (lat == null || lon == null) {
            _uiState.value = WeatherUiState.Error("Invalid coordinates.")
            return
        }
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            _uiState.value = WeatherUiState.Error("Invalid coordinates. Latitude must be between -90 and 90, and longitude between -180 and 180.")
            return
        }
        viewModelScope.launch {
            try {
                // Check network availability before making the API call
                if (!isNetworkAvailable(context)) {
                    _uiState.value = WeatherUiState.Error("No internet connection. Please check your network and try again.")
                    return@launch
                }
                val weatherData = withContext(Dispatchers.IO) {
                    Log.i("loadWeather", "loadWeather: $lat $lon")
                    repository.fetchCurrentWeather(lat, lon)
                }

                // Switch back to main thread for UI updates
                withContext(Dispatchers.Main) {
                    val current = weatherData.current
                    if (current == null) {
                        _uiState.value = WeatherUiState.Error("No current weather data available")
                        return@withContext
                    }

                    val cityName = getCityFromCoordinates(lat, lon, context) ?: label

                    _uiState.value = WeatherUiState.Success(
                        cityLabel = cityName,
                        temperatureC = "${current.temperature2m ?: "-"} °C",
                        humidity = "${current.relativeHumidity2m ?: "-"} %",
                        wind = "${current.windSpeed10m ?: "-"} m/s",
                        time = current.time ?: "-"
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleError(e)
                }
            }
        }
    }

    private fun handleError(e: Throwable) {
        val errorMessage = when (e) {
            is HttpException -> when (e.code()) {
                400 -> "Bad request. Please check your input."
                401 -> "Unauthorized. Please check your API key."
                404 -> "Location not found. Please try another location."
                429 -> "Too many requests. Please try again later."
                500 -> "Server error. Please try again later."
                else -> "Failed to fetch weather data. Code: ${e.code()}"
            }
            is IOException -> "Network error. Please check your internet connection."
            else -> "An unexpected error occurred: ${e.message ?: "Unknown error"}"
        }
        _uiState.value = WeatherUiState.Error(errorMessage)
        Log.e("WeatherViewModel", "Error: ${e.message}", e)
    }

}
