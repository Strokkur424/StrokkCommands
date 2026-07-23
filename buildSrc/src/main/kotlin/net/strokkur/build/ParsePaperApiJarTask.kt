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
import net.strokkur.jap.code.convert.ConvertToGenericType
import net.strokkur.jap.code.type.CodeClassType
import net.strokkur.jap.code.type.CodePrimitiveType
import net.strokkur.jap.code.type.CodeType
import net.strokkur.jap.code.type.CodeTypes
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

    for (argumentType in ArgumentTypesIterator(apiJar.toPath())) {
      println("ArgumentType: " + argumentType)
    }
    println()
    for (registryKeyType in RegistryKeyTypeIterator(apiJar.toPath())) {
      println("RegistryKeyType: " + registryKeyType)
    }
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
