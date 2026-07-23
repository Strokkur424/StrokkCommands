package net.strokkur.build

import net.strokkur.build.ConverterTypes.*
import net.strokkur.jap.code.annotations.CodeAnnotation
import net.strokkur.jap.code.classmodel.CodeClass
import net.strokkur.jap.code.classmodel.CodeField
import net.strokkur.jap.code.classmodel.CodeMethod
import net.strokkur.jap.code.classmodel.CodeParameterDefinition.ofVarargs
import net.strokkur.jap.code.classmodel.builder.ClassBuilder
import net.strokkur.jap.code.classmodel.builder.MethodBuilder
import net.strokkur.jap.code.expression.Expressions
import net.strokkur.jap.code.expression.Expressions.*
import net.strokkur.jap.code.statement.Statements
import net.strokkur.jap.code.statement.Statements.variableDeclarationFinal
import net.strokkur.jap.code.type.CodeTypes
import net.strokkur.jap.code.type.preset.JavaTypes
import net.strokkur.jap.code.util.Modifiers
import net.strokkur.jap.code.util.StyleConfig
import java.nio.file.Path

internal class PaperArgumentConverterBuilder(
  val target: String,
  val path: Path
) {
  val customHandled: MutableList<ArgumentType> = mutableListOf()

  fun needsCustomHandling(type: ArgumentType): Boolean {
    val result = false
    if (result) {
      customHandled.add(type)
    }
    return result
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
    builder.extendsClass(BRIGADIER_ARGUMENT_CONVERTER)

    builder.addFields(
      CodeField.builder(CODE_CLASS_TYPE, "ARGUMENT_TYPES")
        .addModifiers(Modifiers.PRIVATE, Modifiers.STATIC, Modifiers.FINAL)
        .setInitializer(
          CODE_TYPES.chainMethod("ofClass")
            .addParameters(string(ARGUMENT_TYPES.fqn))
        ),
      CodeField.builder(CODE_CLASS_TYPE, "REGISTRY_KEY")
        .addModifiers(Modifiers.PRIVATE, Modifiers.STATIC, Modifiers.FINAL)
        .setInitializer(
          CODE_TYPES.chainMethod("ofClass")
            .addParameters(string(REGISTRY_KEY.fqn))
        ),
      CodeField.builder(CODE_CLASS_TYPE, "REGISTRY_ARGUMENT_EXTRACTOR")
        .addModifiers(Modifiers.PRIVATE, Modifiers.STATIC, Modifiers.FINAL)
        .setInitializer(
          CODE_TYPES.chainMethod("ofClass")
            .addParameters(string(REGISTRY_ARGUMENT_EXTRACTOR.fqn))
        ),
    )

    val initializeBuilder = CodeMethod.builder("initializeArguments")
      .addModifiers(Modifiers.PROTECTED)
      .addAnnotations(CodeTypes.ofJavaClass(Override::class.java))
    initializeBuilder.addCode(
      Expressions.superExpr().chainMethod("initializeArguments"),
      Statements.blank()
    )

    for (argumentType in ArgumentTypesIterator(path)) {
      println("ArgumentType: " + argumentType)
    }

    println()

    for (registryKeyType in RegistryKeyTypeIterator(path)) {
      initializeBuilder.addCode(
        methodInvocation("putRegistry")
          .addParameters(
            string(registryKeyType.name),
            CODE_TYPES.chainMethod(
              "ofClass",
              string(registryKeyType.type.identifiableName())
            )
          )
      )
    }

    builder.addMethods(initializeBuilder, createPutSimpleMethod(), createPutRegistryMethod())
    return builder.build()
  }

  private fun createPutSimpleMethod(): MethodBuilder {
    return CodeMethod.builder("putSimple")
      .addModifiers(Modifiers.PRIVATE)
      .addParameter(JavaTypes.STRING, "methodName")
      .addParameter(JavaTypes.STRING, "returnTypeFqn")
      .addParameters(ofVarargs(CODE_TYPE, "extraTypes"))
      .setCode(
        variableDeclarationFinal(
          CODE_CLASS_TYPE,
          "returnType",
          CODE_TYPES.chainMethod("ofClass", variable("returnTypeFqn"))
        ),
        variableDeclarationFinal(
          JavaTypes.LIST.typed(CODE_TYPE),
          "types",
          JavaTypes.ARRAY_LIST.typed().ctor(JavaTypes.ARRAYS.chainMethod("asList", variable("extraTypes")))
        ),
        variable("types").chainMethod("add", variable("returnType")),
        methodInvocation("putFor").addParameters(
          lambdaInline(
            listOf("p", "name"), BRIGADIER_ARGUMENT_TYPE.chainMethod(
              "of",
              variable("ARGUMENT_TYPES").chainMethod("chainMethod", variable("methodName")),
              EXPRESSIONS.chainMethod("variable", string("ctx"))
                .chainMethod(
                  "chainMethod",
                  string("getArgument"),
                  EXPRESSIONS.chainMethod("string", variable("name")),
                  variable("returnType").chainMethod("chainField", string("class"))
                ).setStyle(StyleConfig.MULTILINE)
            ).setStyle(StyleConfig.MULTILINE)
          ), variable("types")
        )
      )
  }

  private fun createPutRegistryMethod(): MethodBuilder {
    return CodeMethod.builder("putRegistry")
      .addModifiers(Modifiers.PRIVATE)
      .addParameter(JavaTypes.STRING, "name")
      .addParameter(CODE_CLASS_TYPE, "type")
      .setCode(
        methodInvocation("putFor")
          .addParameters(
            lambdaInline(
              listOf("p", "argName"), BRIGADIER_ARGUMENT_TYPE.chainMethod("of")
                .addParameters(
                  variable("ARGUMENT_TYPES").chainMethod(
                    "chainMethod",
                    string("resource"),
                    variable("REGISTRY_KEY").chainMethod("chainField", variable("name"))
                  ),
                  EXPRESSIONS
                    .chainMethod("variable", string("ctx"))
                    .chainMethod(
                      "chainMethod",
                      string("getArgument"),
                      EXPRESSIONS.chainMethod("string", variable("argName")),
                      variable("type").chainMethod("chainField", string("class"))
                    ).setStyle(StyleConfig.MULTILINE)
                ).setStyle(StyleConfig.MULTILINE)
            )
          ),

        methodInvocation("putFor")
          .addParameters(
            lambdaInline(
              listOf("p", "argName"), BRIGADIER_ARGUMENT_TYPE.chainMethod("of")
                .addParameters(
                  variable("ARGUMENT_TYPES").chainMethod(
                    "chainMethod",
                    string("resourceKey"),
                    variable("REGISTRY_KEY").chainMethod("chainField", variable("name"))
                  ),
                  variable("REGISTRY_ARGUMENT_EXTRACTOR").chainMethod(
                    "chainMethod",
                    string("getTypedKey"),
                    EXPRESSIONS.chainMethod("variable", string("ctx")),
                    variable("REGISTRY_KEY").chainMethod("chainField", variable("name")),
                    EXPRESSIONS.chainMethod("string", variable("argName")),
                  ).setStyle(StyleConfig.MULTILINE)
                ).setStyle(StyleConfig.MULTILINE)
            )
          ),
      )
  }
}
