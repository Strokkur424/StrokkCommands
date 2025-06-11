package net.strokkur.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface SuggestionField {
    String field();

    Class<?> base() default Class.class;
}
