package ru.yudnikov.meta.describing.descriptions

import java.util.UUID

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import ru.yudnikov.core.modeling.{Model, Reference}
import ru.yudnikov.meta.describing.Serialization

import scala.collection.GenIterable

/**
  * Created by Don on 31.07.2017.
  */
trait Description {
  
  //protected val logger = Logger(LoggerFactory.getLogger(getClass.getSimpleName))
  
  val aClass: Class[_]
  
  val children: List[Description]
  
  lazy val isReference: Boolean = classOf[Reference[Model]].isAssignableFrom(aClass)
  lazy val isCollection: Boolean = classOf[GenIterable[_]].isAssignableFrom(aClass)
  lazy val isReferenceCollection: Boolean = classOf[GenIterable[Reference[Model]]].isAssignableFrom(aClass)
  
  lazy val isOption: Boolean = classOf[Option[_]].isAssignableFrom(aClass)
  
  def filter(p: Description => Boolean): Set[Description] =
    if (p(this)) Set(this) else Set[Description]() union {
      children.flatMap {
        _.filter(p)
      }.toSet
    }
  
  override def hashCode(): Int = 41 * aClass.hashCode() * children.hashCode()
  
  override def equals(obj: scala.Any): Boolean = obj match {
    case d: Description if d.aClass == aClass && d.children == children => true
    case _ => false
  }
  
  override def toString: String =
    Serialization.classAliases.getOrElse(aClass, aClass).getName + {
      if (children.nonEmpty)
        s"(${
          children.map {
            case d: PropertyDescription =>
              s"${d.name} -> $d"
            case d: Description =>
              d.toString
          }.mkString(",")
        })"
      else this match {
        case d: InstanceDescription => s"(${d.instance})"
        case _ => ""
      }
    }
}
