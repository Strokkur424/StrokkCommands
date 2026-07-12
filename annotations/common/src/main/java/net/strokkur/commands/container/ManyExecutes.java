package net.strokkur.commands.container;

import net.strokkur.commands.Executes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
public @interface ManyExecutes {
  Executes[] value();
}
