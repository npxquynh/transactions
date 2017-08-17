package ru.yudnikov.meta.describing.descriptions

import ru.yudnikov.meta.describing.Serialization
import ru.yudnikov.meta.describing.instances.InstanceDescriptionImpl

/**
  * Created by Don on 31.07.2017.
  */
trait InstanceDescription extends Description {
  
  val instance: Any

  val children: List[InstanceDescription] = instance match {
    case list: Iterable[Any] =>
      list.par.map(item => InstanceDescriptionImpl(item, item.getClass)).toList
    case opt: Option[_] =>
      opt match {
        case Some(inst) => List(InstanceDescriptionImpl(inst, inst.getClass))
        case None => Nil
      }
    case Some(inst) =>
      List(InstanceDescriptionImpl(inst, inst.getClass))
    case _ =>
      Nil
  }
  
  override def hashCode(): Int = 41 * aClass.hashCode() * children.hashCode() * children.map {
    case d: InstanceDescription => d.instance.hashCode()
    case _ => 1
  }.product
  
  override def equals(obj: Any): Boolean = obj match {
    case x: InstanceDescription if x.instance == instance => true
    case _ => false
  }
  
  override def toString: String = if (children.isEmpty) Serialization.serialize(instance) else super.toString
  
}
