package ru.yudnikov.core.storing

import com.typesafe.scalalogging.Logger

import scala.util.{Failure, Success, Try}

/**
  * Created by Don on 07.08.2017.
  */
class Locker[K](logger: Logger) {

  private var locked: Set[K] = Set()

  def lock(id: K): Try[Unit] = {
    if (!locked.contains(id))
      Success {
        locked = locked + id
        logger.debug(s"locked id $id")
      }
    else
      Failure {
        new Exception(s"can't lock already locked id $id")
      }
  }

  def unlock(id: K): Try[Unit] = {
    if (locked.contains(id))
      Success {
        locked = locked - id
        logger.debug(s"unlocked $id")
      }
    else
      Failure {
        new Exception(s"can't unlock non locked id $id")
      }
  }
}