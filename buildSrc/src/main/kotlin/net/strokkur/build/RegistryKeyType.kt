package net.strokkur.build

import dev.denwav.hypo.asm.HypoAsm
import dev.denwav.hypo.types.pattern.TypePattern
import dev.denwav.hypo.types.pattern.TypePatterns
import dev.denwav.hypo.types.sig.TypeSignature
import net.strokkur.jap.code.type.CodeClassType
import java.nio.file.Path

internal class RegistryKeyType(
  val type: CodeClassType,
  val name: String
) {
  override fun toString(): String {
    return "${type.simpleName()} ${name}"
  }
}

internal class RegistryKeyTypeIterator(path: Path) : Iterable<RegistryKeyType> {
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
