package io.youi.example

import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.outr.scribe._

trait ServerExampleCommunication extends ExampleCommunication {
  private val increment = new AtomicInteger(0)

  override def reverse(value: String): Future[String] = Future(value.reverse)
  override def time: Future[Long] = Future(System.currentTimeMillis())
  override def counter: Future[Int] = Future(increment.getAndIncrement())

  name.attach { n =>
    logger.info(s"Name changed on server: $n")
    url.foreach { u =>
      logger.info(s"URL: $u")
    }
  }
}