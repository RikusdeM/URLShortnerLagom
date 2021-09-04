package org.example.url.api

import play.api.Logging
import play.api.libs.json.{Format, Json}
import scala.language.implicitConversions
import scala.util.Random

case class URLSimple(url: String)
object URLSimple extends Logging {
  import URL._
  def apply(url: URL): URLSimple = {
    URLSimple(url.urlString(true))
  }
  def apply(): URLSimple = {
    URLSimple("")
  }
  implicit val format: Format[URLSimple] = Json.format[URLSimple]
}

case class URL(protocol: String, host: String, port: Option[Int]) {
  import URL._
  def urlString(shortened: Boolean): String =
    this.port match {
      case Some(port) if !shortened =>
        s"${this.protocol}$protocolSeparator${this.host}:${port.toString}"
      case _ => s"${this.protocol}$protocolSeparator${this.host}"
    }
}
object URL extends Logging {
  val protocolSeparator = "://"
  val portSeparator = ':'
  val defaultServiceProtocol = "http"
  val URLException = new Exception("PLEASE PROVIDE VALID URL")

  def apply(urlSimple: URLSimple): Option[URL] = {
    stringToURL(urlSimple.url)
  }
  def apply(): URL = {
    URL("", "", None)
  }

  val hostPortSplit: (String, String) => Option[URL] =
    (hostPort: String, protocol: String) => {
      hostPort.split(portSeparator).toList match {
        case host :: port :: Nil =>
          Some(URL(protocol, host, Some(port.toInt)))
        case host :: Nil if host.nonEmpty => Some(URL(protocol, host, None))
        case _                            => None
      }
    }

  def stringToURL(urlString: String): Option[URL] = {
    urlString.split(protocolSeparator).toList match {
      case protocol :: hostPort :: Nil =>
        hostPortSplit(hostPort, protocol)
      case hostPort :: Nil =>
        hostPortSplit(hostPort, defaultServiceProtocol)
      case _ => None
    }
  }
  implicit val format: Format[URL] = Json.format[URL]
}
case class URLPair(shortened: URL, original: URL)
object URLPair extends Logging with Config {
  import Helpers._
  def apply(originalURL: URL): URLPair = {
    URLPair(
      URL(
        originalURL.protocol,
        randomID(config.shortenedUrlLenght),
        originalURL.port
      ),
      originalURL
    )
  }
  implicit val format: Format[URLPair] = Json.format[URLPair]
}
object Helpers {
  def randomID(length: Int): String =
    Random.alphanumeric.take(length).mkString("")
}
