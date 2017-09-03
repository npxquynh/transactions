package ru.yudnikov.meta.describing.descriptions

import ru.yudnikov.meta.describing.Reflector
import ru.yudnikov.meta.describing.classes.{CaseClassPropertyDescription, ClassPropertyDescription}

import scala.collection.parallel.mutable.ParSeq

/**
  * Created by Don on 02.08.2017.
  */
trait CaseClassDescription extends Description {
  
  override val children: List[PropertyDescription] = {
    val args = Reflector.unapplyCaseClass(aClass)
    args.map {
      case (name, tpe) if Reflector.isCaseClass(tpe) =>
        CaseClassPropertyDescription(this, name, tpe, Reflector.classByType(tpe))
      case (name, tpe) =>
        ClassPropertyDescription(this, name, tpe)
    }.toList
  }
}
