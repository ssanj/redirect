package com.example.redirect.funcsmonads

trait HttpResult[A] {
  val statusCode: Option[StatusCode]
  val headers: Map[String, Seq[String]]

  def location: Option[Location] =
    headers.get("Location").flatMap(_.headOption.map(uri => Location(Uri(uri))))
}

final case class Uri(value: String) {
  val endsWithSlash: Boolean = value.endsWith("/")
  val startsWithSlash: Boolean =  value.startsWith("/")
  val withoutEndSlash: Uri = if (endsWithSlash) Uri(value.take(value.length - 1)) else this
  val isAbsolute: Boolean = value.startsWith("http")
  val isRelative: Boolean = !isAbsolute
}

object Uri {
  def join(prevLocation: Uri, nextLocation: Uri): Uri = {
    if (nextLocation.isRelative) {
      (prevLocation.endsWithSlash, nextLocation.startsWithSlash) match {
        case (true, true)           => Uri(s"${prevLocation.withoutEndSlash.value}${nextLocation.value}")
        case (_, true) | (true, _)  => Uri(s"${prevLocation.value}${nextLocation.value}")
        case (false, false)         => Uri(s"${prevLocation.value}/${nextLocation.value}")
      }
    } else nextLocation
  }
}

final case class StatusCode(value: Int)
final case class Location(value: Uri)

sealed trait ErrorType
final case class NoLocation(uri: Uri, statusCode: StatusCode) extends ErrorType
final case class NoStatusCode(uri: Uri) extends ErrorType
final case class UnhandledStatusCode(uri:Uri, statusCode: StatusCode) extends ErrorType
final case class UnhandledError(error: Throwable) extends ErrorType


final case class Continue(prevLocation: Uri, statusCode: StatusCode, nextLocation: Uri)

final case class Navigation(uri: Uri, statusCode: StatusCode)

final case class Done(navs: Vector[Navigation])