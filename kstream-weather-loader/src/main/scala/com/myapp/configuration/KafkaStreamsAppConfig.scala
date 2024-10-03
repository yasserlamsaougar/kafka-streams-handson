package com.myapp.configuration

import com.typesafe.config.Config
import io.circe.generic.auto._
import io.circe.config.syntax._
import org.slf4j.LoggerFactory

case class KafkaStreamsAppConfig(applicationId: String, bootstrapServers: String, schemaRegistryUrl: String, defaultKeySerde: String, defaultValueSerde: String, topics: Topics)


case class Topics(inputTopic: String, outputTopic: String)


object KafkaStreamsAppConfig {
  val log = LoggerFactory.getLogger(KafkaStreamsAppConfig.getClass)
  def apply(config: Config): KafkaStreamsAppConfig = {
    config.getConfig("kafkaStreamsApp").as[KafkaStreamsAppConfig] match {
      case Left(error) =>
        log.error("error reading kafka-streams-app config", error)
        throw error
      case Right(value) =>
        value
    }
  }
}