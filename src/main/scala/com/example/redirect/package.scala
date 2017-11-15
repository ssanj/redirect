package com.example

package object redirect {

  def stacktrace(t: Throwable): String = {
    import java.io.{ByteArrayOutputStream, PrintStream}
    val bout = new ByteArrayOutputStream()
    t.printStackTrace(new PrintStream(bout, true))
    new String(bout.toByteArray, "UTF-8")
  }

}

