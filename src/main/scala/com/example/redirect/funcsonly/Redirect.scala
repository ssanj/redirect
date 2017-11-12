package com.example.redirect.funcsonly

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

import org.slf4j.LoggerFactory

object Redirect {

  val LOGGER = LoggerFactory.getLogger("com.example.redirect.funcsonly.Redirect");

  private[funcsonly] def nextInstr(currentUri: Uri, statusCode: Option[StatusCode], location: Option[Location]): NextInstr =
    (currentUri, statusCode, location) match {
      case (uri, None, _) =>  Error(NoStatusCode(uri))
      case (uri, Some(sc), _) if (sc.value >= 200 && sc.value < 300) => Done(uri, sc)
      case (uri, Some(sc), loc) if (sc.value == 301 || sc.value == 302) => loc.fold[NextInstr](Error(NoLocation(uri, sc)))(l => Continue(uri, l.value))
      case (uri, Some(sc), _) => Error(UnhandledStatusCode(uri, sc))
  }

  def apply[A](uri: Uri, fetch: Uri => Future[HttpResult[A]])(implicit ec: ExecutionContext): Future[Unit] = {
     fetch(uri).flatMap { result =>
        nextInstr(uri, result.statusCode, result.headers.get("Location").flatMap(_.headOption.map(uri => Location(Uri(uri))))) match {
         case Done(Uri(prevLocation), statusCode)                   => done(s"$prevLocation")
         case Continue(prevLocationUri@Uri(prevLocation), nextLocation)             => output(s"$prevLocation", LOGGER.info _).flatMap {_ => Redirect(resolveRelativeUri(prevLocationUri, nextLocation), fetch) }
         case Error(NoLocation(Uri(uri), StatusCode(sc)))      => error(s"No location header found at: $uri, status code: $sc")
         case Error(NoStatusCode(Uri(uri)))                    => error(s"No status code returned for: $uri")
         case Error(UnhandledStatusCode(uri, StatusCode(sc)))  => error(s"$uri returned unhandled status code: $sc")
       }
     } recover {
       case NonFatal(e) =>
        // error(s"unexpected error: ${e.getMessage}").map(_ => e.printStackTrace())
        LOGGER.error(s"unexpected error: ${e.getMessage}", e)
     }
  }

  private def resolveRelativeUri(prevLocation: Uri, nextLocation: Uri): Uri = {
    if (!nextLocation.value.startsWith("http")) {
      //TODO: Fix
      if (prevLocation.value.endsWith("/")) Uri(s"${prevLocation.value}${nextLocation.value}")
      else Uri(s"${prevLocation.value}/${nextLocation.value}")
    } else nextLocation
  }

  private def output(message: String, log: String => Unit)(implicit ec: ExecutionContext): Future[Unit] = Future(log(message))

  private def done(message: String)(implicit ec: ExecutionContext): Future[Unit] = output(message + "\n--Done--", LOGGER.info _)

  private def error(message: String)(implicit ec: ExecutionContext): Future[Unit] = output(message + "\n--Error--", LOGGER.error _)
}