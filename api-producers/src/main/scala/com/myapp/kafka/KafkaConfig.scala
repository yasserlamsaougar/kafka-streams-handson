package com.myapp.kafka

import cats.effect.IO
import com.google.protobuf.Message
import com.myapp.configuration.KafkaStreamsAppConfig
import fs2.kafka.{Acks, ProducerSettings, Serializer}
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer

import scala.jdk.CollectionConverters._

class KafkaConfig(kafkaStreamsConfig: KafkaStreamsAppConfig) {

  def producerSettings[T <: Message]: ProducerSettings[IO, String, T] = {
    val keySerializer = Serializer[IO, String]
    val valueSerializer = Serializer.instance[IO, T] { (topic, headers, value) =>
      val protobufSerializer = new KafkaProtobufSerializer[T]()
      protobufSerializer.configure(Map(
        "schema.registry.url" -> kafkaStreamsConfig.schemaRegistryUrl
      ).asJava, false)
      IO.delay(protobufSerializer.serialize(topic, headers.asJava, value))
    }

    ProducerSettings[IO, String, T](keySerializer, valueSerializer)
      .withBootstrapServers(kafkaStreamsConfig.bootstrapServers)
      .withAcks(Acks.All)
      .withEnableIdempotence(true)
      .withMaxInFlightRequestsPerConnection(5)
      .withRetries(3)
      .withProperties((
        "schema.registry.url", kafkaStreamsConfig.schemaRegistryUrl
      ))

  }

}