package ru.yudnikov.core.storing.storages.cassandra

import java.nio.ByteBuffer
import java.nio.charset.Charset

import com.datastax.driver.core.{DataType, ProtocolVersion, TypeCodec}

/**
  * Created by Don on 13.08.2017.
  */
abstract class Codec[T](dataType: DataType, aClass: Class[T]) extends TypeCodec[T](dataType, aClass) {
  
  override def serialize(value: T, protocolVersion: ProtocolVersion): ByteBuffer = {
    ByteBuffer.wrap(format(value).getBytes(Charset.defaultCharset))
  }
  
  override def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): T = {
    parse(new String(bytes.array, Charset.defaultCharset()))
  }
}