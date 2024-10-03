package com.myapp.configuration

import com.typesafe.config.Config
import org.slf4j.{Logger, LoggerFactory}
import io.circe.generic.auto._
import io.circe.config.syntax._

case class WeatherApiConfig(baseUrl: String, apiKey: String)

object WeatherApiConfig {
  val log: Logger = LoggerFactory.getLogger(KafkaStreamsAppConfig.getClass)

  def apply(config: Config): WeatherApiConfig = {
    config.getConfig("weatherApi").as[WeatherApiConfig] match {
      case Left(error) =>
        log.error("error reading weather-api config", error)
        throw error
      case Right(value) =>
        value
    }
  }
}