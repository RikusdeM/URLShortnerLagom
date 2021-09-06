package org.example.urlstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.softwaremill.macwire._
import org.example.url.api.URLService
import org.example.urlstream.api.URLShortnerStreamService

class URLShortnerStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new URLShortnerStreamApplication(context) {
      override def serviceLocator: NoServiceLocator.type = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new URLShortnerStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[URLShortnerStreamService])
}

abstract class URLShortnerStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[URLShortnerStreamService](wire[URLShortnerStreamServiceImpl])

  // Bind the URLService client
  lazy val urlService: URLService = serviceClient.implement[URLService]
}
