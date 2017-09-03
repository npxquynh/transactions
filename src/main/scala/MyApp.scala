import java.util.UUID

import ru.yudnikov.core.Commons
import models.Person
import org.joda.time.DateTime
import ru.yudnikov.meta.parsing.Parser

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

  /*
  def body = {

    val mother = new Person("Oleg", None, None)
    val father = new Person("Oleg2", None, Some(mother.reference))
    val igor = new Person("Igor", Some(mother.reference), Some(father.reference))
    val string = igor.toString
    println(string)
    val p = new Parser(string).result
    println(p)
    /* igor.save()
    Thread.sleep(10000)
    mother.save() */
  }
  
  Commons.repeat(body, 1)
  
  Thread.sleep(30000)
  
  Person.trace()
  */

  val mother = new Person("Mom", None, None)
  val father = new Person("Dad", None, Some(mother.reference))
  val igor = new Person("Igor", Some(mother.reference), Some(father.reference))
  val string = igor.toString
  println(string)
  val p = new Parser(string)
  println(p.x)

}
