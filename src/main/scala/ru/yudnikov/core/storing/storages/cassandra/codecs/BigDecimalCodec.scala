package ru.yudnikov.core.storing.storages.cassandra.codecs

import java.math.BigInteger
import java.nio.ByteBuffer

import com.datastax.driver.core.TypeCodec.DecimalCodec
import com.datastax.driver.core.{DataType, ProtocolVersion, TypeCodec}

/**
  * Created by Don on 13.08.2017.
  */
class BigDecimalCodec extends TypeCodec[scala.math.BigDecimal](DataType.decimal(), classOf[scala.math.BigDecimal]) {
  
  override def format(value: BigDecimal): String = value.toString
  
  override def parse(value: String): BigDecimal = BigDecimal(value)
  
  override def serialize(value: BigDecimal, protocolVersion: ProtocolVersion): ByteBuffer = {
    val bigInteger = value.bigDecimal.unscaledValue
    val scale = value.scale
    val bigIntegerBytes = bigInteger.toByteArray
    val bytes = ByteBuffer.allocate(4 + bigIntegerBytes.length)
    bytes.putInt(scale)
    bytes.put(bigIntegerBytes)
    bytes.rewind().asInstanceOf[ByteBuffer]
  }
  
  override def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): BigDecimal = ???
  
}
