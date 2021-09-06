package org.example.url.impl

import akka.Done
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.EntityRef
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.EventStreamElement
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.util.Timeout
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import org.example.url.api.APIExceptions.shortenThrowable
import org.example.url.api.{URL, URLPair, URLService, URLSimple}

/**
  * Implementation of the HelloService.
  */
class URLServiceImpl(
    clusterSharding: ClusterSharding,
    persistentEntityRegistry: PersistentEntityRegistry
)(implicit ec: ExecutionContext)
    extends URLService {

  /**
    * Looks up the entity for the given ID.
    */
  private def entityRef(id: String): EntityRef[URLCommand] =
    clusterSharding.entityRefFor(URLState.typeKey, id)

  implicit val timeout = Timeout(5.seconds)

  override def lookup(shortenedURL: String): ServiceCall[URLSimple, URLPair] =
    ServiceCall { _ =>
      // Look up the sharded entity (aka the aggregate instance) for the given shortenedURL.
      val ref = entityRef(shortenedURL)

      // Ask the aggregate instance the lookup command.
      ref
        .ask[URLPair](replyTo => Lookup(URLSimple(shortenedURL), replyTo))
    }

  override def shorten(shortenedURL: String): ServiceCall[URL, Done] =
    ServiceCall { request =>
      // Look up the sharded entity (aka the aggregate instance) for the given shortenedURL.
      val ref = entityRef(shortenedURL)

      // Tell the aggregate to use the greeting message specified.
      ref
        .ask[Confirmation](replyTo => Shorten(request, replyTo))
        .map {
          case Accepted => Done
          case _        => throw BadRequest(shortenThrowable(request.urlString(false)))
        }
    }

  override def urlPairTopic(): Topic[URLPair] =
    TopicProducer.singleStreamWithOffset { fromOffset =>
      persistentEntityRegistry
        .eventStream(URLEvent.Tag, fromOffset)
        .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(
      helloEvent: EventStreamElement[URLEvent]
  ): URLPair = {
    helloEvent.event match {
      case Shortened(urlPair) =>
//        api.GreetingMessageChanged(helloEvent.entityId, msg)
        urlPair
    }
  }
}
