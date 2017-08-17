package ru.yudnikov.core.storing.storages.cassandra.column

import com.datastax.driver.core.{DataType, TypeCodec}
import org.joda.time.DateTime

/**
  * Created by Don on 14.08.2017.
  */
trait AbstractColumn[T] {
  
  val name: String
  
  val maybeClass: Option[Class[_]]
  
  val dataType: DataType
  
  val isNative: Boolean
  
  val isIndex: Boolean
  
  val isPrimaryKey: Boolean
  
  val codec: TypeCodec[T]
  
  val children: List[AbstractColumn[T]]
  
}

object AbstractColumn {
  
  lazy val dateExpansion: Map[String, DateTime => DateTime] = Map(
    "year" -> (d => d.withDayOfYear(1).withTimeAtStartOfDay()),
    "month" -> (d => d.withDayOfMonth(1).withTimeAtStartOfDay()),
    "day" -> (d => d.withTimeAtStartOfDay())
  )
  
}
