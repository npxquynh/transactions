package ru.yudnikov.core.storing.storages.cassandra


import java.io.File
import java.util.UUID

import com.datastax.driver.core._
import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import ru.yudnikov.Dependencies
import ru.yudnikov.core.modeling.{Model, Reference}
import ru.yudnikov.core.storing.storages.cassandra.Cassandra.getClass
import ru.yudnikov.core.storing.storages.cassandra.codecs.{BigDecimalCodec, JodaCodec, ReferenceCodec, ReferenceListCodec}
import ru.yudnikov.core.storing.storages.cassandra.column.{Column, ColumnInstanced}
import ru.yudnikov.core.storing.{Storable, Storage}
import ru.yudnikov.meta.describing.Reflection
import ru.yudnikov.meta.describing.classes.{ClassDescriptionImpl, StorableClassDescription}
import ru.yudnikov.meta.describing.descriptions.{InstanceDescription, PropertyDescription}
import ru.yudnikov.meta.describing.instances.{InstancePropertyDescription, StorableInstanceDescription}

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

/**
  * Created by Don on 13.08.2017.
  */
trait CassandraStorage extends Storage {
  
  private val logger = Logger(LoggerFactory.getLogger(getClass.getSimpleName))
  
  implicit class ScalableFuture[T](listenableFuture: ListenableFuture[T]) {
    def asScala: Future[T] = {
      val promise = Promise[T]()
      val callback = new FutureCallback[T] {
        
        def onFailure(t: Throwable): Unit = promise.failure(t)
        
        def onSuccess(result: T): Unit = promise.success(result)
        
      }
      Futures.addCallback(listenableFuture, callback)
      promise.future
    }
  }
  
  val dataTypesNative: Map[Class[_], DataType] = Map(
    classOf[Reference[Model]] -> DataType.varchar(),
    classOf[org.joda.time.DateTime] -> DataType.timestamp(),
    classOf[java.util.Date] -> DataType.timestamp(),
    classOf[java.util.UUID] -> DataType.uuid(),
    classOf[java.lang.Boolean] -> DataType.cboolean(),
    classOf[java.lang.Character] -> DataType.varchar(),
    classOf[java.lang.String] -> DataType.varchar(),
    classOf[java.lang.Short] -> DataType.smallint(),
    classOf[java.lang.Integer] -> DataType.cint(),
    classOf[java.lang.Long] -> DataType.bigint(),
    classOf[java.lang.Double] -> DataType.cdouble(),
    classOf[java.lang.Float] -> DataType.cfloat(),
    classOf[scala.math.BigDecimal] -> DataType.decimal(),
    classOf[scala.collection.immutable.List[Reference[Model]]] -> DataType.list(DataType.varchar()),
    classOf[scala.collection.immutable.List[_]] -> DataType.list(DataType.varchar())
  )
  
  lazy private val conf = Dependencies.conf
  
  lazy private val host: String = conf.getString("cassandra.host")
  lazy private val port: Int = conf.getInt("cassandra.port")
  
  lazy val cluster: Cluster = Cluster.builder().addContactPoint(host).withPort(port).build()
  cluster.getConfiguration.getCodecRegistry.register(new ReferenceCodec)
  cluster.getConfiguration.getCodecRegistry.register(new ReferenceListCodec)
  cluster.getConfiguration.getCodecRegistry.register(new BigDecimalCodec)
  cluster.getConfiguration.getCodecRegistry.register(new JodaCodec)
  
  lazy protected val keyspace: String = conf.getString("cassandra.keyspace")
  lazy protected val session: Session = cluster.connect()
  
  protected def executeFutureUnit(query: String): Future[Try[Unit]] = {
    if (Dependencies.executeQueries) executeFuture(query) map {
      case Success(_) =>
        Success()
      case Failure(exception) =>
        Failure(exception)
    } else
      Future(Success(logger.info(s"query wouldn't be executed: \n$query")))
  }
  
  protected def executeFuture(query: String): Future[Try[ResultSet]] = {
    try {
      logger.debug(s"executing query: \n$query")
      session.executeAsync(session.prepare(query).bind()).asScala map (resultSet => Success(resultSet))
    } catch {
      case e: Exception =>
        logger.warn(s"can't execute query: \n$query", e)
        Future(Failure(e))
    }
  }
  
  def createKeyspace: Future[Unit] = {
    executeFuture(s"CREATE KEYSPACE IF NOT EXISTS $keyspace WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };") map {
      case Success(_) =>
        Success()
      case Failure(exception) =>
        Failure(exception)
    }
  }
  
  def dropKeyspace: Future[Unit] = {
    executeFuture(s"DROP KEYSPACE IF EXISTS $keyspace;") map {
      case Success(_) =>
        Success()
      case Failure(exception) =>
        Failure(exception)
    }
  }
  
  def createTable(description: StorableClassDescription[_]): Future[Unit] = {
    val cs = columns(description)
    val tokens = cs.flatMap(c => List(c).union(c.children)).map(c => c.creationToken)
    val key = s"PRIMARY KEY(${cs.filter(_.isPrimaryKey).flatMap(c => List(c).union(c.children)).map(_.name).mkString(", ")})"
    executeFuture(s"CREATE TABLE IF NOT EXISTS $keyspace.${description.aClass.getSimpleName} (${(tokens :+ key).mkString(", ")});") map {
      case Success(_) =>
        Success()
      case Failure(exception) =>
        Failure(exception)
    }
  }
  
  protected def columns[T](description: StorableClassDescription[_]): List[column.Column[T]] = {
    description.children.map {
      d: PropertyDescription => column.Column[T](d)
    }
  }
  
  protected def columnsInstanced[T](description: InstanceDescription): List[column.ColumnInstanced[T]] = {
    description.children.map {
      case d: InstancePropertyDescription => column.ColumnInstanced[T](d)
    }
  }
}
