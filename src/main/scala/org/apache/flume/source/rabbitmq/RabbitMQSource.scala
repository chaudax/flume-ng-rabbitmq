package org.apache.flume.source.rabbitmq

import org.apache.flume.conf.Configurable

import org.apache.flume.source.AbstractSource
import org.apache.flume._
import org.slf4j.{LoggerFactory, Logger}
import com.rabbitmq.client.ConnectionFactory

import org.apache.flume.event.SimpleEvent

import akka.actor._

import com.github.sstone.amqp.{Amqp, Consumer, ConnectionOwner}
import com.github.sstone.amqp.Amqp._
import scala.concurrent.duration._

import com.rabbitmq.client.AMQP.BasicProperties

import collection.JavaConversions._


/**
 * Created by etsvigun on 2/13/14.
 */
class RabbitMQSource extends AbstractSource with Configurable with EventDrivenSource {
  private val logger: Logger = LoggerFactory.getLogger(classOf[RabbitMQSource])

  private val counterGroup: CounterGroup = new CounterGroup
  private var connectionFactory: Option[ConnectionFactory] = None
  private var queueName: Option[String] = None
  private var topics: Option[Seq[String]] = None
//  private var autoAck: Boolean = false

  private var connection: Option[ActorRef] = None
  private var consumer: Option[ActorRef] = None
  private var queueParamaters: Option[QueueParameters] = None

  override def configure(context: Context) = {
    connectionFactory = Some(getFactory(context))
    queueName = getQueueName(context)
    topics = Some(RabbitMQUtil.getTopics(context))
    //    autoAck = getAutoAck(context)

    queueParamaters = queueName match {
      case Some(qn) => Some(QueueParameters(qn, passive = true))
      case None =>
        throw new IllegalArgumentException("You must configure queue name parameter for a source")
    }

    val connFactory = getFactory(context)

    val conn = system.actorOf(
      ConnectionOwner.props(connFactory, 1 second).
      withDispatcher("pinned-dispatcher"))
    connection = Some(conn)

    val listener = system.actorOf(Props(new Actor {
      private val logger: Logger = LoggerFactory.getLogger(classOf[RabbitMQSource])
      lazy val channelProcessor = getChannelProcessor
      var unacked = 0L
      def receive = {
        case Delivery(consumerTag, envelope, properties, body) =>
          val tag = envelope.getDeliveryTag
          if(unacked >=100){
            sender ! Reject(tag)
          }
          else {
            unacked += 1
            try {
              channelProcessor.processEvent(Event(body, properties))
              sender ! Ack(tag)
            } catch {
              case t: Throwable =>
                sender ! Reject(tag)
                logger.warn("Cannot process event:", t)
            }
            unacked -= 1
          }
      }
    }).withDispatcher("pinned-dispatcher"))

    consumer = Some(ConnectionOwner.createChildActor(conn, Consumer.props(
      listener,
      channelParams = None,
      autoack = false),
      name = Some("flume")))
  }

  override def start() {
    consumer map { (c:ActorRef) =>
      logger.info("Source starting")
      Amqp.waitForConnection(system, c).await()
      counterGroup.incrementAndGet("open.attempts")
      c.tell(Record(AddQueue(queueParamaters.get)), ActorRef.noSender)
    }
  }

  override def stop() {
    logger.info("Source stopping")
    connection map {
      _ ! PoisonPill
    }
    super.stop()
  }
}

object Event {
  def apply(body: Array[Byte], properties: BasicProperties): SimpleEvent = {
    val event = new SimpleEvent()
    event.setBody(body)
    event.setHeaders(getHeaders(properties))
    event
  }
}
