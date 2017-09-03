package ru.yudnikov.meta.describing.classes

import ru.yudnikov.core.storing.Storable
import ru.yudnikov.meta.describing.Reflector
import ru.yudnikov.meta.describing.descriptions.{CaseClassDescription, StorableDescription}


/**
  * Created by igor.yudnikov on 01-Aug-17.
  */
case class StorableClassDescription[K](
                                        keyClass: Class[K],
                                        aClass: Class[_ <: Storable[K]]
                                      ) extends StorableDescription[K] with CaseClassDescription {
  
}