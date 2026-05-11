/*
 * StrokkCommands - A super simple annotation based zero-shade Paper command API library.
 * Copyright (C) 2025 Strokkur24
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see <https://www.gnu.org/licenses/>.
 */
package net.strokkur.commands.internal.codegen;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodePackageEqualityTests {

  @ParameterizedTest
  @CsvSource({
      "this.one.matches, this.one.matches",
      "this.also.matches, this.also.matches",
      "java.lang, java.lang",
      "io.papermc.paper.command, io.papermc.paper.command"
  })
  void ensureTwoPackagesEqual(String path1, String path2) {
    final CodePackage first = CodePackage.of(path1);
    final CodePackage second = CodePackage.of(path2);
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @ParameterizedTest
  @CsvSource({
      "this.one.matches, io.papermc.paper.command",
      "this.also.matches, this.one.matches",
      "java.lang, this.also.matches",
      "io.papermc.paper.command, java.lang"
  })
  void ensureTwoPackagesDoNotEqual(String path1, String path2) {
    final CodePackage first = CodePackage.of(path1);
    final CodePackage second = CodePackage.of(path2);
    assertNotEquals(first, second);
    assertNotEquals(first.hashCode(), second.hashCode());
  }

  @ParameterizedTest
  @CsvSource({
      "base.package, base.package",
      "base.package, java.lang",
      "different.one, different.one",
      "different.one, java.lang",
      "a.very.long.one.because.why.not, a.very.long.one.because.why.not",
      "a.very.long.one.because.why.not, java.lang"
  })
  void ensureRedundancyWorks(String path1, String path2) {
    final CodePackage first = CodePackage.of(path1);
    final CodePackage second = CodePackage.of(path2);
    assertTrue(CodePackage.isRedundantImport(first, second));
  }

  @ParameterizedTest
  @CsvSource({
      "base.package, base.package.subpath",
      "base.package.subpath, base.package",
      "hey.now, java.util",
      "hey.now, java.lang.subpath",
  })
  void ensureRedundancyWorks2(String path1, String path2) {
    final CodePackage first = CodePackage.of(path1);
    final CodePackage second = CodePackage.of(path2);
    assertFalse(CodePackage.isRedundantImport(first, second));
  }
}
