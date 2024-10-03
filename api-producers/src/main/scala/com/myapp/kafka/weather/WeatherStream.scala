package com.myapp.kafka.weather

import cats.effect.IO
import com.myapp.apis.WeatherWrapper
import fs2._

import scala.concurrent.duration.FiniteDuration


object WeatherStream {

  def weatherDataStream(weatherWrapper: WeatherWrapper, cityNames: Seq[String], interval: FiniteDuration): Stream[IO, Either[Throwable, Map[String, String]]] = {
    Stream.fixedRateStartImmediately[IO](interval) // Emit a tick every `interval`
      .evalMap { _ =>
        weatherWrapper.getCurrentWeather(cityNames).attempt // Fetch weather data and capture errors
      }
  }
}
