package ru.yudnikov.core

import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import ru.yudnikov.core.modeling.Model
import ru.yudnikov.core.storing.Storable

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.reflect.ClassTag
import scala.util.Random

/**
  * Created by Don on 16.08.2017.
  */
object Commons {
  
  private val logger = Logger(LoggerFactory.getLogger(getClass))
  
  val random: Random = new Random()
  
  def thread(body: => Unit): Thread = {
    val t = new Thread {
      override def run(): Unit = body
    }
    t.start()
    t
  }
  
  def repeat(body: => Unit, n: Int = 100000): Unit = {
    val start = new DateTime()
    println(s"started $n iterations @ ${start.toLocalTime}")
    for (i <- 1 to n) {
      body
    }
    println(s"finished $n iterations in ${new DateTime().getMillis - start.getMillis} ms")
  }
  
  def await(future: Future[Any]): Any = {
    Await.result(future, Duration.Inf)
  }
  
  def loggerName[T](aClass: Class[_], tClass: Class[T]): String = {
    s"${aClass.getSimpleName}[${tClass.getSimpleName}]"
  }
  
  /*
  def logger[T <: Model](aClass: Class[_], tClass: Class[T]): Logger = {
    val name = loggerName(aClass, tClass)
      Logger(LoggerFactory.getLogger(name))
  }
  */
  
  def logger[T](aClass: Class[_])(implicit classTag: ClassTag[T]): Logger = {
    val name = loggerName(aClass, classTag.runtimeClass.asInstanceOf[Class[T]])

    Logger(LoggerFactory.getLogger(name))

  }
  
}
