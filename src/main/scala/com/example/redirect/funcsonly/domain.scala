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
}

final case class Uri(value: String)
final case class StatusCode(value: Int)
final case class Location(value: Uri)

sealed trait ErrorType
final case class NoLocation(uri: Uri, statusCode: StatusCode) extends ErrorType
final case class NoStatusCode(uri: Uri) extends ErrorType
final case class UnhandledStatusCode(uri:Uri, statusCode: StatusCode) extends ErrorType
// final case class SomeException(uri: Uri, message: Option[String], ex: Throwable) extends ErrorType

sealed trait NextInstr
final case class Error(value: ErrorType) extends NextInstr
final case class Continue(prevLocation: Uri, nextLocation: Uri) extends NextInstr
final case class Done(prevLocation: Uri,  statusCode: StatusCode) extends NextInstr