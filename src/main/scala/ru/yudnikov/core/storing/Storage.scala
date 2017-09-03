package ru.yudnikov.core.storing

import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.util.Try

/**
  * Created by Don on 07.08.2017.
  */
trait Storage {
  
  def save(storable: Storable[_]): Future[Try[Unit]]

  def save(storables: List[Storable[_]]): Future[Try[Unit]]
  
  def list[K, T <: Storable[K]](aClass: Class[T]): Future[List[T]]
  
  def load[K, T <: Storable[K]](aClass: Class[T], id: K): Future[Try[T]]
  
}
