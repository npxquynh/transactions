package ru.yudnikov.core.storing.storages.cassandra.column

import com.datastax.driver.core.{DataType, TypeCodec}
import org.joda.time.DateTime
import ru.yudnikov.meta.describing.descriptions.PropertyDescription
import ru.yudnikov.meta.describing.instances.InstancePropertyDescription

/**
  * Created by Don on 14.08.2017.
  */
case class ColumnInstanced[T](
                               name: String,
                               maybeClass: Option[Class[T]],
                               dataType: DataType,
                               codec: TypeCodec[T],
                               isNative: Boolean,
                               isPrimaryKey: Boolean,
                               isIndex: Boolean,
                               instance: T,
                               children: List[ColumnInstanced[T]] = Nil
                             ) extends AbstractColumn[T] {
  
  val value: String = {
    //logger.trace(s"codec is $codec, value ${description.instance}")
    try {
      codec.format(instance)
    } catch {
      case e: Exception =>
        if (maybeClass.isEmpty) {
          codec.format(instance.toString.asInstanceOf[T])
        } else {
          throw e
        }
    }
  }
  
}

object ColumnInstanced {
  
  def instantiate[T](column: Column[T], inst: T): ColumnInstanced[T] = {
    val result = ColumnInstanced(column.name, column.maybeClass, column.dataType, column.codec, column.isNative,
      column.isPrimaryKey, column.isIndex, inst)
    
    val children: List[ColumnInstanced[T]] =
      if (column.name == "date" && column.maybeClass.contains(classOf[DateTime])) {
        AbstractColumn.dateExpansion.map { t =>
          result.copy(name = "date" + t._1.capitalize, instance = t._2(result.instance.asInstanceOf[DateTime]).asInstanceOf[T])
        }.toList
      } else {
        Nil
      }
    
    result.copy(children = children)
    
  }
  
  def apply[T](column: Column[T], instance: T): ColumnInstanced[T] = instantiate(column, instance)
  
  def apply[T](description: InstancePropertyDescription): ColumnInstanced[T] = {
    instantiate[T](Column(description), description.instance.asInstanceOf[T])
  }
}

