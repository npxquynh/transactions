package ru.yudnikov.core.storing


import ru.yudnikov.core.storing.storages.cassandra.Cassandra
import ru.yudnikov.meta.describing.Reflection

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

import ru.yudnikov.core.Commons
import ru.yudnikov.meta.describing.classes.StorableClassDescription

/**
  * Created by Don on 07.08.2017.
  */
abstract class StorableManager[K, +T <: Storable[K]](implicit t: ClassTag[T], k: ClassTag[K]) {
  
  val kClass: Class[K] = k.runtimeClass.asInstanceOf[Class[K]]
  
  private[this] val aClass: Class[T] = t.runtimeClass.asInstanceOf[Class[T]]
  
  private val logger = Commons.logger[T](getClass)
  
  def storage: Storage = Cassandra
  
  def save(storable: Storable[K]): Future[Try[Unit]] = Future {
    Success {
      logger.trace(s"saving storable $storable")
      Thread.sleep(1000)
      println(s"saved storable $storable")
    }
  }
  
  def list: Future[List[T]] = ???
  
  def load(id: K): Future[Option[T]] = {
    storage.load(aClass, id) map {
      case Success(t: T) =>
        Some(t)
      case Failure(exception) =>
        println(s"cant load storable by id $id", exception.getCause)
        None
    }
  }
  
  def remove(storable: Storable[K]): Future[Try[Unit]] = Future {
    Success {
      println(s"removing storable $storable")
      Thread.sleep(1000)
      println(s"removed storable $storable")
    }
  }
  
  val description: StorableClassDescription[K] = StorableClassDescription(kClass, aClass)
  
}
