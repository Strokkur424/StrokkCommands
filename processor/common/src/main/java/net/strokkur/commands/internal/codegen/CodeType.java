/*
 * StrokkCommands - A super simple annotation based zero-shade Paper command API library.
 * Copyright (C) 2025 Strokkur24
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see <https://www.gnu.org/licenses/>.
 */
package net.strokkur.commands.internal.codegen;

import net.strokkur.commands.internal.codegen.visitor.CodeVisitable;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitor;

public interface CodeType extends CodeVisitable {
  VoidType VOID = new VoidType();
  PrimitiveType BYTE = new PrimitiveType("byte");
  PrimitiveType CHAR = new PrimitiveType("char");
  PrimitiveType SHORT = new PrimitiveType("short");
  PrimitiveType INT = new PrimitiveType("int");
  PrimitiveType LONG = new PrimitiveType("long");
  PrimitiveType FLOAT = new PrimitiveType("float");
  PrimitiveType DOUBLE = new PrimitiveType("double");

  ClassType STRING = CodeType.ofClass(CodeClass.STRING);
  ArrayType STRING_ARRAY = CodeType.ofArray(CodeType.STRING);

  static GenericType generic(String name) {
    return new GenericType(name);
  }

  static ClassType ofClass(CodeClass codeClass) {
    return new ClassType(codeClass);
  }

  static ClassType ofClass(String fqn) {
    return new ClassType(CodeClass.simple(fqn));
  }

  static ArrayType ofArray(CodeType inner) {
    return new ArrayType(inner);
  }

  String name();

  String fullyQualifiedName();

  @Override
  default <R> R accept(CodeVisitor<R> visitor) {
    return visitor.visitType(this);
  }

  abstract class SimpleType implements CodeType {
    private final String name;

    private SimpleType(String name) {
      this.name = name;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public String fullyQualifiedName() {
      return name;
    }
  }

  class VoidType extends PrimitiveType {
    private VoidType() {
      super("void");
    }
  }

  class PrimitiveType extends SimpleType {
    private PrimitiveType(String name) {
      super(name);
    }
  }

  class GenericType extends SimpleType {
    private GenericType(String name) {
      super(name);
    }
  }

  class ClassType implements CodeType {
    private final CodeClass codeClass;

    private ClassType(CodeClass codeClass) {
      this.codeClass = codeClass;
    }

    @Override
    public String name() {
      return codeClass.name();
    }

    @Override
    public String fullyQualifiedName() {
      return codeClass.fullyQualifiedName();
    }
  }

  class ArrayType implements CodeType {
    private final CodeType inner;

    private ArrayType(CodeType inner) {
      this.inner = inner;
    }

    @Override
    public String name() {
      return inner.name() + "[]";
    }

    @Override
    public String fullyQualifiedName() {
      return inner.name() + "[]";
    }

    public CodeType inner() {
      return inner;
    }
  }
}
