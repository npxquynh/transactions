package ru.yudnikov.meta.describing.classes

import ru.yudnikov.meta.describing.descriptions.{CaseClassDescription, Description, PropertyDescription}

import scala.reflect.runtime.universe._

/**
  * Created by Don on 02.08.2017.
  */
case class CaseClassPropertyDescription(parent: Description, name: String, aType: Type, aClass: Class[_]) extends CaseClassDescription with PropertyDescription
