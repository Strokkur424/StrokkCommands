package net.strokkur.build

import dev.denwav.hypo.asm.AsmClassDataProvider
import dev.denwav.hypo.asm.HypoAsm
import dev.denwav.hypo.core.HypoContext
import dev.denwav.hypo.model.ClassProviderRoot
import dev.denwav.hypo.types.PrimitiveType
import dev.denwav.hypo.types.VoidType
import dev.denwav.hypo.types.desc.ArrayTypeDescriptor
import dev.denwav.hypo.types.desc.ClassTypeDescriptor
import dev.denwav.hypo.types.desc.TypeDescriptor
import dev.denwav.hypo.types.kind.ClassType
import dev.denwav.hypo.types.pattern.TypePattern
import dev.denwav.hypo.types.pattern.TypePatterns
import dev.denwav.hypo.types.sig.ArrayTypeSignature
import dev.denwav.hypo.types.sig.ClassTypeSignature
import dev.denwav.hypo.types.sig.TypeSignature
import dev.denwav.hypo.types.sig.param.TypeArgument
import dev.denwav.hypo.types.sig.param.TypeVariable
import dev.denwav.hypo.types.sig.param.WildcardArgument
import net.strokkur.jap.code.CodeGenUtil
import net.strokkur.jap.code.annotations.CodeAnnotation
import net.strokkur.jap.code.classmodel.CodeClass
import net.strokkur.jap.code.classmodel.CodeField
import net.strokkur.jap.code.classmodel.CodeMethod
import net.strokkur.jap.code.classmodel.builder.ClassBuilder
import net.strokkur.jap.code.classmodel.builder.MethodBuilder
import net.strokkur.jap.code.convert.ConvertToClassType
import net.strokkur.jap.code.convert.ConvertToGenericType
import net.strokkur.jap.code.expression.Expressions
import net.strokkur.jap.code.statement.Statements
import net.strokkur.jap.code.type.CodeClassType
import net.strokkur.jap.code.type.CodePrimitiveType
import net.strokkur.jap.code.type.CodeType
import net.strokkur.jap.code.type.CodeTypes
import net.strokkur.jap.code.type.preset.JavaTypes
import net.strokkur.jap.code.util.Modifiers
import net.strokkur.jap.code.util.StyleConfig
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
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

  @get:OutputFile
  abstract val apiJar: RegularFileProperty

  @TaskAction
  fun run() {
    val apiJar = apiJar.get().asFile

    val url = URI(url.get()).toURL()
    url.openStream().use { input ->
      apiJar.outputStream().use { output ->
        input.copyTo(output)
      }
    }

    val createdClass = createClass(apiJar.toPath())
    val javaSource = CodeGenUtil.createJavaFile(createdClass)
    println(javaSource)
  }

  private fun createClass(path: Path): CodeClass {
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

private enum class ConverterTypes(val fqn: String) : ConvertToClassType {
  AUTO_SERVICE("com.google.auto.service.AutoService"),

  BRIGADIER_ARGUMENT_CONVERTER("net.strokkur.commands.internal.arguments.BrigadierArgumentConverter"),
  BRIGADIER_ARGUMENT_TYPE("net.strokkur.commands.internal.arguments.BrigadierArgumentType"),

  CODE_CLASS_TYPE("net.strokkur.jap.code.type.CodeClassType"),
  CODE_TYPE("net.strokkur.jap.code.type.CodeType"),
  CODE_TYPES("net.strokkur.jap.code.type.CodeTypes"),
  JAVA_TYPES("net.strokkur.jap.code.type.preset.JavaTypes"),
  EXPRESSIONS("net.strokkur.jap.code.expression.Expressions"),

  ARGUMENT_TYPES("io.papermc.paper.command.brigadier.argument.ArgumentTypes"),
  REGISTRY_KEY("io.papermc.paper.registry.RegistryKey"),
  REGISTRY_ARGUMENT_EXTRACTOR("io.papermc.paper.command.brigadier.argument.RegistryArgumentExtractor")
  ;

  override fun toClassType(): CodeClassType? {
    return CodeTypes.ofClass(fqn)
  }
}

private class ArgumentTypesIterator(path: Path) : Iterable<ArgumentType> {
  val argumentTypes: List<ArgumentType>

  init {
    val ctx = HypoContext.builder()
      .withProvider(AsmClassDataProvider.of(ClassProviderRoot.fromJar(path)))
      .withContextProvider(AsmClassDataProvider.of(ClassProviderRoot.ofJdk()))
      .build()

    val typeArgument = TypePattern.capture(TypePatterns.isClass())
    val pattern = TypePatterns.isClassNamed("com/mojang/brigadier/arguments/ArgumentType")
      .and(TypePatterns.hasTypeArguments(typeArgument));

    val data = ctx.provider.findClass("io/papermc/paper/command/brigadier/argument/ArgumentTypes")!!
    argumentTypes = data.methods().stream()
      .filter { !it.name().contains("resource") }
      .filter { it.signature()?.let { pattern.match(it.returnType).matches() } ?: false }
      .map { data ->
        val returnType = (typeArgument.getOrNull(pattern.match(data.signature()!!.returnType)) as ClassType).name;
        return@map ArgumentType(
          data.name(),
          CodeTypes.ofClass(returnType.replace('/', '.')),
          data.params()
            .map { toCodeType(it) }
            .toTypedArray()
        )
      }
      .toList()

    ctx.close()
  }

  override fun iterator(): Iterator<ArgumentType> {
    return argumentTypes.iterator()
  }
}

private class ArgumentType(
  val methodName: String,
  val returnType: CodeClassType,
  val args: Array<CodeType>
) {
  override fun toString(): String {
    val argsString = args
      .map { it.simpleName() }
      .joinToString(", ")
    return "${returnType.simpleName} ${methodName}(${argsString})"
  }
}

private class RegistryKeyTypeIterator(path: Path) : Iterable<RegistryKeyType> {
  val types: List<RegistryKeyType>

  init {
    val typeArgument = TypePattern.capture(TypePatterns.isClass())
    val pattern = TypePatterns.isClassNamed("io/papermc/paper/registry/RegistryKey")
      .and(TypePatterns.hasTypeArguments(typeArgument));

    types = HypoAsm.use<List<RegistryKeyType>, Exception>(path) { ctx ->
      val data = ctx.findClass("io/papermc/paper/registry/RegistryKey")!!
      return@use data.fields()
        .map {
          RegistryKeyType(
            toCodeType(typeArgument[pattern.match(it.signature()!!)] as TypeSignature) as CodeClassType,
            it.name()
          )
        }
        .toList()
    }
  }

  override fun iterator(): Iterator<RegistryKeyType> {
    return types.iterator()
  }
}

private class RegistryKeyType(
  val type: CodeClassType,
  val name: String
) {
  override fun toString(): String {
    return "${type.simpleName()} ${name}"
  }
}

fun toCodeType(signature: TypeSignature): CodeType {
  return when (signature) {
    PrimitiveType.CHAR -> CodePrimitiveType.CHAR
    PrimitiveType.BYTE -> CodePrimitiveType.BYTE
    PrimitiveType.SHORT -> CodePrimitiveType.SHORT
    PrimitiveType.INT -> CodePrimitiveType.INT
    PrimitiveType.LONG -> CodePrimitiveType.LONG
    PrimitiveType.FLOAT -> CodePrimitiveType.FLOAT
    PrimitiveType.DOUBLE -> CodePrimitiveType.DOUBLE
    PrimitiveType.BOOLEAN -> CodePrimitiveType.BOOL
    VoidType.INSTANCE -> CodePrimitiveType.VOID
    is ArrayTypeSignature -> toCodeType(signature.baseType).toArray()
    is ClassTypeSignature -> CodeTypes.ofClassTyped(
      signature.asReadable(), *signature.typeArguments
        .map { toGenericType(it) }
        .toTypedArray())

    is TypeVariable -> CodeTypes.generic(signature.name)
    is TypeVariable.Unbound -> CodeTypes.generic(signature.name)
  }
}

fun toCodeType(descriptor: TypeDescriptor): CodeType {
  return when (descriptor) {
    PrimitiveType.CHAR -> CodePrimitiveType.CHAR
    PrimitiveType.BYTE -> CodePrimitiveType.BYTE
    PrimitiveType.SHORT -> CodePrimitiveType.SHORT
    PrimitiveType.INT -> CodePrimitiveType.INT
    PrimitiveType.LONG -> CodePrimitiveType.LONG
    PrimitiveType.FLOAT -> CodePrimitiveType.FLOAT
    PrimitiveType.DOUBLE -> CodePrimitiveType.DOUBLE
    PrimitiveType.BOOLEAN -> CodePrimitiveType.BOOL
    VoidType.INSTANCE -> CodePrimitiveType.VOID
    is ArrayTypeDescriptor -> toCodeType(descriptor.baseType).toArray()
    is ClassTypeDescriptor -> CodeTypes.ofClass(descriptor.asReadable())
  }
}

fun toGenericType(arg: TypeArgument): ConvertToGenericType {
  return when (arg) {
    WildcardArgument.INSTANCE -> CodeTypes.genericWildcard()
    is ClassTypeSignature -> CodeTypes.ofClass(arg.asReadable())
    else -> error("Unhandled type argument type: ${arg.javaClass}")
  }
}
