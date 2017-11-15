package com.example.redirect
package funcsmonads

import DispatchClient.Dispatch
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import org.slf4j.LoggerFactory
import cats.instances.future._

object Runner extends LogSupport {
  override val LOGGER = LoggerFactory.getLogger(Runner.getClass);

  def main(args: Array[String]): Unit = {
      val uri = Uri("http://uniqueimprints.com")
      // val uri = Uri("http://realcommercial.com.au")
      val connection = DispatchClient.connect()
      val resultFE: Future[Either[ErrorType, Done]] =
          Redirect[Future, Dispatch](uri, DispatchClient.fetch(connection), Vector.empty[Navigation])

      val resultE: Either[ErrorType, Done] = Await.result(resultFE, 20.seconds)

      resultE match {
        case Left(NoLocation(uri, sc))           => error(s"No location header found at: $uri, status code: $sc")
        case Left(NoStatusCode(uri))             => error(s"No status code returned for: $uri")
        case Left(UnhandledStatusCode(uri, sc))  => error(s"$uri returned unhandled status code: $sc")
        case Left(UnhandledError(err))           => error(s"unhandled error: ${stacktrace(err)}")
        case Right(Done(results))                => done(results)
      }

      DispatchClient.shutdown(connection)
      LOGGER.info(s"Runner exited")
  }
}