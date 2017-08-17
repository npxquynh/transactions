package ru.yudnikov.meta.describing

import java.util.UUID

import org.joda.time.DateTime
import ru.yudnikov.core.modeling.{Model, Reference}

/**
  * Why am I here?
  *
  * If you want to serialize something, but have some logic or policy which can obscure your code - give it to me.
  */
object Serialization {

  val classAliases: Map[Class[_], Class[_]] = Map(
    classOf[scala.collection.immutable.$colon$colon[_]] -> classOf[scala.collection.immutable.List[_]]
  )

  def wrap(x: Any, w: String = "'"): String = s"$w$x$w"

  def serialize(x: Any): String = x match {
    
    case ref: Reference[Model] =>
      val args = List(wrap(ref.id), ref.count, wrap(ref.aClass.getName))
      s"${ref.getClass.getName}(${args.mkString(",")})"
    case id: UUID => s"${classOf[UUID].getName}(${wrap(id)})"
    case str: String => wrap(str)
    case date: DateTime => date.getMillis.toString
    case _ => x.toString
  }
}