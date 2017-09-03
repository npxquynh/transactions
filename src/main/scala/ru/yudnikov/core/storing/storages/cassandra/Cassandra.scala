package ru.yudnikov.core.storing.storages.cassandra

import com.datastax.driver.core.Row
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import ru.yudnikov.core.storing.storages.cassandra.column.ColumnInstanced
import ru.yudnikov.core.storing.{Storable, Storage}
import ru.yudnikov.meta.describing.classes.{ClassDescriptionImpl, StorableClassDescription}

import scala.collection.JavaConversions
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag

/**
  * Created by Don on 07.08.2017.
  */
object Cassandra extends CassandraStorage {
  
  private val logger = Logger(LoggerFactory.getLogger(getClass.getSimpleName))

  def insertStatement(storable: Storable[_]): Future[String] = Future {
    logger.trace(s"getting insert statement $storable")
    val cs = columnsInstanced(storable._description)
    val csInstanced = cs.map(c => c.asInstanceOf[ColumnInstanced[Any]])
    val csNonEmpty = csInstanced.filter(_.instance != None)
    val csAll = csNonEmpty.flatMap(c => List(c).union(c.children))
    val tableName: String = s"$keyspace.${storable.getClass.getSimpleName}"
    val columnNames: String = csAll.map(c => c.name).mkString(", ")
    val values: String = csAll.map(c => c.value).mkString(", ")
    s"INSERT INTO $tableName ($columnNames) values ($values)"
  }

  override def save(storable: Storable[_]): Future[Try[Unit]] = {
    insertStatement(storable) flatMap { insert =>
      executeFutureUnit(insert + ";")
    }
  }

  override def save(storables: List[Storable[_]]): Future[Try[Unit]] = {
    val insertsFuture = Future.sequence {
      storables.map {
        insertStatement
      }
    }
    insertsFuture flatMap { inserts =>
      executeFutureUnit(inserts.mkString("BEGIN BATCH\n", ";\n", "\nAPPLY BATCH;"))
    }
  }

  def rowStream(aClass: Class[_ <: Storable[_]]): Future[Stream[Row]] = {
    executeFuture(s"SELECT * FROM ${aClass.getSimpleName};") map {
      case Success(resultSet) =>
        JavaConversions.asScalaIterator(resultSet.iterator()).toStream
      case Failure(exception) =>
        logger.error(s"can't get row stream of $aClass")
        throw exception
    }
  }

  def fetchRow[K, T <: Storable[K]](row: Row)(implicit k: ClassTag[K], t: ClassTag[T]): Option[T] = {
    val kClass: Class[K] = k.runtimeClass.asInstanceOf[Class[K]]
    val tClass: Class[T] = t.runtimeClass.asInstanceOf[Class[T]]
    val d = StorableClassDescription(kClass, tClass)
    val cs = columns(d)
    null
  }

  override def list[K, T <: Storable[K]](aClass: Class[T]): Future[List[T]] = {
    Future(Nil)
  }

  override def load[K, T <: Storable[K]](aClass: Class[T], id: K): Future[Try[T]] = {
    Future {
      Failure {
        new Exception(s"can't load ${aClass.getSimpleName} by id $id")
      }
    }
  }
}
