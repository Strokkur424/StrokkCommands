package net.strokkur.commands.internal.codegen.builder;

import net.strokkur.commands.internal.codegen.CodeAnnotation;
import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.CodeField;
import net.strokkur.commands.internal.codegen.CodeType;
import net.strokkur.commands.internal.codegen.Modifiers;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FieldBuilder {
  private @Nullable String name = null;
  private @Nullable CodeType type = null;
  private @Nullable CodeExpression initialiser;
  private final Set<Modifiers> modifiers = new HashSet<>();
  private final List<CodeAnnotation> annotations = new ArrayList<>();

  FieldBuilder() {
    // package-private ctor
  }

  public FieldBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public FieldBuilder setType(CodeType type) {
    this.type = type;
    return this;
  }

  public FieldBuilder setInitialiser(@Nullable CodeExpression initialiser) {
    this.initialiser = initialiser;
    return this;
  }

  public FieldBuilder setModifiers(Set<Modifiers> modifiers) {
    this.modifiers.clear();
    this.modifiers.addAll(modifiers);
    return this;
  }

  public FieldBuilder addAnnotation(CodeAnnotation annotation) {
    this.annotations.add(annotation);
    return this;
  }

  public CodeField build() {
    Objects.requireNonNull(name);
    Objects.requireNonNull(type);
    return new CodeField(
        name, type, initialiser, EnumSet.copyOf(modifiers), List.copyOf(annotations)
    );
  }
}
