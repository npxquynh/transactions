package ru.yudnikov.meta.describing

import java.util.UUID

import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import ru.yudnikov.core.modeling.{Model, Reference}

/**
  * Why am I here?
  *
  * If you want to serialize something, but have some logic or policy which can obscure your code - give it to me.
  */
object Serializer {

  private val logger = Logger(LoggerFactory.getLogger(getClass.getSimpleName))

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
    case _ => x.getClass.getName + "(" + wrap(x.toString) + ")"
    //case _ => x.toString
  }

  def deserialize(aClass: Class[_], args: List[String]): Option[Any] = {
    None
  }

  def deserialize(className: String, args: List[String]): Option[Any] = {
    logger.debug(s"try to deserialize $className with args $args")

    val aClass = Class.forName(className)
    val reference = classOf[Reference[Model]].getName
    className match {
      case reference =>
        logger.debug(s"ref captured")
      case _ =>
        logger.debug(s"something captured $className")
    }
    deserialize(aClass, args)
  }

}