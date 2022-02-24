/*
 * Copyright 2021 Lightbend Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.akkaserverless.javasdk.impl

import com.google.protobuf.{ ByteString, Descriptors, Message => JavaMessage, Parser }
import java.util.concurrent.CompletionStage

import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.dataformat.protobuf.ProtobufMapper
import com.fasterxml.jackson.dataformat.protobuf.ProtobufMapper
import com.google.protobuf.UnsafeByteOperations

/**
 * A resolved service method.
 */
final case class ResolvedServiceMethod[I, O](
    descriptor: Descriptors.MethodDescriptor,
    inputType: ResolvedType[I],
    outputType: ResolvedType[O]) {

  def outputStreamed: Boolean = descriptor.isServerStreaming
  def name: String = descriptor.getName

  def method(): Descriptors.MethodDescriptor = descriptor
}

/**
 * A resolved type
 */
trait ResolvedType[T] {

  /**
   * The class for this type.
   */
  def typeClass: Class[T]

  /**
   * The URL for this type.
   */
  def typeUrl: String

  /**
   * Parse the given bytes into this type.
   */
  def parseFrom(bytes: ByteString): T

  /**
   * Convert the given value into a byte string.
   */
  def toByteString(value: T): ByteString
}

private final class JavaPbResolvedType[T <: JavaMessage](
    override val typeClass: Class[T],
    override val typeUrl: String,
    parser: Parser[T])
    extends ResolvedType[T] {
  override def parseFrom(bytes: ByteString): T = parser.parseFrom(bytes)
  override def toByteString(value: T): ByteString = value.toByteString
}

private final class ScalaPbResolvedType[T <: scalapb.GeneratedMessage](
    override val typeClass: Class[T],
    override val typeUrl: String,
    companion: scalapb.GeneratedMessageCompanion[_])
    extends ResolvedType[T] {
  override def parseFrom(bytes: ByteString): T = companion.parseFrom(bytes.newCodedInput()).asInstanceOf[T]
  override def toByteString(value: T): ByteString = value.toByteString
}

private final class ProtoJacksonResolvedType[T](
    override val typeClass: Class[T],
    override val typeUrl: String,
    mapper: ProtobufMapper)
    extends ResolvedType[T] {
  val schemaWrapper = mapper.generateSchemaFor(typeClass)

  override def parseFrom(bytes: ByteString): T =
    mapper
      .readerFor(typeClass)
      .`with`(schemaWrapper)
      .readValue(bytes.toByteArray)

  override def toByteString(value: T): ByteString = {
    val array = mapper.writer(schemaWrapper).writeValueAsBytes(value)
    ByteString.copyFrom(array) // FIXME: how to avoid this copy?
  }
}

trait ResolvedEntityFactory {
  // TODO JavaDoc
  def resolvedMethods: Map[String, ResolvedServiceMethod[_, _]]
}
