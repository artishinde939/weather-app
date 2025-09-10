@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION")

package com.example.weather_app.utils

import android.content.Context
import android.location.Geocoder
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    fun convertToReadableDate(dateTimeString: String, timeZone: String = "Asia/Kolkata"): String {
        // Parse the input date string
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateTimeString)

        // Convert the date into the desired format
        val outputFormat = SimpleDateFormat("MMMM dd, yyyy, h:mm a z", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getTimeZone(timeZone)

        return outputFormat.format(date)
    }

    // Function to get the city name from latitude and longitude using Geocoder
    fun getCityFromCoordinates(latitude: Double, longitude: Double, context: Context): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses != null && addresses.isNotEmpty()) {
                // Extract the city from the address components
                addresses[0].locality // "locality" is typically the city
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}