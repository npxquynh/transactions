import java.util.UUID

import ru.yudnikov.core.Commons
import models.Person
import org.joda.time.DateTime

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.stm._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag
import scala.util.{Failure, Random, Success, Try}


/**
  * Created by Don on 15.08.2017.
  */
object MyApp extends App {
  
  def body = {
    //atomic { implicit txn =>
    val oleg = new Person("Oleg", None, None)
    val oleg2 = new Person("Oleg2", None, Some(oleg.reference), oleg.id)
    /*println(oleg)
    println(oleg.reference)*/
    val igor = new Person("Igor", Some(oleg.reference), Some(oleg2.reference))
    //println(igor.reference.model)
    /*println(igor)
    println(igor.reference)
    println(igor.reference.model)
    println(igor.reference.model.reference)*/
    //}
    
  }
  
  Commons.repeat(body, 1)
  
  Thread.sleep(1000)
  
  Person.trace()
  
}

