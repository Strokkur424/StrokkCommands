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

import net.strokkur.commands.internal.codegen.impl.BasicCodeMethod;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface CodeMethod {

  static CodeMethod.Builder builder() {
    return new Builder();
  }

  static CodeMethod.Builder builder(CodeClass declaringClass, String name) {
    return new Builder()
        .declaringClass(declaringClass)
        .name(name);
  }

  /// Example: `methodName`
  String name();

  /// Example: `methodName(String, int)`
  default String javadocName() {
    return name() + "(" + parameters().stream()
        .map(param -> param.type().fullyQualifiedName())
        .collect(Collectors.joining(", "))
        + ")";
  }

  CodeClass declaredClass();

  CodeType returnType();

  List<CodeParameter> parameters();

  boolean isStatic();

  class Builder {
    private @Nullable CodeClass declaredClass = null;
    private @Nullable String name = null;

    private CodeType returnType = CodeType.VOID;
    private boolean isStatic = false;
    private final List<CodeParameter> parameters = new ArrayList<>();

    public Builder declaringClass(CodeClass declaringClass) {
      this.declaredClass = declaringClass;
      return this;
    }

    public Builder returnType(CodeType returnType) {
      this.returnType = returnType;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder parameters(List<CodeParameter> parameters) {
      this.parameters.clear();
      this.parameters.addAll(parameters);
      return this;
    }

    public Builder parameter(CodeType type, String name) {
      this.parameters.add(CodeParameter.of(type, name));
      return this;
    }

    public Builder setStatic() {
      this.isStatic = true;
      return this;
    }

    public Builder setStatic(boolean value) {
      this.isStatic = value;
      return this;
    }

    public CodeMethod build() {
      Objects.requireNonNull(this.declaredClass);
      Objects.requireNonNull(this.name);
      return new BasicCodeMethod(
          declaredClass,
          returnType,
          name,
          List.copyOf(parameters),
          isStatic
      );
    }
  }
}
