package com.myapp.apis

object Errors {

  case class ApiError(message: String, originCause: String) extends Throwable

}
