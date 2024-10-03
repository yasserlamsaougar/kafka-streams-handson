package com.myapp.helpers

import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat

import scala.util.Try

object JsonHelper {


  def jsonToProto[M <: Message](json: String, builder: Message.Builder): Either[Throwable,  M] = {
    Try {
      JsonFormat.parser()
        .merge(json, builder)
      builder.build().asInstanceOf[M]
    }.toEither
  }
}
