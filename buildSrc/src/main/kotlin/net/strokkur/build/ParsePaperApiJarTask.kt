package net.strokkur.build

import dev.denwav.hypo.asm.AsmClassDataProvider
import dev.denwav.hypo.core.HypoContext
import dev.denwav.hypo.model.ClassProviderRoot
import dev.denwav.hypo.model.data.ClassData
import dev.denwav.hypo.model.data.MethodData
import dev.denwav.hypo.types.desc.ClassTypeDescriptor
import dev.denwav.hypo.types.sig.ClassTypeSignature
import dev.denwav.hypo.types.sig.Signature
import dev.denwav.hypo.types.sig.param.TypeArgument
import net.strokkur.jap.code.convert.ConvertToGenericType
import net.strokkur.jap.code.type.CodeClassType
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
import java.util.function.Predicate

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

    println("hi")
    for (argumentType in ArgumentTypesIterator(apiJar.toPath(), { true })) {
      println("ArgumentType: " + argumentType)
    }
    println("two")
  }
}

private class ArgumentTypesIterator(path: Path, methodPredicate: Predicate<MethodData>) : Iterable<ArgumentType> {
  val argumentTypes: List<ArgumentType>

  init {
    val ctx = HypoContext.builder()
      .withProvider(AsmClassDataProvider.of(ClassProviderRoot.fromJar(path)))
      .withContextProvider(AsmClassDataProvider.of(ClassProviderRoot.ofJdk()))
      .build()
//    HydrationManager.createDefault().hydrate(ctx)

    val data = ctx.provider.findClass("io/papermc/paper/command/brigadier/argument/ArgumentTypes")!!
    argumentTypes = data.methods().stream()
      .filter(methodPredicate)
      .filter { (it.returnType() as? ClassTypeDescriptor)?.name == "com.mojang.brigadier.arguments.ArgumentType" }
      .map { data ->
        println(data)
        return@map ArgumentType(
          data.name(),
          typeToCode(data.returnType().asSignature()),
          arrayOf()
//          superTypesOfType((data.returnType().asSignature() as ClassTypeSignature).typeArguments[0].)
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
  val extraTypes: Array<CodeClassType>
) {
  override fun toString(): String {
    return "(${methodName} // ${returnType.fullyQualifiedName()} // ${extraTypes.map { it.name() }})"
  }
}

fun returnType(method: MethodData) {
  println(method.name())
}

fun typeToCode(sig: Signature): CodeClassType {
  if (sig is ClassTypeSignature) {
    val out: CodeClassType = CodeTypes.ofClass(sig.asReadable());
    if (sig.typeArguments.isNotEmpty()) {
      return out.typed(*typeArgsToCode(sig.typeArguments))
    }
    return out
  }

  error("Unknown signature type: " + sig.javaClass)
}

fun typeArgsToCode(args: List<TypeArgument>): Array<out ConvertToGenericType> {
  return args
    .map { arg -> typeToCode(arg as Signature) }
    .toTypedArray()
}

fun superTypesOfType(data: ClassData): Array<CodeClassType> {
  return data.signature()?.superInterfaces
    ?.filter {
      it.name !in listOf(
        "net.kyori.adventure.audience.ForwardingAudience"
      )
    }
    ?.map { typeToCode(it) }
    ?.toTypedArray() ?: arrayOf()
}
