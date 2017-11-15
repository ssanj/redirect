package com.example.redirect.funcsmonads

import scala.util.control.NonFatal
import scala.language.higherKinds
import cats.Monad
import cats.ApplicativeError
import cats.syntax.either._
import cats.syntax.applicativeError._

object Redirect {

  def apply[M[_], A](uri: Uri, fetch: Uri => M[HttpResult[A]], navs: Vector[Navigation])(implicit M: Monad[M], ME: ApplicativeError[M, Throwable]): M[Either[ErrorType, Done]] = {
     val result: M[Either[ErrorType, Done]] =
      M.flatMap(fetch(uri)) { result =>
        (uri, result.statusCode, result.location) match {
          case (uri, None, _)                                                     => M.pure(NoStatusCode(uri).asLeft[Done])
          case (uri, Some(sc), _)   if (sc.value / 100 == 2)                      => M.pure(Done(Navigation(uri, sc) +: navs).asRight[ErrorType])
          case (uri, Some(sc), Some(loc)) if (sc.value == 301 || sc.value == 302) => Redirect(Uri.join(uri, loc.value), fetch, Navigation(uri, sc) +: navs)
          case (uri, Some(sc), None) if (sc.value == 301 || sc.value == 302)      => M.pure(NoLocation(uri, sc).asLeft[Done])
          case (uri, Some(sc), _)                                                 => M.pure(UnhandledStatusCode(uri, sc).asLeft[Done])
       }
     }

     result.recover {
        case NonFatal(e) => UnhandledError(e).asLeft[Done]
     }
  }
}