package net.strokkur.commands.internal.intermediate.attributes;

import net.strokkur.commands.internal.util.Classes;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public interface DefaultExecutable extends Executable {
  Type defaultExecutableArgumentTypes();

  enum Type {
    NONE(null, Set.of()),
    ARRAY("ctx.getInput().split(\" \")", Set.of()),
    LIST("Collections.unmodifiableList(Arrays.asList(ctx.getInput().split(\" \")))", Set.of(Classes.COLLECTIONS, Classes.ARRAYS));

    private final @Nullable String getter;
    private final Set<String> imports;

    Type(@Nullable final String getter, final Set<String> imports) {
      this.getter = getter;
      this.imports = imports;
    }

    public @Nullable String getGetter() {
      return this.getter;
    }

    public Set<String> getImports() {
      return this.imports;
    }
  }
}
