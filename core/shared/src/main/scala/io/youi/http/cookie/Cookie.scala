package io.youi.http.cookie

import java.util.Date

import io.youi.http.DateHeaderKey

sealed trait Cookie {
  def name: String
  def value: String

  def toHTTP(): String
}

case class RequestCookie(name: String, value: String) extends Cookie {
  override def toHTTP(): String = s"$name=$value"
}

case class ResponseCookie(name: String,
                          value: String,
                          expires: Option[Long] = None,
                          maxAge: Option[Long] = None,
                          domain: Option[String] = None,
                          path: Option[String] = None,
                          secure: Boolean = false,
                          httpOnly: Boolean = false) extends Cookie {
  override def toHTTP(): String = {
    val b = new StringBuilder
    b.append(s"$name=$value")
    expires.foreach(l => b.append(s"; Expires=${DateHeaderKey.parser.format(new Date(l))}"))
    maxAge.foreach(l => b.append(s"; Max-Age=$l"))
    domain.foreach(s => b.append(s"; Domain=$s"))
    path.foreach(s => b.append(s"; Path=$s"))
    if (secure) b.append("; Secure")
    if (httpOnly) b.append("; HttpOnly")
    b.toString()
  }
}