/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.google.code.cxf.protobuf.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.code.cxf.protobuf.addressbook.AddressBookProtos.AddressBook;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;

/**
 * Utilities for generateing a protocol buffer service back to .proto source.
 * 
 * @author Gyorgy Orban
 */
public final class ProtobufUtils {

	private ProtobufUtils() {
	}

	public static void generateProtoFromDescriptor(Descriptor descriptor,
			Appendable out) throws IOException {
		HashMap<Descriptor, Boolean> descriptors = new HashMap<Descriptor, Boolean>();
		generateProtoFromDescriptor(descriptor, out, descriptors);
	}

	public static void generateProtoFromDescriptor(Descriptor descriptor,
			Appendable out, HashMap<Descriptor, Boolean> descriptors) throws IOException {
		generateProtoFromDescriptor(descriptor, out, "", descriptors);
		
		// make sure all message type definitions are generated
		for (Descriptor d : new HashSet<Descriptor>(descriptors.keySet())) {
			if (!descriptors.get(d)) {
				generateProtoFromDescriptor(d, out, descriptors);
			}
		}
	}

	public static void generateProtoFromDescriptor(Descriptor descriptor,
			Appendable out, String indent, Map<Descriptor, Boolean> descriptors)
			throws IOException {
		descriptors.put(descriptor, true);

		out.append(indent + "message " + descriptor.getName() + " {\n");

		for (FieldDescriptor fieldDescriptor : descriptor.getFields()) {
			generateProtoFromDescriptor(fieldDescriptor, out, indent + "    ", descriptors);
		}

		for (Descriptor nested : descriptor.getNestedTypes()) {
			generateProtoFromDescriptor(nested, out, indent + "    ",
					descriptors);
		}

		for (EnumDescriptor enumDescriptor : descriptor.getEnumTypes()) {
			generateProtoFromDescriptor(enumDescriptor, out, indent + "    ");
		}

		out.append(indent + "}\n");
	}

	public static void generateProtoFromDescriptor(EnumDescriptor descriptor,
			Appendable out, String indent) throws IOException {
		out.append(indent + "enum " + descriptor.getName() + " {\n");
		for (EnumValueDescriptor valueDescriptor : descriptor.getValues()) {
			generateProtoFromDescriptor(valueDescriptor, out, indent + "    ");
		}

		out.append(indent + "}\n");
	}

	public static void generateProtoFromDescriptor(FileDescriptor descriptor,
			Appendable out) throws IOException {
		String package1 = descriptor.getPackage();
		if (package1 != null) {
			out.append("package " + package1 + ";\n");
		}

		FileOptions options = descriptor.getOptions();
		String javaPackage = options.getJavaPackage();
		if (javaPackage != null) {
			out.append("option java_package = \"" + javaPackage + "\";\n");
		}

		String javaOuterClassname = options.getJavaOuterClassname();
		if (javaOuterClassname != null) {
			out.append("option java_outer_classname = \"" + javaOuterClassname
					+ "\";\n");
		}

		for (ServiceDescriptor serviceDescriptor : descriptor.getServices()) {
			generateProtoFromDescriptor(serviceDescriptor, out);
		}

		for (Descriptor messageDescriptor : descriptor.getMessageTypes()) {
			generateProtoFromDescriptor(messageDescriptor, out, "", new HashMap<Descriptor, Boolean>());
		}

		for (EnumDescriptor enumDescriptor : descriptor.getEnumTypes()) {
			generateProtoFromDescriptor(enumDescriptor, out, "");
		}
	}

	public static void generateProtoFromDescriptor(
			ServiceDescriptor descriptor, Appendable out) throws IOException {
		out.append("service " + descriptor.getName() + " {\n");
		for (MethodDescriptor methodDescriptor : descriptor.getMethods()) {
			generateProtoFromDescriptor(methodDescriptor, out);
		}
		out.append("}\n");
	}

	public static void generateProtoFromDescriptor(MethodDescriptor descriptor,
			Appendable out) throws IOException {
		out.append("    rpc ");
		out.append(descriptor.getName());
		out.append(" (" + descriptor.getInputType().getFullName() + ')');
		out.append(" returns");
		out.append(" (" + descriptor.getOutputType().getFullName() + ')');
		out.append(";\n");
	}

	public static void generateProtoFromDescriptor(FieldDescriptor descriptor,
			Appendable out, String indent, Map<Descriptor, Boolean> descriptors) throws IOException {
		out.append(indent);
		if (descriptor.isRequired()) {
			out.append("required ");
		}

		if (descriptor.isOptional()) {
			out.append("optional ");
		}

		if (descriptor.isRepeated()) {
			out.append("repeated ");
		}

		if (descriptor.getType().equals(Type.MESSAGE)) {
			out.append(descriptor.getMessageType().getFullName() + " ");
			Descriptor messageType = descriptor.getMessageType();
			if (descriptors.get(messageType) == null) {
				descriptors.put(messageType, false);
			}
		} else if (descriptor.getType().equals(Type.ENUM)) {
			out.append(descriptor.getEnumType().getFullName() + " ");
		} else {
			out.append(descriptor.getType().toString().toLowerCase() + " ");
		}

		out.append(descriptor.getName() + " = " + descriptor.getNumber());

		if (descriptor.hasDefaultValue()) {
			out.append(" [default = ");
			Object defaultValue = descriptor.getDefaultValue();

			if (defaultValue instanceof EnumValueDescriptor) {
				out.append(((EnumValueDescriptor) defaultValue).getName());
			}

			out.append("]");
		}

		out.append(";\n");
	}

	public static void generateProtoFromDescriptor(
			EnumValueDescriptor descriptor, Appendable out, String indent)
			throws IOException {
		out.append(indent);

		out.append(descriptor.getName() + " = " + descriptor.getNumber());
		out.append(";\n");
	}

	public static void main(String[] args) throws IOException {
		generateProtoFromDescriptor(AddressBook.getDescriptor().getFile(),
				System.out);
	}

}
