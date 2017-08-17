package ru.yudnikov

import java.io.File
import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try

/**
  * Created by Don on 07.08.2017.
  */
object Dependencies {
  
  implicit val conf: Config = ConfigFactory.parseFile(new File(s"src/main/resources/application.conf"))
  
}
