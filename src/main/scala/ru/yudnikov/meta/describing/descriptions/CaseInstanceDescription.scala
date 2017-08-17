package ru.yudnikov.meta.describing.descriptions

import ru.yudnikov.meta.describing.Reflection
import ru.yudnikov.meta.describing.instances.{CaseInstancePropertyDescription, InstancePropertyDescription}

/**
  * Created by Don on 01.08.2017.
  */
trait CaseInstanceDescription extends InstanceDescription {
  
  override val children: List[InstanceDescription with PropertyDescription] = {
    val args = Reflection.unapplyCaseInstance(instance).map(arg => arg._1 -> (Reflection.classByType(arg._2._1) -> arg._2._2))
    args.map {
      case (name, (cls, inst)) if Reflection.isCaseClass(cls) => InstancePropertyDescription(name, cls, inst, this)
      case (name, (cls, inst)) => InstancePropertyDescription(name, cls, inst, this)
    }.toList
  }
}
