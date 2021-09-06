package org.example.urlstream.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import org.example.url.api.URLPair

/**
  * The hello stream interface.
  *
  * This describes everything that Lagom needs to know about how to serve and
  * consume the [[URLShortnerStreamService]].
  */
trait URLShortnerStreamService extends Service {

  def stream: ServiceCall[Source[String, NotUsed], Source[URLPair, NotUsed]]

  override final def descriptor: Descriptor = {
    import Service._

    named("url-stream")
      .withCalls(
        namedCall("stream", stream)
      ).withAutoAcl(true)
  }
}

