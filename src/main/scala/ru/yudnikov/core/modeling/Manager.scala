package ru.yudnikov.core.modeling

import java.util.UUID

import com.typesafe.scalalogging.Logger
import ru.yudnikov.core._
import ru.yudnikov.core.storing.{Persister, Storable, StorableManager}
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

  protected val countPersister = new Persister[UUID, Long](logger)

  def includeBackward(reference: Reference[Model], backward: Reference[Model]): Try[Unit]= {
    backwardReferee.include(reference.id, backward)
  }

  def excludeBackward(reference: Reference[Model], backward: Reference[Model]): Try[Unit]= {
    backwardReferee.exclude(reference.id, backward)
  }

  def update(model: Model): Reference[_ <: Model] = {
    logger.trace(s"updating $model")
    atomic { implicit txn =>
      val count = counter.count()
      val reference = Reference(aClass, model.id, count)
      keeper.keep(model, count)
      val refs = model._description.filter[InstanceDescription](_.isReference).map(_.instance.asInstanceOf[Reference[Model]])
      forwardReferee.include(model.id, refs) match {
        case set: Set[Try[Unit]] if set.exists(_.isFailure) =>
          logger.info(s"refs are yet included")
          //throw new Exception(s"cant include forward refs of $model $refs")
        case set: Set[Try[Unit]] =>
          logger.trace(s"forward refs are included without fails $set")
        case _ =>
          val msg = s"unknown state"
          logger.error(msg)
          throw new Exception(msg)
      }
      refs.foreach(_.include(reference.asInstanceOf[Reference[Model]]))
      reference
    }
  }
  
  def get(reference: Reference[_ <: Model]): Try[T] = keeper.get(reference.id, reference.count) match {
    case Success(t) =>
      Success(t)
    case Failure(exception) =>
      logger.info(s"can't get model by id & count ${reference.count}, getting by id only", exception.getMessage)
      keeper.get(reference.id)
  }

  override def saveFuture(storable: Storable[UUID]): Future[Try[Unit]] = atomic { implicit txn =>

    val model = storable.asInstanceOf[Model]

    if (countPersister.isPersistent(model.id, model.reference.count)) {
      val msg = s"model is already saved $model"
      logger.debug(msg)
      return Future(Failure(new Exception(msg)))
    }

    countPersister.persist(model.id, model.reference.count)

    // saving dependent refs
    val refs = model._description.filter[InstanceDescription](_.isReference).map(_.instance.asInstanceOf[Reference[Model]]).toList
    val saveFuture = if (refs.isEmpty) {
      logger.trace(s"model has no references $model")
      super.saveFuture(storable)
    } else {
      logger.trace(s"model has references (${refs.size}): $refs")
      val refsGrouped = refs.groupBy(_.manager)
      try {
        val nonPersisted = refsGrouped.flatMap {
          t => {
            val manager = t._1
            val refs = t._2.filter(ref => !t._1.countPersister.isPersistent(ref.id, ref.count))
            refs.map { ref =>
              manager.countPersister.persist(ref.id, ref.count)
            }
            refs
          }
        }.toList
        logger.debug(s"non persisted refs are (${nonPersisted.size}) $nonPersisted")
        val storables: List[Model] = nonPersisted.map(_.model) :+ model
        super.saveFuture(storables)
      } catch {
        case e: Exception =>
          throw e
      }
    }

    saveFuture

  }

  def trace(): Unit = {
    counter.trace()
    keeper.trace()
    forwardReferee.trace()
    backwardReferee.trace()
  }
  
}