package net.strokkur.commands.internal.codegen;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Language(
    value = "JAVA",
    prefix = "class Wrapper__ { Object __method() { if (true) {",
    suffix = "} return null; } }")
@Retention(RetentionPolicy.SOURCE)
public @interface JavaStatements {
}
