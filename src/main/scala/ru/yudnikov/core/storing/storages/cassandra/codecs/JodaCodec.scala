package ru.yudnikov.core.storing.storages.cassandra.codecs

import java.nio.ByteBuffer

import com.datastax.driver.core.{DataType, ProtocolVersion}
import org.joda.time.DateTime
import ru.yudnikov.core.storing.storages.cassandra.Codec

/**
  * Created by Don on 13.08.2017.
  */
class JodaCodec extends Codec[DateTime](DataType.timestamp(), classOf[DateTime]) {
  
  override def format(value: DateTime): String = s"${value.getMillis.toString}"
  
  override def parse(value: String): DateTime = new DateTime(value.toLong)
  
  override def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): DateTime =
    new DateTime(ByteBuffer.wrap(bytes.array()).getLong)
}