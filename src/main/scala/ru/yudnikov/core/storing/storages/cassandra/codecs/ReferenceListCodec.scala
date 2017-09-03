package ru.yudnikov.core.storing.storages.cassandra.codecs

import com.datastax.driver.core.DataType
import ru.yudnikov.core.modeling.{Model, Reference}
import ru.yudnikov.core.storing.storages.cassandra.Codec
import ru.yudnikov.meta.describing.Serializer

/**
  * Created by Don on 13.08.2017.
  */
class ReferenceListCodec extends Codec[List[Reference[Model]]](DataType.list(DataType.varchar()), classOf[List[Reference[Model]]]) {
  
  override def format(value: List[Reference[Model]]): String = {
    value.map(r => "'" + Serializer.serialize(r).replace("'", "''") + "'").mkString("[", ",", "]")
  }
  
  override def parse(value: String): List[Reference[Model]] = Nil
  
}
