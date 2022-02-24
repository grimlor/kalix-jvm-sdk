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

import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

import scala.jdk.CollectionConverters.IterableHasAsJava
import scala.jdk.CollectionConverters.IterableHasAsScala

import com.akkaserverless.javasdk.action.Action
import com.fasterxml.jackson.dataformat.protobuf.ProtobufMapper
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchema
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Descriptors

object ProtoDescriptorGenerator {

  private object InternalGenerator {

    private def returnParamTypeName(method: Method) = {
      val genericReturnType = method.getGenericReturnType.asInstanceOf[ParameterizedType]
      genericReturnType.getActualTypeArguments()(0).getTypeName
    }

    private def returnParamType(method: Method) =
      try {
        // FIXME: don't load class, but extract the simple name using regex
        Class.forName(returnParamTypeName(method))
      } catch {
        case e: ClassNotFoundException =>
          throw new RuntimeException(e)
      }

    private def buildMessageType(protobufSchema: ProtobufSchema): DescriptorProtos.DescriptorProto = {
      // FIXME: still need to find out how is this structure.
      // I guess we will need to traverse the type looking for nested types (non scalar types)

      val fields = protobufSchema.getRootType.fields().asScala.map { field =>
        DescriptorProtos.FieldDescriptorProto.newBuilder
          .setName(field.name)
          .setNumber(field.id)
          // FIXME: we will have to map all scalar types by had, for now, only Long
          .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64)
          .build()
      }

      DescriptorProtos.DescriptorProto.newBuilder
        .setName(protobufSchema.getRootType.getName)
        .addAllField(fields.asJava)
        .build()
    }

    def genFileDescriptor(name: String, packageName: String, handlers: Seq[Method]): Descriptors.FileDescriptor = {

      val protoMapper = new ProtobufMapper

      val protoBuilder = DescriptorProtos.FileDescriptorProto.newBuilder
      protoBuilder
        .setName(name + ".proto") // FIXME: snake_case this ?!
        .setSyntax("proto3")
        .setPackage(packageName)
        .setOptions(DescriptorProtos.FileOptions.newBuilder.setJavaMultipleFiles(true).build)

      // build messages types
      handlers
        // we pick the return type and the first input param.
        // We only support methods with exactly one param!
        .flatMap(method => Set(returnParamType(method), method.getParameterTypes()(0)))
        // types can be used many times, we need unique descriptors
        .toSet
        .foreach { messageType: Class[_] =>
          val schema = protoMapper.generateSchemaFor(messageType)
          protoBuilder.addMessageType(buildMessageType(schema))
        }

      // build gRPC service
      val serviceBuilder = DescriptorProtos.ServiceDescriptorProto.newBuilder
      serviceBuilder.setName(name)

      handlers.foreach { method =>
        val methodBuilder = DescriptorProtos.MethodDescriptorProto.newBuilder
        val input = method.getParameterTypes()(0).getName
        val output = returnParamType(method).getName

        methodBuilder
          .setName(method.getName.capitalize)
          .setInputType(input)
          .setOutputType(output)

        serviceBuilder.addMethod(methodBuilder.build())
      }

      protoBuilder.addService(serviceBuilder.build())

      // finally build all final descriptor
      Descriptors.FileDescriptor.buildFrom(protoBuilder.build, new Array[Descriptors.FileDescriptor](0))
    }
  }

  def generateFileDescriptorAction(component: Class[_]): Descriptors.FileDescriptor = {

    val handler =
      component.getDeclaredMethods
        .filter(_.getReturnType == classOf[Action.Effect[_]])
        // actions have only one input param, always
        .filter(_.getParameters.length == 1)

    InternalGenerator.genFileDescriptor(component.getSimpleName, component.getPackageName, handler)
  }
}
