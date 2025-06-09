package net.strokkur.commands.internal;

import net.strokkur.commands.annotations.arguments.DoubleArg;
import net.strokkur.commands.annotations.arguments.FinePosArg;
import net.strokkur.commands.annotations.arguments.FloatArg;
import net.strokkur.commands.annotations.arguments.IntArg;
import net.strokkur.commands.annotations.arguments.LongArg;
import net.strokkur.commands.annotations.arguments.StringArg;
import net.strokkur.commands.objects.arguments.StringArgType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

@NullUnmarked
abstract class BrigadierArgumentConversion {

    private static final Map<String, BiFunction<VariableElement, String, BrigadierArgumentType>> CONVERSION_MAP = new HashMap<>();

    static {
        // Primitive argument types
        putFor((p, name) -> BrigadierArgumentType.of(
            "BoolArgumentType.bool()",
            "BoolArgumentType.getBool(ctx, \"%s\")".formatted(name),
            "com.mojang.brigadier.arguments.BoolArgumentType"
        ), "boolean", "java.lang.Boolean");

        putFor((p, name) -> annotatedOr(p, IntArg.class,
            a -> "IntegerArgumentType.integer(%s, %s)".formatted(a.min(), a.max()),
            "IntegerArgumentType.integer()", "IntegerArgumentType.getInteger(ctx, \"%s\")".formatted(name),
            "com.mojang.brigadier.arguments.IntegerArgumentType"
        ), "int", "java.lang.Integer");

        putFor((p, name) -> annotatedOr(p, LongArg.class,
            a -> "LongArgumentType.longArg(%s, %s)".formatted(a.min(), a.max()),
            "LongArgumentType.longArg()", "LongArgumentType.getLong(ctx, \"%s\")".formatted(name),
            "com.mojang.brigadier.arguments.LongArgumentType"
        ), "long", "java.lang.Long");

        putFor((p, name) -> annotatedOr(p, FloatArg.class,
            a -> "FloatArgumentType.floatArg(%s, %s)".formatted(a.min(), a.max()),
            "FloatArgumentType.floatArg()",
            "FloatArgumentType.getFloat(ctx, \"%s\")".formatted(name),
            "com.mojang.brigadier.arguments.FloatArgumentType"
        ), "float", "java.lang.Float");

        putFor((p, name) -> annotatedOr(p, DoubleArg.class,
            a -> "DoubleArgumentType.doubleArg(%s, %s)".formatted(a.min(), a.max()),
            "DoubleArgumentType.doubleArg()", "DoubleArgumentType.getDouble(ctx, \"%s\")".formatted(name),
            "com.mojang.brigadier.arguments.DoubleArgumentType"
        ), "double", "java.lang.Double");

        putFor((p, name) -> annotatedOr(p, StringArg.class,
            a -> "StringArgumentType.%s()".formatted(a.value().getBrigadierType()),
            "StringArgumentType.%s()".formatted(StringArgType.WORD.getBrigadierType()),
            "StringArgumentType.getString(ctx, \"%s\")".formatted(name),
            "com.mojang.brigadier.arguments.StringArgumentType"
        ), "java.lang.String");

        // Location arguments
        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.blockPosition()",
            "ctx.getArgument(\"%s\", BlockPositionResolver.class).resolve(ctx.getSource())".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver"
            )
        ), "io.papermc.paper.math.BlockPosition");

        putFor((p, name) -> annotatedOr(p, FinePosArg.class,
            a -> "ArgumentTypes.finePosition(%s)".formatted(a.value()),
            "ArgumentTypes.finePosition()",
            "ctx.getArgument(\"%s\", FinePositionResolver.class).resolve(ctx.getSource())".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver"
            )
        ), "io.papermc.paper.math.FinePosition");

        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.world()",
            "ctx.getArgument(\"%s\", World.class)".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "org.bukkit.World"
            )
        ), "org.bukkit.World");

        //<editor-fold desc="Entity and Player arguments">
        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.entity()",
            "ctx.getArgument(\"%s\", EntitySelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst()".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver"
            )
        ), "org.bukkit.entity.Entity");

        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.entities()",
            "ctx.getArgument(\"%s\", EntitySelectorArgumentResolver.class).resolve(ctx.getSource())".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver"
            )
        ), "java.util.List<org.bukkit.entity.Entity>", "java.util.Collection<org.bukkit.entity.Entity>");

        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.entities()",
            "ctx.getArgument(\"%s\", EntitySelectorArgumentResolver.class).resolve(ctx.getSource()).toArray(Entity[]::new)".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver",
                "org.bukkit.entity.Entity"
            )
        ), "org.bukkit.entity.Entity[]");

        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.player()",
            "ctx.getArgument(\"%s\", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst()".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver"
            )
        ), "org.bukkit.entity.Player");

        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.players()",
            "ctx.getArgument(\"%s\", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource())".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver"
            )
        ), "java.util.List<org.bukkit.entity.Player>", "java.util.Collection<org.bukkit.entity.Player>");

        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.players()",
            "ctx.getArgument(\"%s\", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).toArray(Player[]::new)".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver",
                "org.bukkit.entity.Player"
            )
        ), "org.bukkit.entity.Player[]");

        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.playerProfiles()",
            "ctx.getArgument(\"%s\", PlayerProfileListResolver.class).resolve(ctx.getSource()).getFirst()".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver"
            )
        ), "com.destroystokyo.paper.profile.PlayerProfile");

        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.playerProfiles()",
            "ctx.getArgument(\"%s\", PlayerProfileListResolver.class).resolve(ctx.getSource())".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver"
            )
        ), "java.util.Collection<com.destroystokyo.paper.profile.PlayerProfile>");

        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.playerProfiles()",
            "ctx.getArgument(\"%s\", PlayerProfileListResolver.class).resolve(ctx.getSource()).stream().toList()".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver"
            )
        ), "java.util.Collection<com.destroystokyo.paper.profile.PlayerProfile>", "java.util.List<com.destroystokyo.paper.profile.PlayerProfile>");

        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.playerProfiles()",
            "ctx.getArgument(\"%s\", PlayerProfileListResolver.class).resolve(ctx.getSource()).toArray(PlayerProfile[]::new)".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver",
                "com.destroystokyo.paper.profile.PlayerProfile"
            )
        ), "com.destroystokyo.paper.profile.PlayerProfile[]");
        //</editor-fold>
    }

    private static void putFor(BiFunction<VariableElement, String, BrigadierArgumentType> value, String... keys) {
        for (String key : keys) {
            CONVERSION_MAP.put(key, value);
        }
    }

    @Nullable
    public static BrigadierArgumentType getAsArgumentType(@NonNull VariableElement parameter, @NonNull String argumentName, @NonNull String type) {
        if (!CONVERSION_MAP.containsKey(type)) {
            StrokkCommandsPreprocessor.getMessenger().ifPresent(messager -> messager.printError("Cannot find Brigadier equivalent for argument of type " + type + ".", parameter));
            return null;
        }

        BrigadierArgumentType out = CONVERSION_MAP.get(type).apply(parameter, argumentName);
        if (out != null) {
            return out;
        }

        StrokkCommandsPreprocessor.getMessenger().ifPresent(messager -> messager.printError("An unexpected error occurred whilst converting type " + type + " to Brigadier equivalent.", parameter));
        return null;
    }

    private static <T extends Annotation> BrigadierArgumentType annotatedOr(VariableElement parameter, Class<T> annotation, Function<T, String> withAnnotation,
                                                                            String withoutAnnotation, String retrieval) {
        return annotatedOr(parameter, annotation, withAnnotation, withoutAnnotation, retrieval, Set.of());
    }

    private static <T extends Annotation> BrigadierArgumentType annotatedOr(VariableElement parameter, Class<T> annotation, Function<T, String> withAnnotation,
                                                                            String withoutAnnotation, String retrieval, String singleImport) {
        return annotatedOr(parameter, annotation, withAnnotation, withoutAnnotation, retrieval, Set.of(singleImport));
    }

    private static <T extends Annotation> BrigadierArgumentType annotatedOr(VariableElement parameter, Class<T> annotation, Function<T, String> withAnnotation,
                                                                            String withoutAnnotation, String retrieval, Set<String> imports) {
        T annotated = parameter.getAnnotation(annotation);
        if (annotated == null) {
            return BrigadierArgumentType.of(withoutAnnotation, retrieval, imports);
        }

        return BrigadierArgumentType.of(withAnnotation.apply(annotated), retrieval, imports);
    }
}
