package net.strokkur.commands.annotations.arguments;

import net.strokkur.commands.objects.arguments.StringArgType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface StringArg {
    StringArgType value() default StringArgType.WORD;
}
