package com.example.redirect.funcsonly

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

import org.slf4j.LoggerFactory

object Redirect {

  private val LOGGER = LoggerFactory.getLogger(Redirect.getClass);

  private[funcsonly] def nextInstr(currentUri: Uri, statusCode: Option[StatusCode], location: Option[Location]): NextInstr =
    (currentUri, statusCode, location) match {
      case (uri, None, _)                                               => Error(NoStatusCode(uri))
      case (uri, Some(sc), _)   if (sc.value / 100 == 2)                => Done(uri, sc)
      case (uri, Some(sc), loc) if (sc.value == 301 || sc.value == 302) => doContinue(uri, sc, loc)
      case (uri, Some(sc), _)                                           => Error(UnhandledStatusCode(uri, sc))
  }

  private def doContinue(uri: Uri, sc: StatusCode, locOp: Option[Location]): NextInstr = {
    locOp.fold[NextInstr](Error(NoLocation(uri, sc)))(l => Continue(uri, sc, l.value))
  }

  def apply[A](uri: Uri, fetch: Uri => Future[HttpResult[A]])(implicit ec: ExecutionContext): Future[Unit] = {
     fetch(uri).flatMap { result =>
        nextInstr(uri, result.statusCode, result.location) match {
         case Done(Uri(prevLocation), StatusCode(sc))           => done(s"$prevLocation [${sc}]")
         case Continue(prevLocation, statusCode, nextLocation)  => cont[A](prevLocation, statusCode, nextLocation, fetch)
         case Error(NoLocation(Uri(uri), StatusCode(sc)))       => error(s"No location header found at: $uri, status code: $sc")
         case Error(NoStatusCode(Uri(uri)))                     => error(s"No status code returned for: $uri")
         case Error(UnhandledStatusCode(uri, StatusCode(sc)))   => error(s"$uri returned unhandled status code: $sc")
       }
     } recover {
       case NonFatal(e) =>
        // error(s"unexpected error: ${e.getMessage}").map(_ => e.printStackTrace())
        import java.io.{ByteArrayOutputStream, PrintStream}
        val bout = new ByteArrayOutputStream()
        e.printStackTrace(new PrintStream(bout, true))
        val st = new String(bout.toByteArray, "UTF-8")
        LOGGER.error(s"unexpected error: ${e.getMessage}\n${st}${heading("Error")}")
     }
  }

  private def cont[A](prevLocation: Uri,
                      statusCode: StatusCode,
                      nextLocation: Uri,
                      fetch: Uri => Future[HttpResult[A]])(
                      implicit ec: ExecutionContext): Future[Unit] = {
    output(s"${prevLocation.value} [${statusCode.value}]", LOGGER.info _).
      flatMap {_ => Redirect(Uri.join(prevLocation, nextLocation), fetch) }
  }

  private def heading(value: String): String = s"\n--${value}--"

  private def output(message: String, log: String => Unit)(implicit ec: ExecutionContext): Future[Unit] = Future(log(message))

  private def done(message: String)(implicit ec: ExecutionContext): Future[Unit] = output(message + heading("Done"), LOGGER.info _)

  private def error(message: String)(implicit ec: ExecutionContext): Future[Unit] = output(message + heading("Error"), LOGGER.error _)
}