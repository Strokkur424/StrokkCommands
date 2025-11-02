package net.strokkur.commands.modded.arguments;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Declares that an `int` parameter should be interpreted as a time argument.
///
/// The optional parameter declares the minimum number of time that needs to be entered.
///
/// Example usage:
/// ```java
/// @Executes
/// void executes(S source, @TimeArg int time);
/// ```
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface TimeArg {
  /// {@return the minimum number of time (in ticks) that needs to be entered}
  int value() default 0;
}
