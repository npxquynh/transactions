package ru.yudnikov.meta.describing.classes

import ru.yudnikov.meta.describing.descriptions.{ClassDescription, Description, PropertyDescription}
import ru.yudnikov.meta.describing.Reflection

import scala.reflect.runtime.universe._

/**
  * Created by igor.yudnikov on 01-Aug-17.
  */
case class ClassPropertyDescription(
                                     parent: Description,
                                     name: String,
                                     aType: Type
                                   ) extends PropertyDescription with ClassDescription {
  
  override val aClass: Class[_] = Reflection.classByType(aType)
}