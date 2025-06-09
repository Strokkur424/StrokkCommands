package net.strokkur.commands.annotations.arguments;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface LongArg {
    long max() default Long.MAX_VALUE;
    long min() default Long.MIN_VALUE;
}
