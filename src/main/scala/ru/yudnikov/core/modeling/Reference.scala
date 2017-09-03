package ru.yudnikov.core.modeling

import java.util.UUID

import ru.yudnikov.meta.describing.Reflector

import scala.util.Try

/**
  * Created by Don on 16.08.2017.
  */
case class Reference[T <: Model](aClass: Class[T], id: UUID, count: Long)
                                (implicit val manager: Manager[_ <: Model]) {

  def model: T = manager.get(this).get.asInstanceOf[T]

  def include(backward: Reference[Model]): Try[Unit] = manager.includeBackward(this.asInstanceOf[Reference[Model]], backward)

  def exclude(backward: Reference[Model]): Try[Unit] = manager.excludeBackward(this.asInstanceOf[Reference[Model]], backward)

}

object Reference {
  
  def apply(string: String): Reference[Model] = {
    null
  }

  def apply(args: scala.collection.immutable.$colon$colon[Any]): Reference[Model] = {
    if (args.isInstanceOf[List[String]]) {
      val strings = args.asInstanceOf[List[String]]
      val idString = strings.head
      val countString = strings.tail.head
      val className = strings.tail.tail.head
      val id = UUID.fromString(idString)
      val count = countString.toLong
      val aClass = Class.forName(className).asInstanceOf[Class[Model]]
      val manager = Reflector.modelManager(aClass)
      Reference[Model](aClass, id, count)(manager)
    } else
      throw new Exception(s"")
  }

}
