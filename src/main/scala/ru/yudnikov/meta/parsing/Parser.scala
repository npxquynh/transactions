package ru.yudnikov.meta.parsing

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import ru.yudnikov.meta.describing.{Reflector, Serializer}

import scala.collection.parallel.ParMap
import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import ru.yudnikov.meta.extraction._

/**
  * description: some.class.name(p1, p2, ..., pn)
  * property: name -> value
  * value: 123 | 'string' | instance
  * instance: some.class.name(some.class.name(string)) - nested n times
  * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  *
  * @param string - string to parse
  *               i.e. "ru.yudnikov.models.User(int -> scala.Int(12, 'asd'), d -> scala.Double(12), s -> 'Oleg12')"
  */
class Parser(string: String) extends StandardTokenParsers {

  private val logger = Logger(LoggerFactory.getLogger(getClass.getSimpleName))

  lexical.delimiters ++= List("(", ")", ",", ".", "->")

  def className: Parser[String] = repsep(ident, ".").map(_.mkString("."))

  def description: Parser[(String, Map[String, Any])] = (className ~ ("(" ~> repsep(property, ",") <~ ")")).map(x => x._1 -> x._2.toMap)

  def property: Parser[(String, Any)] = (ident ~ "->" ~ value).map(x => x._1._1 -> x._2)

  def instance: Parser[(String, List[Any])] = (className ~ ("(" ~> repsep(value, ",") <~ ")")).map(x => x._1 -> x._2)

  def value: Parser[Any] = description | instance | stringLit | numericLit | className

  lazy val result: ParseResult[Any] = phrase(description)(new lexical.Scanner(string))

  def instantiate(x: Any): Any = x match {
    case (className: String, m: Map[String, Any]) =>
      val aClass = Class.forName(className)
      val args = m.map(t => t._1 -> instantiate(t._2))
      logger.debug(s"instantiating $aClass with args $args")
      val x = Reflector.applyStorableInstance(aClass, args)
      x
    case s: String =>
      s
    case (className: String, s: String) =>
      s
    case (className: String, List(arg: String)) =>
      apply(Class.forName(className), arg)
    case (className: String, args: List[Any]) if !args.exists(_.getClass != classOf[String]) =>
      apply(Class.forName(className), args)
    case (className: String, List(arg: Any)) =>
      apply(Class.forName(className), instantiate(arg))
    case (className: String, args: List[Any]) =>
      apply(Class.forName(className), args.map(instantiate))
    case _ =>
      logger.warn(s"unmatched case for value $x")
  }

  lazy val x: Any = instantiate(result.get)
  
}
