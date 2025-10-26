package net.strokkur.commands.modded;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Declares a clientside command. This is the annotation the client annotation-processor listens to when generating commands.
///
/// This annotation can only be used on **top-level classes**, whose visibility is at least package-private.
/// You can find documentation about declaring commands with StrokkCommands in [the official documentation](https://commands.strokkur.net).
///
/// Requires entering a command name.
///
/// Example usage:
/// ```java
/// @ClientCommand("mycommand")
/// class MyCommand {
///   /* command definition here */
/// }
/// ```
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ClientCommand {
  /// {@return the name of the command}
  String value();
}
