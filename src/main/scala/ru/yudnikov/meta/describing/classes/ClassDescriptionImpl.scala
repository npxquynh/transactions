package ru.yudnikov.meta.describing.classes

import ru.yudnikov.meta.describing.Reflector
import ru.yudnikov.meta.describing.descriptions.ClassDescription

import scala.reflect.runtime.universe._

/**
  * Created by Don on 01.08.2017.
  */
case class ClassDescriptionImpl(aType: Type, aClass: Class[_]) extends ClassDescription

object ClassDescriptionImpl {

  def apply(aClass: Class[_]): ClassDescriptionImpl = new ClassDescriptionImpl(Reflector.typeByClass(aClass), aClass)

}
