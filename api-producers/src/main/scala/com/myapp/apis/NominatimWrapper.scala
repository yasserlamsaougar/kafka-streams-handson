package com.myapp.apis

import Errors.ApiError
import com.myapp.apis.NominatimWrapper.ReverseResponse

import java.net.URI
import java.net.http.HttpResponse.{BodyHandler, BodySubscribers}
import java.net.http.{HttpClient, HttpRequest}
import java.nio.charset.StandardCharsets
import scala.concurrent.{Future, Promise}

class NominatimWrapper(baseUrl: String, httpClient: HttpClient) {

  def reverseCity(latitude: Double, longitude: Double): Future[ReverseResponse] = {
    val promise = Promise.apply[ReverseResponse]()
    val request = HttpRequest.newBuilder()
      .GET()
      .uri(URI.create(s"${baseUrl.stripSuffix("/")}/reverse?lat=$latitude&lon=$longitude&format=jsonv2&addressdetails=0&namedetails=0&extratags=0&zoom=10"))
      .build()
    val theFutureResponse = httpClient.sendAsync(request, NominatimWrapper.responseHandler)
    theFutureResponse.handleAsync((value, error) => {
      if (error != null) {
        promise.failure(error)
      } else {
        value.body() match {
          case Left(apiError: ApiError) =>
            promise.failure(apiError)
          case Right(value) =>
            promise.success(value)
        }
      }
    })
    promise.future
  }
}

object NominatimWrapper {

  import io.circe.generic.auto._


  case class ReverseResponse(display_name: String, name: String, addresstype: String, boundingbox: Array[Double])

  val responseHandler: BodyHandler[Either[ApiError, ReverseResponse]] = (responseInfo) => {
    if (responseInfo.statusCode() == 200) {
      BodySubscribers.mapping[String, Either[ApiError, ReverseResponse]](BodySubscribers.ofString(StandardCharsets.UTF_8), (input) => {
        io.circe.parser.decode[ReverseResponse](input).left.map {
          error => {
            ApiError("unable to decode response", error.getMessage)
          }
        }
      })
    } else {
      BodySubscribers.mapping[String, Either[ApiError, ReverseResponse]](BodySubscribers.ofString(StandardCharsets.UTF_8), (input) => {
        Left[ApiError, ReverseResponse](ApiError(s"received status code ${responseInfo.statusCode()}", input))
      })
    }
  }
}
