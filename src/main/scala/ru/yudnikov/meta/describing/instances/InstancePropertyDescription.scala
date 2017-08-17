package ru.yudnikov.meta.describing.instances

import ru.yudnikov.meta.describing.descriptions.{InstanceDescription, PropertyDescription}

/**
  * Created by Don on 31.07.2017.
  */
case class InstancePropertyDescription(
                                        name: String,
                                        aClass: Class[_],
                                        instance: Any,
                                        parent: InstanceDescription
                                      ) extends InstanceDescription with PropertyDescription
