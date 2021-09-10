package org.example.url.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{
  KafkaProperties,
  PartitionKeyStrategy
}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

object URLService {
  val TOPIC_NAME = "url_shortner"
}
trait URLService extends Service {

  /**
    * Example : curl --location --request GET 'localhost:8080/trex?url=http://z9keaG1J'
    * @param shortenedURL : String
    * @return
    */
  def lookup(shortenedURL: String): ServiceCall[NotUsed, URLPair]

  /**
    * Example : curl --location --request POST 'localhost:8080/trex/shorten?url=http://reddit.com:80'
    * @param id : String
    * @return
    */
  def shorten(id: String): ServiceCall[NotUsed, Done]

  /**
    * This gets published to Kafka.
    */
  def urlPairTopic(): Topic[URLPair]

  override final def descriptor: Descriptor = {
    import Service._
    import URL._
    // @formatter:off
    named("urlShortner")
      .withCalls(
        restCall(Method.GET,"/trex?url", lookup _),
        restCall(Method.POST,"/trex/shorten?url", shorten _)
      )
      .withTopics(
        topic(URLService.TOPIC_NAME, urlPairTopic _ )
          // Kafka partitions messages, messages within the same partition will
          // be delivered in order, to ensure that all messages for the same user
          // go to the same partition (and hence are delivered in order with respect
          // to that user), we configure a partition key strategy that extracts the
          // name as the partition key.
          .addProperty(
            KafkaProperties.partitionKeyStrategy,
            PartitionKeyStrategy[URLPair](url => url.shortened.urlString(shortened = true))
          )
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

object APIExceptions {
  val shortenThrowable = (originalURL: String) =>
    new Throwable(s"Could not shorten original URL: $originalURL")
}
