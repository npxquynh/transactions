package ru.yudnikov.core.modeling

import java.util.UUID

import com.typesafe.scalalogging.Logger
import ru.yudnikov.core._
import ru.yudnikov.core.storing.StorableManager

import scala.concurrent.stm._
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
  * Created by Don on 16.08.2017.
  */
abstract class Manager[+T <: Model](implicit classTag: ClassTag[T]) extends StorableManager[UUID, T] {
  
  protected val logger: Logger = Commons.logger(getClass)
  
  implicit val manager: Manager[_ <: Model] = this
  
  private[this] val aClass: Class[T] = classTag.runtimeClass.asInstanceOf[Class[T]]
  
  private[this] val counter = new Counter[T]
  
  private[this] val keeper = new Keeper[T]
  
  def update(model: Model): Reference[_ <: Model] = {
    logger.trace(s"updating $model")
    atomic { implicit txn =>
      val count = counter.count()
      keeper.keep(model, count) match {
        case Success(_) =>
          logger.trace(s"model kept, creating reference with count $count")
          Reference(aClass, model.id, count)
        case Failure(exception) =>
          throw exception
      }
    }
  }
  
  def get(reference: Reference[_ <: Model]): Try[T] = keeper.get(reference.id, reference.count) match {
    case Success(t) =>
      Success(t)
    case Failure(exception) =>
      logger.info(s"can't get model by id & count ${reference.count}, getting by id only", exception.getMessage)
      keeper.get(reference.id)
  }
  
  def trace(): Unit = {
    counter.trace()
    keeper.trace()
  }
  
}