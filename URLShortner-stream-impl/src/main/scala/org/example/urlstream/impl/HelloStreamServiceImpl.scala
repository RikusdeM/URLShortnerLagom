package org.example.urlstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import org.example.url.api.URLService
import org.example.urlstream.api.HelloStreamService

import scala.concurrent.Future

/**
  * Implementation of the HelloStreamService.
  */
class HelloStreamServiceImpl(helloService: URLService)
    extends HelloStreamService {
  def stream =
    ServiceCall { hellos =>
      Future.successful(
        hellos
          .mapAsync(8)(helloService.hello(_).invoke())
      )
    }
}
