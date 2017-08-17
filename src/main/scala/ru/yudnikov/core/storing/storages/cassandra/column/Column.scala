package ru.yudnikov.core.storing.storages.cassandra.column

import com.datastax.driver.core.{CodecRegistry, DataType, TypeCodec}
import org.joda.time.DateTime

import ru.yudnikov.core.modeling.Model

import ru.yudnikov.core.storing.storages.cassandra.Cassandra
import ru.yudnikov.meta.describing.Reflection
import ru.yudnikov.meta.describing.descriptions.PropertyDescription

/**
  * Created by Don on 14.08.2017.
  */
case class Column[T](
                      name: String,
                      maybeClass: Option[Class[T]],
                      dataType: DataType,
                      codec: TypeCodec[T],
                      isNative: Boolean,
                      isPrimaryKey: Boolean,
                      isIndex: Boolean,
                      children: List[Column[T]] = Nil
                    ) extends AbstractColumn[T] {
  
  val creationToken: String = s"$name $dataType"
  
}

object Column {
  
  def codec[T](maybeClass: Option[Class[T]], dataType: DataType): TypeCodec[T] = maybeClass match {
    case Some(cls) =>
      Column.codecRegistry.codecFor(dataType, cls)
    case None =>
      Column.codecRegistry.codecFor(dataType)
  }
  
  lazy val codecRegistry: CodecRegistry = Cassandra.cluster.getConfiguration.getCodecRegistry
  
  def apply[T](description: PropertyDescription): Column[T] = {
    
    val isNative: Boolean = Cassandra.dataTypesNative.get(description.aClass).nonEmpty
    
    val maybeClass: Option[Class[T]] = {
      if (isNative) {
        Some(description.aClass.asInstanceOf[Class[T]])
      } else if (description.isOption && description.children.nonEmpty) {
        Some(description.children.head.aClass.asInstanceOf[Class[T]])
      } else if (Reflection.scalaToJava.get(description.aClass).nonEmpty
        && Cassandra.dataTypesNative.get(Reflection.scalaToJava(description.aClass)).nonEmpty) {
        Some(Reflection.scalaToJava(description.aClass).asInstanceOf[Class[T]])
      } else {
        None
      }
    }
    
    val dataType: DataType = maybeClass match {
      case Some(cls) =>
        Cassandra.dataTypesNative.getOrElse(cls, DataType.varchar())
      case _ =>
        DataType.varchar()
    }
  
    val isPrimaryKey: Boolean = description.name == "id"
  
    /*
    val isPrimaryKey: Boolean = {
      val name = description.name
      val parentClass = description.parent.aClass
      classOf[Action].isAssignableFrom(parentClass) && List("id", "date").contains(name) ||
        classOf[ActionGroup].isAssignableFrom(parentClass) && List("id", "date").contains(name) ||
        classOf[Record[Measure]].isAssignableFrom(parentClass) && List("action", "date").contains(name) ||
        classOf[Model].isAssignableFrom(parentClass) && List("id").contains(name)
    }
    */
    
    val isIndex = false
    
    val column = Column(description.name, maybeClass, dataType, codec(maybeClass, dataType), isNative, isPrimaryKey, isIndex)
    
    val children: List[Column[T]] = if (description.name == "date" && description.aClass == classOf[DateTime]) {
      AbstractColumn.dateExpansion.map(t => column.copy(name = "date" + t._1.capitalize)).toList
    } else {
      Nil
    }
    
    column.copy(children = children)
    
  }
}
