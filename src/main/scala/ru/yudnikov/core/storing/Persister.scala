package ru.yudnikov.core.storing

import java.util.UUID

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import ru.yudnikov.core.Commons
import ru.yudnikov.core.modeling.Model

import scala.concurrent.stm._
import scala.reflect.ClassTag
import scala.util.{Failure, Try}

/**
  * Created by Don on 8/18/2017.
  */
class Persister[K, V](logger: Logger) {

  val persistent: TMap[K, V] = TMap()

  def isPersistent(id: K, value: V): Boolean = atomic { implicit txn =>
    logger.trace(s"checking if $id is persistent in $persistent")
    val currentPersistent = persistent.snapshot
    val res = currentPersistent.get(id) match {
      case Some(x: V) if x == value =>
        true
      case _ =>
        false
    }
    logger.trace(s"result is $res")
    res
  }

  def persist(id: K, value: V): Try[Unit] = atomic { implicit txn =>
    val currentVersions = persistent.snapshot
    currentVersions.get(id) match {
      case Some(x) if x == value =>
        Failure {
          new Exception(s"already persisted")
        }
      case _ =>
        Try {
          persistent.put(id, value)
          logger.trace(s"persisted ($id -> $value)")
        }
    }
  }

}
