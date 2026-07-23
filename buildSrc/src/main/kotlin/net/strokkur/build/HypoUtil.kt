package net.strokkur.build

import dev.denwav.hypo.types.PrimitiveType
import dev.denwav.hypo.types.VoidType
import dev.denwav.hypo.types.desc.ArrayTypeDescriptor
import dev.denwav.hypo.types.desc.ClassTypeDescriptor
import dev.denwav.hypo.types.desc.TypeDescriptor
import dev.denwav.hypo.types.sig.ArrayTypeSignature
import dev.denwav.hypo.types.sig.ClassTypeSignature
import dev.denwav.hypo.types.sig.TypeSignature
import dev.denwav.hypo.types.sig.param.TypeArgument
import dev.denwav.hypo.types.sig.param.TypeVariable
import dev.denwav.hypo.types.sig.param.WildcardArgument
import net.strokkur.jap.code.convert.ConvertToGenericType
import net.strokkur.jap.code.type.CodePrimitiveType
import net.strokkur.jap.code.type.CodeType
import net.strokkur.jap.code.type.CodeTypes

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
