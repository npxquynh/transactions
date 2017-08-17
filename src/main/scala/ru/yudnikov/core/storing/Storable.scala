package ru.yudnikov.core.storing

import ru.yudnikov.meta.describing.descriptions.InstanceDescription
import ru.yudnikov.meta.describing.instances.StorableInstanceDescription

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

/**
  * Created by Don on 07.08.2017.
  */
trait Storable[K] extends Serializable {
  
  val id: K
  
  val version: Long
  
  val manager: StorableManager[K, Storable[K]]
  
  def save(): Unit = Await.result(manager.save(this), Duration.Inf)
  
  def saveFuture: Future[Try[Unit]] = manager.save(this)
  
  def remove(): Unit = Await.result(manager.remove(this), Duration.Inf)
  
  def removeFuture: Future[Try[Unit]] = manager.remove(this)
  
  lazy val _description: InstanceDescription = StorableInstanceDescription(this)
  
  override def toString: String = _description.toString
  
  override def hashCode: Int = _description.hashCode()
  
}
