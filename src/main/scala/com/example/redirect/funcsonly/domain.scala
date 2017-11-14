package com.example.redirect.funcsonly

//StatusCode => Option[Location] => NextInstr
//
//301| 302 => LookUp(location)
//200s => Done()
//Others if location.isEmpty => Error(NoLocation)
//Others => Error(InvalidStatus(others))
//
// Uri => Future[Either[Error, HttpResult[Dispatch]]


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
// final case class SomeException(uri: Uri, message: Option[String], ex: Throwable) extends ErrorType

sealed trait NextInstr
final case class Error(value: ErrorType) extends NextInstr
final case class Continue(prevLocation: Uri, statusCode: StatusCode, nextLocation: Uri) extends NextInstr
final case class Done(prevLocation: Uri,  statusCode: StatusCode) extends NextInstr