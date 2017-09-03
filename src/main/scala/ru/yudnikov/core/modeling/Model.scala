package ru.yudnikov.core.modeling

import java.util.UUID

import ru.yudnikov.core.storing.Storable

/**
  * Created by Don on 16.08.2017.
  */
abstract class Model(val manager: Manager[Model]) extends Storable[UUID] {
  
  val id: UUID

  private val _reference: Reference[_ <: Model] = {
    manager.update(this)
  }
  
  def reference[T <: Model]: Reference[T] = _reference.asInstanceOf[Reference[T]]

}

object Model {

  implicit def toStorable(model: Model): Storable[UUID] = model.asInstanceOf[Storable[UUID]]

}
