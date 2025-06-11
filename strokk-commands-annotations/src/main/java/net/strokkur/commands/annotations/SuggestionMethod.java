package net.strokkur.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface SuggestionMethod {
    String method();

    Class<?> base() default Class.class;
    
    boolean reference() default true;
}
