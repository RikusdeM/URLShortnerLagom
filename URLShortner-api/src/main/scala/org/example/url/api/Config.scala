package org.example.url.api

import com.typesafe.config
import com.typesafe.config.ConfigFactory
import Config.configInitializationError

case class MyAppConfig(shortenedUrlLenght: Int)
object Config {
  val configFactory: config.Config = ConfigFactory.load()
  val configInitializationError: ConfigInitialization = ConfigInitialization(
    "Could not initialize config"
  )

  case class ConfigInitialization(message: String) extends Exception(message) {
    def this(message: String, cause: Throwable) = {
      this(message)
      initCause(cause)
    }
  }
}

trait Config {
  import Config.configFactory
  val config: MyAppConfig = {
    for {
      shortenedUrl <- Some(configFactory.getInt("myApp.shortenedUrlLength"))
    } yield {
      MyAppConfig(shortenedUrl)
    }
  } match {
    case Some(conf) => conf
    case None       => throw configInitializationError
  }
}
