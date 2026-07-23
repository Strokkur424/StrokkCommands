package net.strokkur.build

import net.strokkur.jap.code.CodeGenUtil
import net.strokkur.jap.code.annotations.CodeAnnotation
import net.strokkur.jap.code.classmodel.CodeClass
import net.strokkur.jap.code.classmodel.CodeField
import net.strokkur.jap.code.classmodel.CodeMethod
import net.strokkur.jap.code.classmodel.builder.ClassBuilder
import net.strokkur.jap.code.classmodel.builder.MethodBuilder
import net.strokkur.jap.code.expression.Expressions
import net.strokkur.jap.code.statement.Statements
import net.strokkur.jap.code.type.CodeTypes
import net.strokkur.jap.code.type.preset.JavaTypes
import net.strokkur.jap.code.util.Modifiers
import net.strokkur.jap.code.util.StyleConfig
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.net.URI
import java.nio.file.Path

@DisableCachingByDefault
abstract class ParsePaperApiJarTask : DefaultTask() {
  @get:Input
  abstract val url: Property<String>

  @get:Input
  abstract val target: Property<String>

  @get:OutputDirectory
  abstract val targetDir: DirectoryProperty

  @TaskAction
  fun run() {
    val apiJar = targetDir.file("external-data/api.jar").get().asFile
    apiJar.parentFile.mkdirs()

    val url = URI(url.get()).toURL()
    url.openStream().use { input ->
      apiJar.outputStream().use { output ->
        input.copyTo(output)
      }
    }

    val createdClass = createClass(apiJar.toPath())
    val javaSource = CodeGenUtil.createJavaFile(createdClass)

    val targetFile = targetDir.file("generated/${target.get().replace(".", "/")}.java").get().asFile
    targetFile.parentFile.mkdirs()
    targetFile.writeText(javaSource)
  }

  fun createClass(path: Path): CodeClass {
    val builder = ClassBuilder(CodeTypes.ofClass(target.get()))
    builder.addAnnotations(
      CodeAnnotation.of(
        ConverterTypes.AUTO_SERVICE,
        ConverterTypes.BRIGADIER_ARGUMENT_CONVERTER.chainField("class")
      )
    )
    builder.addModifiers(Modifiers.PUBLIC, Modifiers.FINAL)
    builder.extendsClass(ConverterTypes.BRIGADIER_ARGUMENT_CONVERTER)

    builder.addFields(
      CodeField.builder(ConverterTypes.CODE_CLASS_TYPE, "ARGUMENT_TYPES")
        .addModifiers(Modifiers.PRIVATE, Modifiers.STATIC, Modifiers.FINAL)
        .setInitializer(
          ConverterTypes.CODE_TYPES.chainMethod("ofClass")
            .addParameters(Expressions.string(ConverterTypes.ARGUMENT_TYPES.fqn))
        ),
      CodeField.builder(ConverterTypes.CODE_CLASS_TYPE, "REGISTRY_KEY")
        .addModifiers(Modifiers.PRIVATE, Modifiers.STATIC, Modifiers.FINAL)
        .setInitializer(
          ConverterTypes.CODE_TYPES.chainMethod("ofClass")
            .addParameters(Expressions.string(ConverterTypes.REGISTRY_KEY.fqn))
        ),
      CodeField.builder(ConverterTypes.CODE_CLASS_TYPE, "REGISTRY_ARGUMENT_EXTRACTOR")
        .addModifiers(Modifiers.PRIVATE, Modifiers.STATIC, Modifiers.FINAL)
        .setInitializer(
          ConverterTypes.CODE_TYPES.chainMethod("ofClass")
            .addParameters(Expressions.string(ConverterTypes.REGISTRY_ARGUMENT_EXTRACTOR.fqn))
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
        Expressions.methodInvocation("putRegistry")
          .addParameters(
            Expressions.string(registryKeyType.name),
            ConverterTypes.CODE_TYPES.chainMethod(
              "ofClass",
              Expressions.string(registryKeyType.type.identifiableName())
            )
          )
      )
    }

    builder.addMethods(initializeBuilder, createPutRegistryMethod())
    return builder.build()
  }

  private fun createPutRegistryMethod(): MethodBuilder {
    return CodeMethod.builder("putRegistry")
      .addModifiers(Modifiers.PRIVATE)
      .addParameter(JavaTypes.STRING, "name")
      .addParameter(ConverterTypes.CODE_CLASS_TYPE, "type")
      .setCode(
        Expressions.methodInvocation("putFor")
          .addParameters(
            Expressions.lambdaInline(
              listOf("p", "argname"), ConverterTypes.BRIGADIER_ARGUMENT_TYPE.chainMethod("of")
                .addParameters(
                  Expressions.variable("ARGUMENT_TYPES").chainMethod(
                    "chainMethod",
                    Expressions.string("resource"),
                    Expressions.variable("REGISTRY_KEY").chainMethod("chainField", Expressions.variable("name"))
                  ),
                  ConverterTypes.EXPRESSIONS
                    .chainMethod("variable", Expressions.string("ctx"))
                    .chainMethod(
                      "chainMethod",
                      Expressions.string("getArgument"),
                      ConverterTypes.EXPRESSIONS.chainMethod("string", Expressions.variable("argname")),
                      Expressions.variable("type").chainMethod("chainField", Expressions.string("class"))
                    ).setStyle(StyleConfig.MULTILINE)
                ).setStyle(StyleConfig.MULTILINE)
            )
          ),

        Expressions.methodInvocation("putFor")
          .addParameters(
            Expressions.lambdaInline(
              listOf("p", "argname"), ConverterTypes.BRIGADIER_ARGUMENT_TYPE.chainMethod("of")
                .addParameters(
                  Expressions.variable("ARGUMENT_TYPES").chainMethod(
                    "chainMethod",
                    Expressions.string("resourceKey"),
                    Expressions.variable("REGISTRY_KEY").chainMethod("chainField", Expressions.variable("name"))
                  ),
                  Expressions.variable("REGISTRY_ARGUMENT_EXTRACTOR").chainMethod(
                    "chainMethod",
                    Expressions.string("getTypedKey"),
                    ConverterTypes.EXPRESSIONS.chainMethod("variable", Expressions.string("ctx")),
                    Expressions.variable("REGISTRY_KEY").chainMethod("chainField", Expressions.variable("name")),
                    ConverterTypes.EXPRESSIONS.chainMethod("string", Expressions.variable("argname")),
                  ).setStyle(StyleConfig.MULTILINE)
                ).setStyle(StyleConfig.MULTILINE)
            )
          ),
      )
  }
}
