package net.strokkur.commands.internal.codegen.visitor;

import net.strokkur.commands.internal.codegen.CodeAnnotation;
import net.strokkur.commands.internal.codegen.CodeClass;
import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.CodeField;
import net.strokkur.commands.internal.codegen.CodeMethod;
import net.strokkur.commands.internal.codegen.CodePackage;
import net.strokkur.commands.internal.codegen.CodeParameter;
import net.strokkur.commands.internal.codegen.CodeStatement;
import net.strokkur.commands.internal.codegen.CodeType;

public interface CodeVisitor<R> {
  R visitClass(CodeClass codeClass);

  R visitMethod(CodeMethod codeMethod);

  R visitPackage(CodePackage codePackage);

  R visitParameter(CodeParameter codeParameter);

  R visitType(CodeType codeType);

  R visitAnnotation(CodeAnnotation codeAnnotation);

  R visitField(CodeField codeField);

  R visitExpression(CodeExpression codeExpression);

  R visitStatement(CodeStatement codeStatement);
}
