package ru.yudnikov.meta.describing.descriptions

/**
  * Created by Don on 31.07.2017.
  */
trait StorableDescription[K] extends Description {
  
  val keyClass: Class[K]
  
}
