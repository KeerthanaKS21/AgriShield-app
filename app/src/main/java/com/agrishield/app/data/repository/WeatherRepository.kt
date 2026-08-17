package com.agrishield.app.data.repository

import com.agrishield.app.BuildConfig
import com.agrishield.app.data.model.ForecastItem
import com.agrishield.app.data.model.WeatherData
import com.agrishield.app.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class WeatherRepository {

    private val weatherApi = RetrofitClient.weatherApi

    private val _currentWeather = MutableStateFlow<WeatherData?>(null)
    val currentWeather: StateFlow<WeatherData?> = _currentWeather.asStateFlow()

    private val _forecast = MutableStateFlow<List<ForecastItem>>(emptyList())
    val forecast: StateFlow<List<ForecastItem>> = _forecast.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun fetchWeather(lat: Double, lon: Double, customApiKey: String? = null): Result<WeatherData> = withContext(Dispatchers.IO) {
        _isLoading.value = true
        _error.value = null

        val apiKey = customApiKey?.ifBlank { null }
            ?: BuildConfig.OPENWEATHER_API_KEY.ifBlank { null }
            ?: "DEMO_KEY"

        try {
            val weatherResponse = weatherApi.getCurrentWeather(lat = lat, lon = lon, apiKey = apiKey)
            val current = WeatherData(
                cityName = weatherResponse.name,
                latitude = weatherResponse.coord.lat,
                longitude = weatherResponse.coord.lon,
                temperatureCelsius = weatherResponse.main.temp,
                feelsLikeCelsius = weatherResponse.main.feelsLike,
                humidityPercentage = weatherResponse.main.humidity,
                windSpeedKmh = weatherResponse.wind.speed * 3.6, // m/s to km/h
                condition = weatherResponse.weather.firstOrNull()?.main ?: "Clear",
                conditionDescription = weatherResponse.weather.firstOrNull()?.description ?: "",
                iconCode = weatherResponse.weather.firstOrNull()?.icon ?: "01d",
                rainMmLast3h = weatherResponse.rain?.rain3h ?: weatherResponse.rain?.rain1h ?: 0.0,
                timestamp = System.currentTimeMillis()
            )
            _currentWeather.value = current

            // Also fetch 5-day forecast
            try {
                val forecastResponse = weatherApi.get5DayForecast(lat = lat, lon = lon, apiKey = apiKey)
                val forecastList = forecastResponse.list.map { item ->
                    ForecastItem(
                        dateTimeEpoch = item.dt * 1000L,
                        tempCelsius = item.main.temp,
                        humidity = item.main.humidity,
                        rainProbabilityPercent = (item.pop * 100).toInt(),
                        condition = item.weather.firstOrNull()?.main ?: "Clear",
                        iconCode = item.weather.firstOrNull()?.icon ?: "01d"
                    )
                }
                _forecast.value = forecastList
            } catch (e: Exception) {
                // Forecast error is non-fatal if current weather succeeded
                e.printStackTrace()
            }

            _isLoading.value = false
            Result.success(current)
        } catch (e: Exception) {
            _isLoading.value = false
            val errorMsg = "Weather data unavailable. Please verify API key or internet connection."
            _error.value = errorMsg
            Result.failure(Exception(errorMsg, e))
        }
    }

    fun setDirectWeather(weather: WeatherData) {
        _currentWeather.value = weather
    }
}
