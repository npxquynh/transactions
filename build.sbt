name := "transactions"

version := "1.0"

scalaVersion := "2.11.8"

retrieveManaged := true

// https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
// https://mvnrepository.com/artifact/com.typesafe.scala-logging/scala-logging_2.11
libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.11" % "3.5.0"

// https://mvnrepository.com/artifact/org.scala-lang/scala-parser-combinators
libraryDependencies += "org.scala-lang" % "scala-parser-combinators" % "2.11.0-M4"

// https://mvnrepository.com/artifact/org.scala-stm/scala-stm_2.11
libraryDependencies += "org.scala-stm" % "scala-stm_2.11" % "0.7"

// https://mvnrepository.com/artifact/joda-time/joda-time
libraryDependencies += "joda-time" % "joda-time" % "2.9.9"

// https://mvnrepository.com/artifact/com.datastax.cassandra/cassandra-driver-core
libraryDependencies += "com.datastax.cassandra" % "cassandra-driver-core" % "3.3.0"

// https://mvnrepository.com/artifact/com.typesafe/config
libraryDependencies += "com.typesafe" % "config" % "1.2.1"