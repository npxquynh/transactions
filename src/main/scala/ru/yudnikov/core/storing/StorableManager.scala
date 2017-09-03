package ru.yudnikov.core.storing


import java.util.UUID

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import ru.yudnikov.core.storing.storages.cassandra.Cassandra
import ru.yudnikov.meta.describing.Reflector

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}
import ru.yudnikov.core.Commons
import ru.yudnikov.meta.describing.classes.StorableClassDescription

import scala.concurrent.stm._

/**
  * Created by Don on 07.08.2017.
  */
abstract class StorableManager[K, +T <: Storable[K]](implicit k: ClassTag[K], t: ClassTag[T]) {

  val kClass: Class[K] = k.runtimeClass.asInstanceOf[Class[K]]

  private[this] val aClass: Class[T] = t.runtimeClass.asInstanceOf[Class[T]]

  private val aClassName: String = aClass.getSimpleName

  private val logger = Commons.logger[T](getClass)

  protected val locker = new Locker[K](Logger(LoggerFactory.getLogger(s"Locker[$aClassName]")))

  protected val hashPersister = new Persister[K, Int](Logger(LoggerFactory.getLogger(s"Persister[$aClassName]")))

  def storage: Storage = Cassandra

  def saveFuture(storable: Storable[K]): Future[Try[Unit]] = atomic { implicit txn =>
    if (hashPersister.isPersistent(storable.id, storable.hashCode)) {
      val e = new Exception(s"storable is already persisted")
      throw e
      //Future(Failure(e))
    } else {
      logger.debug(s"locking storable $storable")
      locker.lock(storable.id) match {
        case Success(_) =>
          logger.debug(s"saving storable $storable")
          hashPersister.persist(storable.id, storable.hashCode)
          storage.save(storable) map {
            case Success(_) =>
              logger.debug(s"saved storable, unlocking $storable")
              locker.unlock(storable.id)
            case Failure(exception) =>
              logger.error(s"can't save storable, unlocking $storable")
              locker.unlock(storable.id)
          }
        case Failure(exception) =>
          logger.error(s"can't lock storable $storable", exception)
          throw exception
      }
    }
  }

  protected def saveFuture(storables: List[Storable[K]]): Future[Try[Unit]] = atomic { implicit txn =>

    val storablesGrouped = storables.groupBy(_.manager)

    def unlockStorables = {
      storablesGrouped.flatMap { t =>
        val manager = t._1
        val ss = t._2
        ss.map { s =>
          manager.locker.unlock(s.id)
        }
      }
    }

    def lockStorables = {
      storablesGrouped.flatMap { t =>
        val manager = t._1
        val ss = t._2
        ss.map { s =>
          manager.locker.lock(s.id)
        }
      }
    }

    lockStorables match {
      case _: List[Success[Unit]] =>
        logger.debug(s"storables locked successfully, saving...")
        storage.save(storables)
        unlockStorables match {
          case _: List[Success[Unit]] =>
            Future {
              Success {
                logger.debug(s"storables unlocked successfully, saving complete")
              }
            }
          case list: List[Try[Unit]] if list.exists(_.isFailure) =>
            val msg = s"can't unlock locked storables"
            logger.error(msg)
            throw new Exception(msg)
        }
      case list: List[Try[Unit]] if list.exists(_.isFailure) =>
        unlockStorables
        Future(Failure(new Exception(s"can't lock storables")))
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
