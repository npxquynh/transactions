package ru.yudnikov.meta.describing.descriptions

/**
  * Created by Don on 31.07.2017.
  */
trait PropertyDescription extends Description {
  
  val name: String
  
  val parent: Description
  
}
