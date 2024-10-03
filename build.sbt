ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

val circeVersion = "0.14.3"
val http4sVersion = "1.0.0-M41"

lazy val root = (project in file("."))
  .aggregate(kstreamWeatherLoader, apiProducers)

scalacOptions ++= Seq(
  "-Ymacro-annotations",
  "-Ypartial-unification"
)
lazy val kstreamWeatherLoader = (project in file("kstream-weather-loader"))
  .settings(
    name := "kstream-weather-loader",
    libraryDependencies ++= Seq(
      "org.apache.kafka" %% "kafka-streams-scala" % "3.7.1",
      "org.apache.kafka" % "kafka-clients" % "3.7.1",
      "org.slf4j" % "slf4j-simple" % "2.0.13",
      "io.confluent" % "kafka-streams-protobuf-serde" % "7.7.1",
      "io.confluent" % "kafka-schema-registry-client" % "7.7.1",
      "com.typesafe" % "config" % "1.4.3",
      "io.circe" %% "circe-config" % "0.10.1"
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
      "io.circe" %% "circe-generic-extras"
    ).map(_ % circeVersion),
    Compile / PB.targets := Seq(
      PB.gens.java -> (Compile / sourceManaged).value,
      scalapb.gen(javaConversions = true) -> (Compile / sourceManaged).value
    )
  )


lazy val apiProducers = (project in file("api-producers"))
  .settings(
    name := "api-producers",
    libraryDependencies ++= Seq(
      "org.apache.kafka" % "kafka-clients" % "3.7.1",
      "org.slf4j" % "slf4j-simple" % "2.0.13",
      "io.confluent" % "kafka-streams-protobuf-serde" % "7.7.1",
      "io.confluent" % "kafka-schema-registry-client" % "7.7.1",
      "com.typesafe" % "config" % "1.4.3",
      "io.circe" %% "circe-config" % "0.10.1",
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
      "io.circe" %% "circe-generic-extras"
    ).map(_ % circeVersion),
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "3.11.0",
      "co.fs2" %% "fs2-io" % "3.11.0",
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" %  http4sVersion,
      "org.http4s" %% "http4s-dsl"   % http4sVersion,
      "com.github.fd4s" %% "fs2-kafka" % "3.5.1",
      "org.typelevel" %% "log4cats-slf4j"   % "2.7.0",  // Direct Slf4j Support - Recommended
    ),
    Compile / PB.targets := Seq(
      PB.gens.java -> (Compile / sourceManaged).value,
      scalapb.gen(javaConversions = true) -> (Compile / sourceManaged).value
    )
  )

Compile / PB.targets := Seq(
  PB.gens.java -> (Compile / sourceManaged).value,
  scalapb.gen(javaConversions = true) -> (Compile / sourceManaged).value
)
