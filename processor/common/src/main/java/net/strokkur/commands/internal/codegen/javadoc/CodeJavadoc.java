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
package net.strokkur.commands.internal.codegen.javadoc;

import net.strokkur.commands.internal.codegen.CodeClass;
import net.strokkur.commands.internal.codegen.CodeMethod;
import net.strokkur.commands.internal.codegen.visitor.JavadocVisitor;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface CodeJavadoc {
  void accept(JavadocVisitor visitor);

  //<editor-fold desc="Static methods">
  static CodeJavadoc combine(CodeJavadoc... children) {
    return new JavadocComponentList(List.of(children), false);
  }

  static CodeJavadoc combineLines(CodeJavadoc... children) {
    return new JavadocComponentList(List.of(children), true);
  }

  static CodeJavadoc text(String text) {
    return new PlainText(text);
  }

  static CodeJavadoc header(String text, int level) {
    return new Header(text, level);
  }

  static CodeJavadoc author(String author) {
    return new Meta("author", author);
  }

  static CodeJavadoc version(String version) {
    return new Meta("version", version);
  }

  static CodeJavadoc see(CodeMethod method, String description) {
    return see(method, description, false);
  }

  static CodeJavadoc see(CodeMethod method, String description, boolean localMethod) {
    return new MethodReferenceMeta("see", method, description, localMethod);
  }

  static CodeJavadoc throwsMeta(CodeClass exception, @Nullable String description) {
    return new ClassReferenceMeta("throws", exception, description);
  }

  static CodeJavadoc newline() {
    return new Newline();
  }

  /// Intended to be used inside [#combineLines(CodeJavadoc...)] for a true blank line.
  static CodeJavadoc emptyLine() {
    return visitor -> {
      // noop
    };
  }

  static CodeJavadoc linebreak() {
    return new Linebreak();
  }

  static CodeJavadoc inlineCode(String code) {
    return new InlineCode(code);
  }

  static CodeJavadoc codeBlock(String code) {
    return new CodeBlock(code);
  }

  static CodeJavadoc url(String text, String url) {
    return new Url(text, url);
  }

  static CodeJavadoc classReference(CodeClass ref) {
    return classReference(ref, null);
  }

  static CodeJavadoc classReference(CodeClass ref, @Nullable String description) {
    return new ClassReference(ref, description);
  }

  static CodeJavadoc methodReference(CodeMethod ref) {
    return methodReference(ref, null);
  }

  static CodeJavadoc methodReference(CodeMethod ref, @Nullable String description) {
    return methodReference(ref, description, false);
  }

  static CodeJavadoc methodReference(CodeMethod ref, boolean localMethod) {
    return methodReference(ref, null, localMethod);
  }

  static CodeJavadoc methodReference(CodeMethod ref, @Nullable String description, boolean localMethod) {
    return new MethodReference(ref, description, localMethod);
  }
  //</editor-fold>

  record JavadocComponentList(List<CodeJavadoc> components, boolean insertNewlines) implements CodeJavadoc {
    @Override
    public void accept(JavadocVisitor visitor) {
      for (CodeJavadoc component : components) {
        component.accept(visitor);
        if (insertNewlines) {
          new Newline().accept(visitor);
        }
      }
    }
  }

  record PlainText(String text) implements CodeJavadoc {
    @Override
    public void accept(JavadocVisitor visitor) {
      visitor.visit(this);
    }
  }

  record Meta(String descriptor, String value) implements CodeJavadoc {
    @Override
    public void accept(JavadocVisitor visitor) {
      visitor.visit(this);
    }
  }

  record ClassReferenceMeta(String descriptor, CodeClass codeClass, @Nullable String text) implements CodeJavadoc {
    @Override
    public void accept(JavadocVisitor visitor) {
      visitor.visit(this);
    }
  }

  record MethodReferenceMeta(String descriptor, CodeMethod codeMethod, @Nullable String text, boolean localMethod) implements CodeJavadoc {
    @Override
    public void accept(JavadocVisitor visitor) {
      visitor.visit(this);
    }
  }

  record Header(String text, int level) implements CodeJavadoc {
    @Override
    public void accept(JavadocVisitor visitor) {
      visitor.visit(this);
    }
  }

  record Newline() implements CodeJavadoc {
    @Override
    public void accept(JavadocVisitor visitor) {
      visitor.visit(this);
    }
  }

  record Linebreak() implements CodeJavadoc {
    @Override
    public void accept(JavadocVisitor visitor) {
      visitor.visit(this);
    }
  }

  record InlineCode(String code) implements CodeJavadoc {
    @Override
    public void accept(JavadocVisitor visitor) {
      visitor.visit(this);
    }
  }

  record CodeBlock(String code) implements CodeJavadoc {
    @Override
    public void accept(JavadocVisitor visitor) {
      visitor.visit(this);
    }
  }

  record Url(String text, String url) implements CodeJavadoc {
    @Override
    public void accept(JavadocVisitor visitor) {
      visitor.visit(this);
    }
  }

  record ClassReference(CodeClass codeClass, @Nullable String name) implements CodeJavadoc {
    @Override
    public void accept(JavadocVisitor visitor) {
      visitor.visit(this);
    }
  }

  record MethodReference(CodeMethod method, @Nullable String name, boolean localMethod) implements CodeJavadoc {
    @Override
    public void accept(JavadocVisitor visitor) {
      visitor.visit(this);
    }
  }
}
