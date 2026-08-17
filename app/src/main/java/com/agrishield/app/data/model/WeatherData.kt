package com.agrishield.app.data.model

import com.google.gson.annotations.SerializedName

data class WeatherData(
    val cityName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val temperatureCelsius: Double = 0.0,
    val feelsLikeCelsius: Double = 0.0,
    val humidityPercentage: Int = 0,
    val windSpeedKmh: Double = 0.0,
    val condition: String = "",
    val conditionDescription: String = "",
    val iconCode: String = "01d",
    val rainMmLast3h: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class ForecastItem(
    val dateTimeEpoch: Long = 0,
    val tempCelsius: Double = 0.0,
    val humidity: Int = 0,
    val rainProbabilityPercent: Int = 0,
    val condition: String = "",
    val iconCode: String = ""
)

// OpenWeatherMap JSON DTOs
data class OpenWeatherResponse(
    @SerializedName("name") val name: String,
    @SerializedName("coord") val coord: CoordDto,
    @SerializedName("main") val main: MainDto,
    @SerializedName("wind") val wind: WindDto,
    @SerializedName("weather") val weather: List<WeatherDto>,
    @SerializedName("rain") val rain: RainDto? = null,
    @SerializedName("dt") val dt: Long
)

data class CoordDto(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
)

data class MainDto(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("humidity") val humidity: Int
)

data class WindDto(
    @SerializedName("speed") val speed: Double
)

data class WeatherDto(
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class RainDto(
    @SerializedName("1h") val rain1h: Double? = null,
    @SerializedName("3h") val rain3h: Double? = null
)

data class OpenWeatherForecastResponse(
    @SerializedName("list") val list: List<ForecastItemDto>,
    @SerializedName("city") val city: CityDto
)

data class ForecastItemDto(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: MainDto,
    @SerializedName("weather") val weather: List<WeatherDto>,
    @SerializedName("pop") val pop: Double = 0.0 // Probability of precipitation 0.0 - 1.0
)

data class CityDto(
    @SerializedName("name") val name: String
)
