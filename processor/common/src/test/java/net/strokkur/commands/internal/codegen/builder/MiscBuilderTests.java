package net.strokkur.commands.internal.codegen.builder;

import net.strokkur.commands.internal.codegen.CodePackage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MiscBuilderTests {

  @Test
  void ensureClassBuilderOverloadsDoTheSame() {
    Assertions.assertEquals(
        Builders.classBuilder("com.example.ClassName"),
        Builders.classBuilder("ClassName", CodePackage.of("com.example"))
    );
  }
}
