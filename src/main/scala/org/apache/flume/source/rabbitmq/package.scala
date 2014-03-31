package org.apache.flume.source

import org.apache.flume.Context
import collection.JavaConversions._

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.AMQP.BasicProperties
import akka.actor.ActorSystem
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}

/**
 * Created by etsvigun on 2/13/14.
 */
package object rabbitmq {
  val QueueNameConfig = "queuename"
  val ExchangeNameConfig = "exchangename"
  val HostnameConfig: String = "hostname"
  val PortConfig: String = "port"
  val UsernameConfig: String = "username"
  val PasswordConfig: String = "password"
  val VirtualhostConfig: String = "virtualhost"
  val ConnectionTimeoutConfig: String = "connectiontimeout"
  //  val CONFIG_TOPICS: String = "topics"
  //  val CONFIG_AUTOACK: String = "autoack"

  val Prefix: String = "RabbitMQ"


//  val loggers = ConfigValueFactory.fromIterable(Seq("akka.event.slf4j.Slf4jLogger"))
//  val loglevel = ConfigValueFactory.fromAnyRef("DEBUG")
//  val akkaConfig = ConfigFactory.load().withValue("event-handlers", loggers)//.withValue("loglevel", loglevel)
//  val system = ActorSystem("Flume-RabbitMQ", akkaConfig)
  val system = ActorSystem("Flume-RabbitMQ")

  def getHeaders(properties: BasicProperties) =
    properties.getHeaders map {
      (kv: (String, AnyRef)) => (kv._1, kv._2.toString)
    }


  def noneIfNull[T](value: T): Option[T] = value match {
    case null => None
    case v => Some(v)
  }

  def getQueueName(context: Context): Option[String] = {
    noneIfNull(context.getString(QueueNameConfig))
  }

  def getExchangeName(context: Context): Option[String] = {
    noneIfNull(context.getString(ExchangeNameConfig))
  }

  //  def getAutoAck(context: Context): Boolean = {
  //    context.getBoolean(RabbitMQConstants.CONFIG_AUTOACK, false)
  //  }

  //  def getTopics(context: Context): Option[Array[String]] = {
  //    noneIfNull(context.getString(RabbitMQConstants.CONFIG_TOPICS)) map {
  //      _.split(',')
  //    }
  //  }

  def getFactory(context: Context): ConnectionFactory = {
    def getString(name: String): Option[String] = noneIfNull(context.getString(name))
    def getInteger(name: String): Option[Integer] = noneIfNull(context.getInteger(name))

    val factory: ConnectionFactory = new ConnectionFactory

    val hostname: String = context.getString(HostnameConfig, ConnectionFactory.DEFAULT_HOST)
    factory.setHost(hostname)
    val username: String = context.getString(UsernameConfig, ConnectionFactory.DEFAULT_USER)
    factory.setUsername(username)
    val password: String = context.getString(PasswordConfig, ConnectionFactory.DEFAULT_PASS)
    factory.setPassword(password)
    getInteger(PortConfig) map {
      factory.setPort(_)
    }
    getString(VirtualhostConfig) map factory.setVirtualHost
    getInteger(ConnectionTimeoutConfig) map {
      factory.setConnectionTimeout(_)
    }

    factory
  }
}
