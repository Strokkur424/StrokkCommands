package net.strokkur.build

import dev.denwav.hypo.asm.HypoAsm
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
import java.nio.file.Path

internal class ResolverType(
  val resolvedType: CodeClassType,
  val rawResolvedType: CodeClassType
)

internal fun resolverType(path: Path, className: String): ResolverType? {
  val typeArgument = TypePattern.capture(TypePatterns.isClass())
  val pattern = TypePatterns.isClassNamed {
    it == "io/papermc/paper/command/brigadier/argument/resolvers/ArgumentResolver"
      || it == "io/papermc/paper/command/brigadier/argument/resolvers/selector/SelectorArgumentResolver"
  }.and(TypePatterns.hasTypeArguments(typeArgument));

  return HypoAsm.use<ResolverType?, Exception>(path) { ctx ->
    ctx.findClass(className)
      ?.signature()
      ?.superInterfaces
      ?.filter { pattern.match(it).matches() }
      ?.map {
        val firstMatch = typeArgument.getOrNull(pattern.match(it))!!
        val rawResolvedType = CodeTypes.ofClass(
          (typeArgument.getOrNull(pattern.match(it)) as ClassType).name
            .replace('/', '.')
        )

        val pat = TypePatterns.hasTypeArguments(typeArgument)
        if (pat.match(firstMatch).matches()) {
          val resolvedType = (typeArgument.getOrNull(pat.match(firstMatch)) as ClassType).name;
          return@map ResolverType(
            CodeTypes.ofClass(resolvedType.replace('/', '.')),
            rawResolvedType
          )
        }

        return@map ResolverType(rawResolvedType, rawResolvedType)
      }?.getOrNull(0)
  }
}

internal fun rangeType(path: Path, className: String): CodeClassType? {
  val typeArgument = TypePattern.capture(TypePatterns.isClass())
  val pattern = TypePatterns.isClassNamed("io/papermc/paper/command/brigadier/argument/range/RangeProvider")
    .and(TypePatterns.hasTypeArguments(typeArgument));

  return HypoAsm.use<CodeClassType?, Exception>(path) { ctx ->
    ctx.findClass(className)
      ?.signature()
      ?.superInterfaces
      ?.filter { pattern.match(it).matches() }
      ?.map {
        val firstMatch = typeArgument.getOrNull(pattern.match(it))!!
        val resolvedType = CodeTypes.ofClass(
          (typeArgument.getOrNull(pattern.match(it)) as ClassType).name
            .replace('/', '.')
        )

        return@map resolvedType
      }?.getOrNull(0)
  }
}

internal fun toCodeType(signature: TypeSignature): CodeType {
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

internal fun toCodeType(descriptor: TypeDescriptor): CodeType {
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

internal fun toGenericType(arg: TypeArgument): ConvertToGenericType {
  return when (arg) {
    WildcardArgument.INSTANCE -> CodeTypes.genericWildcard()
    is ClassTypeSignature -> CodeTypes.ofClass(arg.asReadable())
    else -> error("Unhandled type argument type: ${arg.javaClass}")
  }
}
