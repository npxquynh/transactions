package ru.yudnikov.core.modeling

import java.util.UUID

import ru.yudnikov.core.Commons

import scala.concurrent.stm.{TMap, _}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
  * Created by Don on 16.08.2017.
  */
class Keeper[T <: Model](implicit classTag: ClassTag[T]) {
  
  private val logger = Commons.logger(getClass)
  
  private val versions: TMap[UUID, Map[Long, T]] = TMap()
  
  def keep(model: Model, count: Long): Try[Unit] = atomic { implicit txn =>
    logger.trace(s"keeping model $model with count $count")
    Try {
      val currentVersions = versions.snapshot
      val version = count -> model.asInstanceOf[T]
      val modelVersions = currentVersions.get(model.id) match {
        case Some(map) =>
          map + version
        case _ =>
          Map(version)
      }
      versions.put(model.id, modelVersions)
    }
  }
  
  def get(id: UUID): Try[T] = get(id, map => map.keys.max)
  
  def get(id: UUID, count: Long): Try[T] = get(id, _ => count)
  
  def get(id: UUID, keyFunction: Map[Long, T] => Long): Try[T] = atomic { implicit txn =>
    val currentVersions = versions.snapshot
    currentVersions.get(id) match {
      case Some(map) if map.nonEmpty =>
        val key = keyFunction(map)
        map.get(key) match {
          case Some(t) =>
            logger.trace(s"got model $t")
            Success(t)
          case None =>
            Failure(new Exception(s"model not found by key $key among $map"))
        }
      case Some(map) if map.isEmpty =>
        Failure(new Exception(s"model not found in empty map $map"))
      case None =>
        Failure(new Exception(s"model not found by id $id among $currentVersions"))
    }
  }
  
  def trace(): Unit = atomic { implicit txn =>
    val currentVersions = versions.snapshot
    val stringBuilder = new StringBuilder()
    currentVersions.foreach(line => stringBuilder.append("\t" + line + "\n"))
    logger.info(s"versions of size (${currentVersions.size}) contains: \n" + stringBuilder)
  }
  
}
