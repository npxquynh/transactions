package ru.yudnikov.core.modeling

import java.util.UUID

import com.typesafe.scalalogging.Logger
import ru.yudnikov.core._
import ru.yudnikov.core.storing.StorableManager
import ru.yudnikov.meta.describing.descriptions.InstanceDescription

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
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
  
  private[this] val forwardReferee = new Referee[T]
  
  private[this] val backwardReferee = new Referee[T]
  
  def update(model: Model): Reference[_ <: Model] = {
    logger.trace(s"updating $model")
    atomic { implicit txn =>
      val count = counter.count()
      val keepFuture = Future {
        keeper.keep(model, count)
      }
      val includeRefsFuture = Future {
        val refs = model._description.filter[InstanceDescription](_.isReference).map(_.instance.asInstanceOf[Reference[Model]])
        if (refs.nonEmpty) {
          logger.trace(s"including $refs")
          forwardReferee.include(model.id, refs).filter(_.isFailure) match {
            case set: Set[Try[Unit]] if set.isEmpty =>
              Success()
            case fails: Set[Try[Unit]] =>
              throw new Exception(s"can't include refs: ${fails.map(_.asInstanceOf[Failure[Unit]].exception)}")
          }
        } else {
          Success()
        }
      }
      val resultFuture = for {
        a <- keepFuture
        b <- includeRefsFuture
      } yield
        Reference(aClass, model.id, count)
      Await.result(resultFuture, Duration.Inf)
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