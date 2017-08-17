package ru.yudnikov.meta.describing.instances

import ru.yudnikov.meta.describing.descriptions.{CaseInstanceDescription, InstanceDescription, PropertyDescription}

/**
  * Created by Don on 01.08.2017.
  */
case class CaseInstancePropertyDescription(name: String, aClass: Class[_], instance: Any, parent: InstanceDescription)
  extends CaseInstanceDescription with PropertyDescription

