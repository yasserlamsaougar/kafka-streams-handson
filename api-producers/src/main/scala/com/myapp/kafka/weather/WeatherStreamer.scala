package com.myapp.kafka.weather

import cats.effect.IO
import com.myapp.apis.WeatherWrapper
import com.myapp.helpers.JsonHelper
import com.myapp.kafka.KafkaConfig
import com.myapp.weather.WeatherConditionOuterClass
import com.myapp.weather.WeatherConditionOuterClass.WeatherCondition
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerRecords}
import fs2._

import scala.concurrent.duration.FiniteDuration

object WeatherStreamer {

  def weatherToKafkaStream(weatherWrapper: WeatherWrapper, cityNames: Seq[String], interval: FiniteDuration)(kafkaConfig: KafkaConfig): Stream[IO, Unit] = {
    // Create the producer settings for Kafka using Protobuf
    val producerSettings = kafkaConfig.producerSettings[WeatherCondition]

    // Create the weather data stream which fetches data every specified interval
    val weatherStream: Stream[IO, Either[Throwable, Map[String, String]]] = WeatherStream.weatherDataStream(weatherWrapper, cityNames, interval)

    // Create the Kafka producer stream
    KafkaProducer.stream(producerSettings).flatMap { producer =>
      // Transform the weather stream to a protobuf message and publish it to Kafka
      weatherStream.evalMap {
        case Right(response) =>
          // Convert JSON response to Protobuf
          val records = response.flatMap {
            case (key, value) =>
              JsonHelper.jsonToProto[WeatherCondition](value, WeatherCondition.newBuilder()) match {
                case Right(proto) =>
                  // Construct the Kafka ProducerRecord
                  // Produce the record to Kafka and handle success or failure
                  Option(ProducerRecord("weather-topic", key, proto))
                case Left(error) =>
                  println(error)
                  Option.empty[ProducerRecord[String, WeatherConditionOuterClass.WeatherCondition]]
              }
          }.toSeq
          if(records.isEmpty) {
            // Handle the error from the weather stream itself (e.g., HTTP request failure)
            IO(println(s"Failed to fetch weather data"))
          } else {
            producer.produce(ProducerRecords(records)).flatten
              .attempt
              .flatMap {
                case Right(result) =>
                  IO(println(s"Successfully produced record to Kafka: $result"))
                case Left(e) =>
                  IO(println(s"Failed to produce record to Kafka: ${e.getMessage}"))
              }
          }
        case Left(error) =>
          // Handle the error from the weather stream itself (e.g., HTTP request failure)
          IO(println(s"Failed to fetch weather data: ${error.getMessage}"))
      }
    }
  }
}
