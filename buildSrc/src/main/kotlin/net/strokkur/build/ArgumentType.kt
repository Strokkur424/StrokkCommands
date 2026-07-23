package net.strokkur.build

import dev.denwav.hypo.asm.AsmClassDataProvider
import dev.denwav.hypo.core.HypoContext
import dev.denwav.hypo.model.ClassProviderRoot
import dev.denwav.hypo.types.kind.ClassType
import dev.denwav.hypo.types.pattern.TypePattern
import dev.denwav.hypo.types.pattern.TypePatterns
import net.strokkur.jap.code.type.CodeClassType
import net.strokkur.jap.code.type.CodeType
import net.strokkur.jap.code.type.CodeTypes
import java.nio.file.Path

internal class ArgumentType(
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

internal class ArgumentTypesIterator(path: Path) : Iterable<ArgumentType> {
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
