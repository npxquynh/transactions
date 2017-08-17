package ru.yudnikov.meta.describing.instances

import ru.yudnikov.meta.describing.descriptions.{CaseInstanceDescription, Description}

/**
  * Created by Don on 01.08.2017.
  */
case class CaseInstanceDescriptionImpl(
                               instance: Any
                             ) extends CaseInstanceDescription {

  override val aClass: Class[_] = instance.getClass
}
