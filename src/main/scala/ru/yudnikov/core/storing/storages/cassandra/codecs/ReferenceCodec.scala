package ru.yudnikov.core.storing.storages.cassandra.codecs

import com.datastax.driver.core.DataType
import com.datastax.driver.core.exceptions.InvalidTypeException
import ru.yudnikov.core.modeling.{Model, Reference}
import ru.yudnikov.core.storing.storages.cassandra.Codec
import ru.yudnikov.meta.describing.Serialization

/**
  * Created by Don on 13.08.2017.
  */
class ReferenceCodec extends Codec[Reference[Model]](DataType.varchar(), classOf[Reference[Model]]) {
  
  override def format(value: Reference[Model]): String = {
    Serialization.wrap(Serialization.serialize(value).replace("'", "''"))
  }
  
  override def parse(value: String): Reference[Model] = Reference(value) match {
    case Some(ref) => ref
    case _ => throw new InvalidTypeException(s"can't extract Reference from $value")
  }
}