package ru.yudnikov.meta

import java.util.{Date, UUID}

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import ru.yudnikov.Dependencies
import ru.yudnikov.core.modeling.{Model, Reference}

/**
  * Created by Don on 8/31/2017.
  */
package object extraction {

  private val logger = Logger(LoggerFactory.getLogger(s""))

  private val extractors: Map[Class[_], Any] = Map(
    classOf[Reference[Model]] -> Reference,
    classOf[org.joda.time.DateTime] -> DateTime,
    classOf[java.util.Date] -> Date,
    classOf[java.util.UUID] -> UUID,
    classOf[java.lang.Boolean] -> Boolean,
    classOf[java.lang.Character] -> Character,
    classOf[java.lang.Short] -> Short,
    classOf[java.lang.Integer] -> Integer,
    classOf[java.lang.Long] -> Long,
    classOf[java.lang.Double] -> Double,
    classOf[java.lang.Float] -> Float,
    classOf[BigDecimal] -> BigDecimal,
    classOf[List[Any]] -> List,
    classOf[scala.Option[Any]] -> Option
  )

  private val unboxers: Map[Class[_], Any] = Map(
    classOf[java.lang.Integer] -> Int
  )

  def unapply(value: Any, maybeClass: Option[Class[_]] = None): String = {

    val tuple = value match {
      case _ if maybeClass.isEmpty =>
        (value, value.getClass)
      case Some(x) =>
        (x, maybeClass.get)
      case None =>
        ("", maybeClass.get)
      case _ =>
        (value, maybeClass.get)
    }

    extractors.get(tuple._2) match {
      case Some(x) =>
        try {
          x.getClass.getMethod("unapply", tuple._2).invoke(x, tuple._1.asInstanceOf[Object]).toString
        } catch {
          case _: NoSuchMethodException => tuple._1.toString
        }
      case _ => tuple._1.toString
    }
  }

  /*def apply(className: String, args: List[Any]): Any = {
    val aClass = Class.forName(className)
    apply(aClass, args.asInstanceOf[Any])
  }*/

/*  def apply(aClass: Class[_], arg: List[Any]): Any = {
    apply(aClass, arg.asInstanceOf[Any])
  }

  def apply(aClass: Class[_], arg: String): Any = {
    apply(aClass, arg.asInstanceOf[Any])
  }*/

  /*def apply(className: String, arg: String): Any = {
    val aClass = Class.forName(className)
    apply(aClass, arg)
  }*/

  def asScala(aClass: Class[_], arg: Any): Any = {
    unboxers.get(aClass) match {
      case Some(x) => try {
        val method = x.getClass.getMethod("unbox", classOf[java.lang.Object])
        method.invoke(x, arg.asInstanceOf[Object])
      } catch {
        case e: NoSuchMethodException =>
          logger.info(s"unbox method not found for $aClass, arg is $arg")
          throw e
        case e: Exception =>
          throw e
      }
      case _ =>
        throw new Exception(s"[${getClass.getSimpleName}]: $aClass has no unboxer!")
    }
  }

  def apply(aClass: Class[_], arg: Any, maybeArgClass: Option[Class[_]] = None): Any = {
    extractors.get(aClass) match {
      case Some(x) => try {
        val method = x.getClass.getMethod("apply", if (maybeArgClass.isEmpty) arg.getClass else maybeArgClass.get)
        method.invoke(x, arg.asInstanceOf[Object]).asInstanceOf[Any]
      } catch {
        case e: NoSuchMethodException =>
          logger.info(s"extractor not found for $aClass, arg is $arg")
          apply(aClass, arg, Some(classOf[Any]))
        case e: Exception =>
          throw e
      }
      case _ =>
        throw new Exception(s"[${getClass.getSimpleName}]: $aClass has no extractor!")
    }
  }

  private object BigDecimal {

    def unapply(value: BigDecimal): String = value.bigDecimal.toString

    def apply(string: String): BigDecimal = try {
      string.toDouble
    } catch {
      case e: Exception =>
        throw e
    }
  }

  private object Boolean {

    def apply(string: String): Boolean = try {
      string.toBoolean
    } catch {
      case e: Exception =>
        throw e
    }
  }

  private object Character {

    def apply(string: String): Char = try {
      string.head
    } catch {
      case e: Exception =>
        throw e
    }
  }

  private object Date {

    def unapply(date: Date): String = date.getTime.toString

    def apply(string: String): Date = try {
      new Date(string.toLong)
    } catch {
      case e: Exception =>
        throw e
    }
  }

  private object DateTime {

    def unapply(date: org.joda.time.DateTime): String = date.getMillis.toString

    def apply(string: String): org.joda.time.DateTime = try {
      new org.joda.time.DateTime(string.toLong)
    } catch {
      case e: Exception =>
        throw e
    }
  }

  private object Double {

    def apply(string: String): Double = try {
      string.toDouble
    } catch {
      case e: Exception =>
        throw e
    }
  }

  private object Float {

    def apply(string: String): Float = try {
      string.toFloat
    } catch {
      case e: Exception =>
        throw e
    }
  }

  private object Integer {

    def apply(string: String): java.lang.Integer = try {
      string.toInt
    } catch {
      case e: Exception =>
        throw e
    }
  }

  private object List {

    def unapply(nil: List[Nothing]): String = ""

    def apply(args: List[Any]): List[Any] = try {
      if (args.nonEmpty)
        args.toList
      else
        Nil
    } catch {
      case e: Exception =>
        throw e
    }
  }

  private object Long {

    def apply(string: String): Long = try {
      string.toLong
    } catch {
      case e: Exception =>
        throw e
    }
  }

  private object Option {

    def apply(string: String): Option[Any] = string match {
      case "" => None
      case _ => Some(string)
      /*case _ => None*/
    }

    def apply(value: Object): Option[Any] = value match {
      case null => None
      case "" => None
      case _ => Some(value)
      /*case _ => None*/
    }

  }

  private object Short {

    def apply(string: String): Short = try {
      string.toShort
    } catch {
      case e: Exception =>
        throw e
    }
  }

  private object UUID {

    def apply(string: String): UUID = try {
      java.util.UUID.fromString(string)
    } catch {
      case e: Exception =>
        throw e
    }
  }

}