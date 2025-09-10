package com.example.weather_app.screens

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.weather_app.viewmodel.WeatherUiState
import com.example.weather_app.viewmodel.WeatherViewModel
import com.example.weather_app.utils.DateUtils.convertToReadableDate
import com.example.weather_app.utils.NetworkUtils
import kotlinx.coroutines.launch

/**
 * Composable function to display a weather fetching app.
 * @param viewModel [WeatherViewModel] to be used for loading data
 * @return [Unit] (nothing)
 */
@SuppressLint("ObsoleteSdkInt")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val context = LocalContext.current


    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()


    // Default to New Delhi (India) — change as you like
    var lat by remember { mutableStateOf(TextFieldValue("40.710335")) }
    var lon by remember { mutableStateOf(TextFieldValue("-73.99309")) }
    var label by remember { mutableStateOf(TextFieldValue("")) }

    // Network status toast
    val networkStatus = remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        // Check initial network status
        val isOnline = NetworkUtils.isNetworkAvailable(context)
        networkStatus.value = isOnline

        // Show initial status
        val status = if (isOnline) "Online" else "Offline"
        Toast.makeText(context, "You are $status", Toast.LENGTH_SHORT).show()

        // Listen for network changes
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(object :
                ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    if (networkStatus.value != true) {
                        networkStatus.value = true
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "You are back online", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

                override fun onLost(network: android.net.Network) {
                    if (networkStatus.value != false) {
                        networkStatus.value = false
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "You are offline", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }


    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is WeatherUiState.Error -> {
                // Show error message in a snackBar
                snackBarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
            }

            else -> { /* Do nothing for other states */
            }
        }
    }

    // Initial load
    LaunchedEffect(Unit) {
        viewModel.loadWeather(
            lat.text.toDoubleOrNull() ?: 40.710335,
            lon.text.toDoubleOrNull() ?: -73.99309,
            label.text.ifEmpty { "" }
        )
    }

    // Show a loading indicator while data is being fetched
    if (uiState is WeatherUiState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            TopAppBar(title = { Text("Weather Fetch App", fontWeight = FontWeight.SemiBold) })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        viewModel.loadWeather(
                            lat.text.toDoubleOrNull(),
                            lon.text.toDoubleOrNull(),
                            label.text.ifEmpty { "Selected Location" }
                        )
                    }
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            // Inputs (to test different coordinates quickly)

            OutlinedTextField(
                value = lat,
                onValueChange = { newLat ->
                    lat = newLat
                },
                label = { Text("Latitude") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number, // Changed to Number for better keyboard
                    imeAction = ImeAction.Done
                ),
            )

            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = lon,
                onValueChange = { newLat ->
                    lon = newLat
                },
                label = { Text("Longitude") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number, // Changed to Number for better keyboard
                    imeAction = ImeAction.Done
                ),
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = label, onValueChange = { label = it },
                label = { Text("Label (optional)") }, singleLine = true
            )
            Spacer(modifier = Modifier.height(20.dp))


            when (val state = uiState) {
                is WeatherUiState.Success -> {
                    Column {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Location: ${state.cityLabel}")
                                Text("Temperature: ${state.temperatureC}")
                                Text("Humidity: ${state.humidity}")
                                Text("Wind Speed: ${state.wind}")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Last updated: ${convertToReadableDate(state.time)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is WeatherUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "❌ Error",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.message,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                WeatherUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                WeatherUiState.Idle -> {
                    Text("Idle")
                }
            }
        }
    }
}