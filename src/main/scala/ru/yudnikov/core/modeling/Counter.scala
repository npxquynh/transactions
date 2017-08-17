package ru.yudnikov.core.modeling

import ru.yudnikov.core.Commons

import scala.concurrent.stm.{Ref, atomic}
import scala.reflect.ClassTag

/**
  * Created by Don on 16.08.2017.
  */
class Counter[T <: Model](implicit classTag: ClassTag[T]) {
  
  private val logger = Commons.logger(getClass)
  
  private val counter: Ref[Long] = Ref(0)
  
  def count(): Long = atomic { implicit txn =>
    val current = counter()
    val next = current + 1
    counter() = next
    next
  }
  
  def trace(): Unit = atomic { implicit txn =>
    logger.info(s"counter is ${counter()}")
  }

}
