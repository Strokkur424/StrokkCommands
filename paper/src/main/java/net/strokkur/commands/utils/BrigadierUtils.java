package net.strokkur.commands.utils;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.strokkur.commands.annotations.arguments.FloatArg;
import net.strokkur.commands.annotations.arguments.IntArg;
import net.strokkur.commands.annotations.arguments.StringArg;
import net.strokkur.commands.exceptions.UnknownArgumentException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;

import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.Supplier;

@NullUnmarked
public abstract class BrigadierUtils {

    private static final SetMap<String, Function<VariableElement, ArgumentType<?>>> CONVERSION_MAP = new SetHashMap<>();

    static {
        CONVERSION_MAP.putFor(p -> annotatedOr(p, StringArg.class,
            a -> switch (a.value()) {
                case WORD -> StringArgumentType.word();
                case STRING -> StringArgumentType.string();
                case GREEDY -> StringArgumentType.greedyString();
            },
            StringArgumentType::word
        ), "java.lang.String");

        CONVERSION_MAP.putFor(p -> annotatedOr(p, FloatArg.class,
            a -> FloatArgumentType.floatArg(a.min(), a.max()),
            FloatArgumentType::floatArg
        ), "float", "java.lang.Float");

        CONVERSION_MAP.putFor(p -> annotatedOr(p, IntArg.class,
            a -> IntegerArgumentType.integer(a.min(), a.max()),
            IntegerArgumentType::integer
        ), "int", "java.lang.Integer");
    }


    @NonNull
    public static ArgumentType<?> getAsArgumentType(@NonNull VariableElement parameter, @NonNull String type) throws UnknownArgumentException {
        ArgumentType<?> out = CONVERSION_MAP.get(type).apply(parameter);
        if (out != null) {
            return out;
        }

        throw new UnknownArgumentException(type);
    }

    private static <T extends Annotation> ArgumentType<?> annotatedOr(VariableElement parameter, Class<T> annotation, Function<T, ArgumentType<?>> function, Supplier<ArgumentType<?>> supplier) {
        T annotated = parameter.getAnnotation(annotation);
        if (annotated == null) {
            return supplier.get();
        }

        return function.apply(annotated);
    }
}
