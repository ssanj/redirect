package com.example.redirect.funcsonly

import DispatchClient.Dispatch
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import org.slf4j.LoggerFactory

object Runner {
  val LOGGER = LoggerFactory.getLogger(Runner.getClass);

  def main(args: Array[String]): Unit = {
    val uri = Uri("http://uniqueimprints.com")
    // val uri = Uri("http://realcommercial.com.au")
    val connection = DispatchClient.connect()
    val resultF = Redirect[Dispatch](uri, DispatchClient.fetch(connection))
    Await.ready(resultF, 20.seconds)
    DispatchClient.shutdown(connection)
    LOGGER.info(s"Runner exited")
  }
}