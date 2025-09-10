package com.example.weather_app


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weather_app.viewmodel.WeatherViewModel
import com.example.weather_app.di.NetworkModule
import com.example.weather_app.screens.WeatherScreen
import com.example.weather_app.viewmodel.WeatherViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = WeatherViewModelFactory(NetworkModule.weatherRepository,application)

        setContent {
            val vm: WeatherViewModel = viewModel(factory = factory)
            WeatherScreen(vm)
        }
    }
}

