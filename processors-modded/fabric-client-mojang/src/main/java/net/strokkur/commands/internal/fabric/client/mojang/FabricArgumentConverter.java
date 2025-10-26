package net.strokkur.commands.internal.fabric.client.mojang;

import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.BrigadierArgumentType;
import net.strokkur.commands.internal.exceptions.ConversionException;
import net.strokkur.commands.internal.util.MessagerWrapper;
import org.jspecify.annotations.Nullable;

final class FabricArgumentConverter extends BrigadierArgumentConverter {
  public FabricArgumentConverter(final MessagerWrapper messagerWrapper) {
    super(messagerWrapper);
  }

  @Override
  protected @Nullable BrigadierArgumentType handleCustomArgumentAnnotations(final String argumentName, final String type, final SourceVariable parameter) throws ConversionException {
    return null;
  }
}
