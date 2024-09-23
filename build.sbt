ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "kafka-streams-handson",
    libraryDependencies ++= Seq(
      "org.apache.kafka" %% "kafka-streams-scala" % "3.7.1",
      "org.apache.kafka" % "kafka-clients" % "3.7.1",
      "org.slf4j" % "slf4j-simple" % "2.0.13",
      "io.confluent" % "kafka-streams-protobuf-serde" % "7.7.1",
      "io.confluent" % "kafka-schema-registry-client" % "7.7.1",
      "com.typesafe" % "config" % "1.4.3",
    )
  )

Compile / PB.targets := Seq(
  PB.gens.java -> (Compile / sourceManaged).value,
  scalapb.gen(javaConversions=true) -> (Compile / sourceManaged).value
)
