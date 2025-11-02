package net.strokkur.commands.modded.arguments;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Declares that a `net.minecraft.network.chat.Component` parameter should be interpreted as a message argument.
///
/// Example usage:
/// ```java
/// @Executes
/// void executes(S source, @MessageArg Component message);
/// ```
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface MessageArg {
}
