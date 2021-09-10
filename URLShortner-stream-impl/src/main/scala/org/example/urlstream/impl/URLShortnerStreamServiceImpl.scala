package org.example.urlstream.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.ServiceCall
import org.example.url.api.{URLPair, URLService, URLSimple}
import org.example.urlstream.api.URLShortnerStreamService

import scala.concurrent.Future

/**
  * Implementation of the [[URLShortnerStreamService]].
  */
class URLShortnerStreamServiceImpl(urlService: URLService)
    extends URLShortnerStreamService {
  def stream: ServiceCall[Source[String, NotUsed], Source[URLPair, NotUsed]] =
    ServiceCall { url =>
      Future.successful(
        url
          .mapAsync(8){
            url =>
              urlService.lookup(url).invoke()
          }
      )
    }
}
