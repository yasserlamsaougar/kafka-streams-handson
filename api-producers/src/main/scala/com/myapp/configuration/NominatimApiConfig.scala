package com.myapp.configuration

import com.typesafe.config.Config
import org.slf4j.{Logger, LoggerFactory}
import io.circe.generic.auto._
import io.circe.config.syntax._

case class NominatimApiConfig(baseUrl: String)



object NominatimApiConfig {
  val log: Logger = LoggerFactory.getLogger(KafkaStreamsAppConfig.getClass)

  def apply(config: Config): NominatimApiConfig = {
    config.getConfig("nominatimApi").as[NominatimApiConfig] match {
      case Left(error) =>
        log.error("error reading nominatima-api config", error)
        throw error
      case Right(value) =>
        value
    }
  }
}