package ru.yudnikov.core.modeling

import java.util.UUID

/**
  * Created by Don on 16.08.2017.
  */
case class Reference[T <: Model](aClass: Class[T], id: UUID, count: Long)
                                (implicit private val manager: Manager[_ <: Model]) {
  
  def model: T = manager.get(this).get.asInstanceOf[T]
  
}

object Reference {
  
  def apply(string: String): Option[Reference[Model]] = {
    None
  }
  
}
