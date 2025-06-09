package net.strokkur.commands.annotations.arguments;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface DoubleArg {
    double min() default Double.MIN_VALUE;
    double max() default Double.MAX_VALUE;
}
