package net.strokkur.commands.annotations.arguments;

public @interface FinePosArg {

    /**
     * Whether to center integer to .5
     */
    boolean value() default false;
}
