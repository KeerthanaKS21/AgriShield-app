package com.agrishield.app.data.network

import com.agrishield.app.data.model.OpenWeatherForecastResponse
import com.agrishield.app.data.model.OpenWeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): OpenWeatherResponse

    @GET("forecast")
    suspend fun get5DayForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): OpenWeatherForecastResponse
}
