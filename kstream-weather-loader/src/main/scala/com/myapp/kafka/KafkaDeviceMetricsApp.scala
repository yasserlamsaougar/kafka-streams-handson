package com.myapp.kafka

import com.myapp.InputEventOuterClass.InputEvent
import com.typesafe.config.{Config, ConfigFactory}
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

import java.util.Properties
import scala.jdk.CollectionConverters._

object KafkaDeviceMetricsApp extends App {

  // Load configuration
  val config: Config = ConfigFactory.load()

  // Extract Kafka Streams configurations
  val applicationId = config.getString("kafka-streams-app.application-id")
  val bootstrapServers = config.getString("kafka-streams-app.bootstrap-servers")
  val schemaRegistryUrl = config.getString("kafka-streams-app.schema-registry-url")

  val inputTopic = config.getString("kafka-streams-app.input-topic")
  val outputTopic = config.getString("kafka-streams-app.output-topic")

  // Set up Kafka Streams properties
  val props: Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId)
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, config.getString("kafka-streams-app.default-key-serde"))
    p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, config.getString("kafka-streams-app.default-value-serde"))
    p.put("schema.registry.url", schemaRegistryUrl)
    // Add any additional properties if needed
    p
  }

  // Initialize Schema Registry SerDe
  implicit val protobufSerde: KafkaProtobufSerde[InputEvent] = new KafkaProtobufSerde[InputEvent]()
  implicit val stringSerde: Serde[String] = Serdes.String()

  // Configure SerDe with Schema Registry URL
  protobufSerde.configure(Map("schema.registry.url" -> schemaRegistryUrl, KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE -> classOf[InputEvent].getName).asJava, false)

  // Create the StreamsBuilder
  val builder: StreamsBuilder = new StreamsBuilder()

  // Define the input stream with Protobuf SerDe
  val sourceStream: KStream[String, InputEvent] = builder.stream[String, InputEvent](inputTopic)

  // Example processing: filter and map values
  val processedStream: KStream[String, InputEvent] = sourceStream
    .filter((key, value) => value.getCount > 10)
    .mapValues { value =>
      val updatedValue = value.toBuilder.setValue(value.getValue * 2).build()
      updatedValue
    }

  // Output the processed stream to the output topic
  processedStream.to(outputTopic)

  // Build the topology
  val topology = builder.build()

  // Initialize Kafka Streams
  val streams = new KafkaStreams(topology, props)

  // Add shutdown hook to gracefully close the streams application
  sys.ShutdownHookThread {
    streams.close()
  }

  // Start the Kafka Streams application
  streams.start()

  println(s"Kafka Streams application '$applicationId' is running...")
}
