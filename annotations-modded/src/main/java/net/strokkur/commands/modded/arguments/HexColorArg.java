package net.strokkur.commands.modded.arguments;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Declares that an `int` parameter should be interpreted as a hex color argument.
///
/// Example usage:
/// ```java
/// @Executes
/// void executes(S source, @HexColorArg int color);
/// ```
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface HexColorArg {
}
