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
package net.strokkur.commands.internal.codegen.builder;

import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.CodeStatement;
import net.strokkur.commands.internal.codegen.CodeType;
import net.strokkur.commands.internal.codegen.InvokesMethod;
import net.strokkur.commands.internal.codegen.as.AsExpression;
import net.strokkur.commands.internal.codegen.as.AsStatement;
import net.strokkur.commands.internal.util.ConvertableTo;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodInvocationBuilder implements ConvertableTo<InvokesMethod>, AsExpression, AsStatement {
  private final String methodName;
  private CodeType.@Nullable ClassType type = null;
  private final List<CodeExpression> parameters = new ArrayList<>();
  private @Nullable String instanceVariable = null;
  private boolean newline = false;
  private boolean isStatic = false;
  private final List<InvokesMethod.Chained> chained = new ArrayList<>();

  MethodInvocationBuilder(String methodName) {
    this.methodName = methodName;
  }

  public MethodInvocationBuilder setType(CodeType.ClassType type) {
    this.type = type;
    return this;
  }

  public MethodInvocationBuilder addParameter(AsExpression expression) {
    this.parameters.add(expression.getAsExpression());
    return this;
  }

  public MethodInvocationBuilder setInstanceVariable(@Nullable String instanceVariable) {
    this.instanceVariable = instanceVariable;
    return this;
  }

  public MethodInvocationBuilder setNewline() {
    this.newline = true;
    return this;
  }

  public MethodInvocationBuilder setStatic() {
    this.isStatic = true;
    return this;
  }

  public MethodInvocationBuilder chain(String methodName, boolean newline, AsExpression... parameters) {
    this.chained.add(new InvokesMethod.Chained(
        methodName,
        Arrays.stream(parameters)
            .map(AsExpression::getAsExpression)
            .toList(),
        newline
    ));
    return this;
  }

  public InvokesMethod build() {
    return new InvokesMethod(methodName, type, List.copyOf(parameters), instanceVariable, newline, isStatic, List.copyOf(chained));
  }

  @Override
  public InvokesMethod convert() {
    return build();
  }

  @Override
  public CodeExpression.MethodInvocation getAsExpression() {
    return new CodeExpression.MethodInvocation(build());
  }

  @Override
  public CodeStatement.MethodInvocation getAsStatement() {
    return new CodeStatement.MethodInvocation(build());
  }
}
