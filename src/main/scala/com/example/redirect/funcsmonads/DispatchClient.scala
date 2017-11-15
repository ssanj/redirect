package com.example.redirect.funcsmonads

import dispatch._, Defaults._
import scala.util.Try

object DispatchClient {

  class Dispatch

  //ignore SSL cert errors
  def connect(): Http = Http.withConfiguration(_.setAcceptAnyCertificate(true))

  def shutdown(http: Http): Unit = http.shutdown()

  def fetch(http: Http): Uri => Future[HttpResult[Dispatch]] = uri => {
    val svc =
      if (uri.value.startsWith("https")) url(uri.value).secure
      else url(uri.value)

    http(svc).map { response =>
      new HttpResult[Dispatch] {
        override val statusCode: Option[StatusCode]    =
          Try(response.getStatusCode).fold[Option[StatusCode]](_ => None, sc => Option(StatusCode(sc)))

        override val headers: Map[String, Seq[String]] = {
          import scala.collection.JavaConverters._
            Try {
              val entries: List[java.util.Map.Entry[String, String]] =
                response.getHeaders.entries().asScala.toList
              entries.groupBy(_.getKey.toString).mapValues(_.map(_.getValue.toString))
            }.fold(_ => Map.empty[String, Seq[String]], identity)

        }
      }
    }
  }
}