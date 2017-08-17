package ru.yudnikov.core.storing.storages.cassandra

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import ru.yudnikov.core.storing.{Storable, Storage}


import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by Don on 07.08.2017.
  */
object Cassandra extends CassandraStorage {
  
  //private val logger = Logger(LoggerFactory.getLogger(getClass.getSimpleName))
  
  override def save(storable: Storable[_]): Future[Try[Unit]] = {
    val cs = columnsInstanced(storable._description).filter((c: column.ColumnInstanced[_]) => c.instance != None)
    val csAll = cs.flatMap(c => List(c).union(c.children))
    val tableName: String = s"$keyspace.${storable.getClass.getSimpleName}"
    val columnNames: String = csAll.map(c => c.name).mkString(", ")
    val values: String = csAll.map(c => c.value).mkString(", ")
    executeFutureUnit(s"INSERT INTO $tableName ($columnNames) values ($values)")
  }
  
  override def list: Future[List[Storable[_]]] = ???
  
  override def load[K, T <: Storable[K]](aClass: Class[T], id: K): Future[Try[T]] = {
    Future {
      Failure {
        new Exception(s"can't load ${aClass.getSimpleName} by id $id")
      }
    }
  }
}
