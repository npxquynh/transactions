package ru.yudnikov.core.storing

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import ru.yudnikov.meta.describing.descriptions.InstanceDescription
import ru.yudnikov.meta.describing.instances.StorableInstanceDescription

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

/**
  * Created by Don on 07.08.2017.
  */
trait Storable[K] extends Serializable {

  private val logger = Logger(LoggerFactory.getLogger(getClass.getSimpleName))

  val id: K

  val manager: StorableManager[K, Storable[K]]
  
  def save(): Unit = {
    val res = Await.result(manager.saveFuture(this), Duration.Inf)
    res match {
      case Success(_) =>
        //println(s"awaited save")
      case Failure(exception) =>
        //exception.printStackTrace()
        logger.warn(s"can't save storable $this", exception)
        //throw exception
        //println(s"failed...")
    }
  }
  
  def saveFuture: Future[Try[Unit]] = manager.saveFuture(this)
  
  def remove(): Unit = Await.result(manager.remove(this), Duration.Inf)
  
  def removeFuture: Future[Try[Unit]] = manager.remove(this)
  
  lazy val _description: InstanceDescription = StorableInstanceDescription(this)
  
  override def toString: String = _description.toString
  
  override def hashCode: Int = _description.hashCode()
  
}
