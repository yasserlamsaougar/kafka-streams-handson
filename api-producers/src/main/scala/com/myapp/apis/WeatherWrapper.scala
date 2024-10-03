package com.myapp.apis

import cats.effect.{IO, Resource}
import org.http4s.{MediaType, Uri}
import org.http4s.client.Client
import org.http4s.Method.{GET, POST}
import org.http4s.client.dsl.io._
import org.http4s.headers.Accept
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder

class WeatherWrapper(baseUrl: String, apiKey: String, clientRes: Resource[IO, Client[IO]]) {




  private case class Locations(locations: Set[Location])
  private case class Location(q: String, custom_id: String)

  object Response {
    case class BulkData(bulk: List[QueryWrapper])

    case class QueryWrapper(query: Query)

    case class Query(
                      custom_id: String,
                      q: String,
                      location: Location,
                      current: Current
                    )

    case class Location(
                         name: String,
                         region: String,
                         country: String,
                         lat: Double,
                         lon: Double,
                         tz_id: String,
                         localtime_epoch: Long,
                         localtime: String
                       )

    case class Current(
                        last_updated_epoch: Long,
                        last_updated: String,
                        temp_c: Double,
                        temp_f: Double,
                        is_day: Int,
                        condition: Condition,
                        wind_mph: Double,
                        wind_kph: Double,
                        wind_degree: Int,
                        wind_dir: String,
                        pressure_mb: Double,
                        pressure_in: Double,
                        precip_mm: Double,
                        precip_in: Double,
                        humidity: Int,
                        cloud: Int,
                        feelslike_c: Double,
                        feelslike_f: Double,
                        vis_km: Double,
                        vis_miles: Double,
                        uv: Double,
                        gust_mph: Double,
                        gust_kph: Double
                      )

    case class Condition(
                          text: String,
                          icon: String,
                          code: Int
                        )

  }

  def getCurrentWeather(cityNames: Seq[String]): IO[Map[String, String]] = {
    val uri = if(cityNames.length > 1) {
      POST(
        Locations(cityNames.map(cityName => Location(q = cityName, custom_id = cityName)).toSet).asJson.noSpaces,
        Uri.unsafeFromString(s"$baseUrl/current.json?key=$apiKey&q=bulk&aqi=yes")
      )
    } else {
      GET(
        Uri.unsafeFromString(s"$baseUrl/current.json?key=$apiKey&q=${cityNames.head}&aqi=yes"),
        Accept(MediaType.application.json)
      )
    }
    clientRes.use(client => client.expect[Response.BulkData](uri))
      .map(response => {
        response.bulk.map(wrapper => (wrapper.query.q, wrapper.query.asJsonObject.remove("custom_id").remove("q").asJson.noSpaces.replaceAll("us-epa-index", "us_epa_index")
          .replaceAll("gb-defra-index", "gb_defra_index"))).toMap
      })
  }
}

