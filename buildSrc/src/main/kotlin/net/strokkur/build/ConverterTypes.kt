package net.strokkur.build

import net.strokkur.jap.code.convert.ConvertToClassType
import net.strokkur.jap.code.type.CodeClassType
import net.strokkur.jap.code.type.CodeTypes

internal enum class ConverterTypes(val fqn: String) : ConvertToClassType {
  AUTO_SERVICE("com.google.auto.service.AutoService"),

  UNIQUE_PAPER_BRIGADIER_ARGUMENT_CONVERTER("net.strokkur.commands.internal.paper.PaperUniqueBrigadierArgumentConverter"),
  BRIGADIER_ARGUMENT_CONVERTER("net.strokkur.commands.internal.arguments.BrigadierArgumentConverter"),
  BRIGADIER_ARGUMENT_TYPE("net.strokkur.commands.internal.arguments.BrigadierArgumentType"),

  CODE_CLASS_TYPE("net.strokkur.jap.code.type.CodeClassType"),
  CODE_TYPE("net.strokkur.jap.code.type.CodeType"),
  CODE_TYPES("net.strokkur.jap.code.type.CodeTypes"),
  JAVA_TYPES("net.strokkur.jap.code.type.preset.JavaTypes"),
  EXPRESSIONS("net.strokkur.jap.code.expression.Expressions"),

  ARGUMENT_TYPES("io.papermc.paper.command.brigadier.argument.ArgumentTypes"),
  REGISTRY_KEY("io.papermc.paper.registry.RegistryKey"),
  REGISTRY_ARGUMENT_EXTRACTOR("io.papermc.paper.command.brigadier.argument.RegistryArgumentExtractor");

  override fun toClassType(): CodeClassType? {
    return CodeTypes.ofClass(fqn)
  }
}
