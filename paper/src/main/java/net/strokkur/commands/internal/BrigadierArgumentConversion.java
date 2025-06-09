package net.strokkur.commands.internal;

import net.strokkur.commands.annotations.arguments.FloatArg;
import net.strokkur.commands.annotations.arguments.IntArg;
import net.strokkur.commands.annotations.arguments.StringArg;
import net.strokkur.commands.objects.arguments.StringArgType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;
import java.util.function.BiFunction;
import java.util.function.Function;

@NullUnmarked
abstract class BrigadierArgumentConversion {

    private static final SetMap<String, BiFunction<VariableElement, String, BrigadierArgumentType>> CONVERSION_MAP = new SetHashMap<>();

    static {
        CONVERSION_MAP.putFor((p, name) -> annotatedOr(p, StringArg.class,
            a -> "StringArgumentType.%s()".formatted(a.value().getBrigadierType()),
            "StringArgumentType.%s()".formatted(StringArgType.WORD.getBrigadierType()),
            "StringArgumentType.getString(ctx, \"%s\")".formatted(name),
            "com.mojang.brigadier.arguments.StringArgumentType"
        ), "java.lang.String");

        CONVERSION_MAP.putFor((p, name) -> annotatedOr(p, FloatArg.class,
            a -> "FloatArgumentType.floatArg(%s,%s)".formatted(a.min(), a.max()),
            "FloatArgumentType.floatArg()",
            "FloatArgumentType.getFloat(ctx, \"%s\")".formatted(name),
            "com.mojang.brigadier.arguments.FloatArgumentType"
        ), "float", "java.lang.Float");

        CONVERSION_MAP.putFor((p, name) -> annotatedOr(p, IntArg.class,
            a -> "IntegerArgumentType.integer(%s,%s)".formatted(a.min(), a.max()),
            "IntegerArgumentType.integer()", "IntegerArgumentType.getInteger(ctx, \"%s\")".formatted(name),
            "com.mojang.brigadier.arguments.IntegerArgumentType"
        ), "int", "java.lang.Integer");
    }


    @NonNull
    public static BrigadierArgumentType getAsArgumentType(@NonNull VariableElement parameter, @NonNull String argumentName, @NonNull String type) throws UnknownArgumentException {
        BrigadierArgumentType out = CONVERSION_MAP.get(type).apply(parameter, argumentName);
        if (out != null) {
            return out;
        }

        throw new UnknownArgumentException(type);
    }

    private static <T extends Annotation> BrigadierArgumentType annotatedOr(VariableElement parameter, Class<T> annotation, Function<T, String> withAnnotation,
                                                                            String withoutAnnotation, String retrieval) {
        return annotatedOr(parameter, annotation, withAnnotation, withoutAnnotation, retrieval, null);
    }

    private static <T extends Annotation> BrigadierArgumentType annotatedOr(VariableElement parameter, Class<T> annotation, Function<T, String> withAnnotation,
                                                                            String withoutAnnotation, String retrieval, @Nullable String importString) {
        T annotated = parameter.getAnnotation(annotation);
        if (annotated == null) {
            return BrigadierArgumentType.of(withoutAnnotation, retrieval, importString);
        }

        return BrigadierArgumentType.of(withAnnotation.apply(annotated), retrieval, importString);
    }
}
