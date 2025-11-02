package net.strokkur.commands.modded.arguments;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Declares that an `int` parameter should be interpreted as a slot argument.
///
/// Example usage:
/// ```java
/// @Executes
/// void executes(S source, @SlotArg int slot);
/// ```
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface SlotArg {
}
