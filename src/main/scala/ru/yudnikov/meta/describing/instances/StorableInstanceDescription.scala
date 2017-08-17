package ru.yudnikov.meta.describing.instances

import ru.yudnikov.core.storing.Storable
import ru.yudnikov.meta.describing.descriptions.{CaseInstanceDescription, StorableDescription}

import scala.reflect.ClassTag

/**
  * Created by Don on 31.07.2017.
  */
case class StorableInstanceDescription[K, T <: Storable[_] : ClassTag](
                                                          instance: T
                                                        ) extends StorableDescription[K] with CaseInstanceDescription {
  
  override val aClass: Class[T] = instance.getClass.asInstanceOf[Class[T]]

  override val keyClass: Class[K] = instance.id.getClass.asInstanceOf[Class[K]]
}
