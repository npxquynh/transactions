package ru.yudnikov.meta.parsing

import scala.collection.parallel.ParMap
import scala.util.parsing.combinator.syntactical.StandardTokenParsers

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

  lexical.delimiters ++= List("(", ")", ",", ".", "->")

  def className: Parser[String] = repsep(ident, ".").map(_.mkString("."))

  def description: Parser[(String, ParMap[String, Any])] = (className ~ ("(" ~> repsep(property, ",") <~ ")")).map(x => x._1 -> x._2.toMap.par)

  def property: Parser[(String, Any)] = (ident ~ "->" ~ value).map(x => x._1._1 -> x._2)

  def instance: Parser[(String, List[Any])] = (className ~ ("(" ~> repsep(value, ",") <~ ")")).map(x => x._1 -> x._2)

  def value: Parser[Any] = description | instance | stringLit | numericLit | className

  lazy val result: ParseResult[Any] = phrase(description)(new lexical.Scanner(string))
  
}
