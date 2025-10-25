package net.strokkur.commands.meta;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// A meta annotation for the processor to print extended debug information.
///
/// Set the [#only()] property to limit generating and printing to a single source file only.
///
/// **This is a debug annotation which is not recommended in production code**.
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@ApiStatus.Internal
public @interface StrokkCommandsDebug {
  /// {@return the source class to focus on}
  Class<?> only() default Class.class;
}
