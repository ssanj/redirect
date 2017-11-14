package com.example.redirect.funcsonly

import DispatchClient.Dispatch
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import org.slf4j.LoggerFactory

object Runner {
  val LOGGER = LoggerFactory.getLogger("com.example.redirect.funcsonly.Runner");

  def main(args: Array[String]): Unit = {
    // val uri = Uri("http://uriniqueimprints.com")
    val uri = Uri("http://realcommercial.com.au")
    val resultF = Redirect[Dispatch](uri, DispatchClient.fetch)
    Await.ready(resultF, 20.seconds)
    LOGGER.info(s"Runner exited")
    System.exit(0)
  }
}