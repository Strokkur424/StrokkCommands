package net.strokkur.build

import net.strokkur.build.ConverterTypes.*
import net.strokkur.jap.code.annotations.CodeAnnotation
import net.strokkur.jap.code.classmodel.CodeClass
import net.strokkur.jap.code.classmodel.CodeMethod
import net.strokkur.jap.code.classmodel.builder.ClassBuilder
import net.strokkur.jap.code.convert.ConvertToExpression
import net.strokkur.jap.code.convert.ConvertToStatement
import net.strokkur.jap.code.expression.Expressions
import net.strokkur.jap.code.expression.Expressions.methodInvocation
import net.strokkur.jap.code.expression.Expressions.string
import net.strokkur.jap.code.statement.Statements
import net.strokkur.jap.code.type.CodeTypes
import net.strokkur.jap.code.util.Modifiers
import java.nio.file.Path

internal class PaperArgumentConverterBuilder(
  val target: String,
  val path: Path
) {
  fun isHandledSeparately(type: ArgumentType): Boolean {
    return type.returnType.name().endsWith("Resolver")
      || type.returnType.name() == "Integer"
      || type.args.isNotEmpty()
  }

  fun createClass(): CodeClass {
    val builder = ClassBuilder(CodeTypes.ofClass(target))
    builder.addAnnotations(
      CodeAnnotation.of(
        AUTO_SERVICE,
        BRIGADIER_ARGUMENT_CONVERTER.chainField("class")
      )
    )
    builder.addModifiers(Modifiers.PUBLIC, Modifiers.FINAL)
    builder.extendsClass(UNIQUE_PAPER_BRIGADIER_ARGUMENT_CONVERTER)

    val initializeBuilder = CodeMethod.builder("initializeArguments")
      .addModifiers(Modifiers.PROTECTED)
      .addAnnotations(CodeTypes.ofJavaClass(Override::class.java))

    val initializeArgumentsCode: MutableList<ConvertToStatement> = mutableListOf(
      Expressions.superExpr().chainMethod("initializeArguments"),
      Statements.blank()
    )

    val unhandled: MutableList<ArgumentType> = mutableListOf()
    for (argumentType in ArgumentTypesIterator(path)) {
      if (isHandledSeparately(argumentType)) {
        unhandled.add(argumentType)
        continue
      }

      initializeArgumentsCode.add(
        methodInvocation("putSimple")
          .addParameters(
            string(argumentType.methodName),
            string(argumentType.returnType.identifiableName())
          )
      )
    }

    initializeArgumentsCode.add(Statements.blank())
    for (registryKeyType in RegistryKeyTypeIterator(path)) {
      val type: ConvertToExpression

      val typeName = registryKeyType.type.identifiableName()
      if (typeName.endsWith("<?>")) {
        type = CODE_TYPES.chainMethod(
          "ofClass",
          string(typeName.substring(0, typeName.length - 3))
        ).chainMethod("typed", CODE_TYPES.chainMethod("genericWildcard"))
      } else {
        type = CODE_TYPES.chainMethod(
          "ofClass",
          string(typeName)
        )
      }

      initializeArgumentsCode.add(
        methodInvocation("putRegistry")
          .addParameters(
            string(registryKeyType.name),
            type
          )
      )
    }

    // Add unhandled arguments type to the start as a comment.
    val unhandledArgumentTypes = unhandled.map { "\n- ${it}" }.joinToString("")
    if (unhandled.isNotEmpty()) {
      initializeArgumentsCode.addFirst(
        Statements.comment(
          "The following arguments were skipped. These should be handled in the superclass:${unhandledArgumentTypes}"
        )
      )
    }

    initializeBuilder.setCode(*initializeArgumentsCode.toTypedArray())
    builder.addMethods(initializeBuilder)
    return builder.build()
  }
}
