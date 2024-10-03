package com.myapp

import cats.effect.{IO, IOApp, Resource}
import com.myapp.apis.WeatherWrapper
import com.myapp.configuration.{KafkaStreamsAppConfig, WeatherApiConfig}
import com.myapp.kafka.KafkaConfig
import com.myapp.kafka.weather.WeatherStreamer
import com.typesafe.config.{Config, ConfigFactory}
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import scala.concurrent.duration.DurationInt

object Main extends IOApp.Simple {


  private implicit val loggerFactory: LoggerFactory[IO] =
    Slf4jFactory.create[IO]

  val config: Config = ConfigFactory.load()

  private val kafkaStreamsAppConfig = KafkaStreamsAppConfig(config)

  // Extract Kafka Streams configurations
  val bootstrapServers: String = kafkaStreamsAppConfig.bootstrapServers
  val schemaRegistryUrl: String = kafkaStreamsAppConfig.schemaRegistryUrl

  private val weatherApiConfig = WeatherApiConfig(config)
  val httpClient: Resource[IO, Client[IO]] = EmberClientBuilder
    .default[IO]
    .build
  val weatherApiWrapper = new WeatherWrapper(weatherApiConfig.baseUrl, weatherApiConfig.apiKey, httpClient)
  val kafkaConfig = new KafkaConfig(kafkaStreamsAppConfig)

  override def run: IO[Unit] = {
    WeatherStreamer
      .weatherToKafkaStream(weatherApiWrapper, Seq("Argenteuil", "Paris", "Lyon", "Brest", "Marseille", "Grenoble", "Rennes", "Bordeaux", "Nantes"), 5.seconds)(kafkaConfig)
      .compile
      .drain
  }
}
