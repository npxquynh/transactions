package ru.yudnikov.meta.describing.descriptions

import ru.yudnikov.meta.describing.Reflector
import ru.yudnikov.meta.describing.classes.ClassDescriptionImpl

import scala.reflect.runtime.universe._

/**
  * Created by Don on 01.08.2017.
  */
trait ClassDescription extends Description {
  
  val aType: Type
  
  val children: List[Description] = aType.typeArgs match {
    case List(t) =>
      try
        List(ClassDescriptionImpl(t, Reflector.classByType(t)))
      catch {
        case _: ClassNotFoundException if t.typeSymbol.fullName == "scala.Any" =>
          Nil
        case e: ClassNotFoundException =>
          throw e
      }
    case _ =>
      Nil
  }
}
