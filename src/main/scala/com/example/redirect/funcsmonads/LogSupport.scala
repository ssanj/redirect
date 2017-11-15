package com.example.redirect.funcsmonads

import org.slf4j.Logger

trait LogSupport {

  val LOGGER: Logger

  def output(message: String, log: String => Unit): Unit = log(message)

  def done(results: Vector[Navigation]): Unit = {
    results.reverse.map(n => output(s"${n.uri.value} [${n.statusCode.value}]", LOGGER.info _))
    output("\n--Done--", LOGGER.info _)
  }

  def error(message: String): Unit = output(message + "\n--Error--", LOGGER.error _)

}