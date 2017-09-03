package ru.yudnikov.core.modeling

import java.util.UUID

import ru.yudnikov.core.Commons

import scala.concurrent.stm.{atomic, _}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
  * Created by Don on 16.08.2017.
  */
class Referee[T <: Model](implicit classTag: ClassTag[T]) {
  
  private val logger = Commons.logger(getClass)
  
  private val references: TMap[UUID, Set[Reference[Model]]] = TMap()

  def include(id: UUID, references: Set[Reference[Model]]): Set[Try[Unit]] = atomic { implicit txn =>
    references.map(include(id, _))
  }
  
  def include(id: UUID, reference: Reference[Model]): Try[Unit] = atomic { implicit txn =>
    val currentReferences = references.snapshot
    currentReferences.getOrElse(id, Set()) match {
      case set: Set[Reference[Model]] if !set.contains(reference) =>
        Try {
          references.put(id, set + reference)
          logger.trace(s"included reference $reference in $set")
        }
      case _ =>
        Failure(new Exception(s"can't include reference $reference"))
    }
  }
  
  def exclude(id: UUID, reference: Reference[Model]): Try[Unit] = atomic { implicit txn =>
    val currentReferences = references.snapshot
    currentReferences.getOrElse(id, Set()) match {
      case set: Set[Reference[Model]] if set.contains(reference) => Success {
        references.put(id, set - reference)
      }
      case _ =>
        Failure(new Exception(s"can't exclude reference $reference"))
    }
  }
  
  def get(id: UUID): Option[Set[Reference[Model]]] = atomic { implicit txn =>
    references.get(id)
  }

  def trace(): Unit = atomic { implicit txn =>
    val currentReferences = references.snapshot
    val stringBuilder = new StringBuilder()
    currentReferences.foreach(line => stringBuilder.append("\t" + line + "\n"))
    logger.info(s"references of size (${currentReferences.size}) contains: \n" + stringBuilder)
  }

}
