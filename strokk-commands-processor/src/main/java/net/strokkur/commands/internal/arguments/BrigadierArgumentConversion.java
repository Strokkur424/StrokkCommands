package net.strokkur.commands.internal.arguments;

import net.strokkur.commands.StringArgType;
import net.strokkur.commands.annotations.arguments.CustomArg;
import net.strokkur.commands.annotations.arguments.DoubleArg;
import net.strokkur.commands.annotations.arguments.FinePosArg;
import net.strokkur.commands.annotations.arguments.FloatArg;
import net.strokkur.commands.annotations.arguments.IntArg;
import net.strokkur.commands.annotations.arguments.LongArg;
import net.strokkur.commands.annotations.arguments.StringArg;
import net.strokkur.commands.annotations.arguments.TimeArg;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.commands.internal.util.Utils;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static net.strokkur.commands.internal.arguments.BrigadierArgumentConversion.RegistryEntry.registryEntry;
import static net.strokkur.commands.internal.arguments.BrigadierArgumentConversion.SimpleEntry.simpleEntry;

public abstract class BrigadierArgumentConversion {

    //<editor-fold desc="Registry Entries">
    private static final List<RegistryEntry> REGISTRY_ENTRIES = List.of(
        registryEntry("Attribute", "org.bukkit.attribute.Attribute", "ATTRIBUTE"),
        registryEntry("PatternType", "org.bukkit.block.banner.PatternType", "BANNER_PATTERN"),
        registryEntry("Biome", "org.bukkit.block.Biome", "BIOME"),
        registryEntry("Cat.Type", "org.bukkit.entity.Cat", "org.bukkit.entity.Cat.Type", "CAT_VARIANT"),
        registryEntry("Chicken.Variant", "org.bukkit.entity.Chicken", "org.bukkit.entity.Chicken.Variant", "CHICKEN_VARIANT"),
        registryEntry("Cow.Variant", "org.bukkit.entity.Cow", "org.bukkit.entity.Cow.Variant", "COW_VARIANT"),
        registryEntry("DamageType", "org.bukkit.damage.DamageType", "DAMAGE_TYPE"),
        registryEntry("DataComponentType", "io.papermc.paper.datacomponent.DataComponentType", "DATA_COMPONENT_TYPE"),
        registryEntry("Enchantment", "org.bukkit.enchantments.Enchantment", "ENCHANTMENT"),
        registryEntry("EntityType", "org.bukkit.entity.EntityType", "ENTITY_TYPE"),
        registryEntry("Fluid", "org.bukkit.Fluid", "FLUID"),
        registryEntry("Frog.Variant", "org.bukkit.entity.Frog", "org.bukkit.entity.Frog.Variant", "FROG_VARIANT"),
        registryEntry("GameEvent", "org.bukkit.GameEvent", "GAME_EVENT"),
        registryEntry("ItemType", "org.bukkit.inventory.ItemType", "ITEM"),
        registryEntry("JukeboxSong", "org.bukkit.JukeboxSong", "JUKEBOX_SONG"),
        registryEntry("MapCursor.Type", "org.bukkit.map.MapCursor", "org.bukkit.map.MapCursor.Type", "MAP_DECORATION_TYPE"),
        registryEntry("MemoryKey", "org.bukkit.entity.memory.MemoryKey", "org.bukkit.entity.memory.MemoryKey<?>", "MEMORY_MODULE_TYPE"),
        registryEntry("MenuType", "org.bukkit.inventory.MenuType", "MENU"),
        registryEntry("PotionEffectType", "org.bukkit.potion.PotionEffectType", "MOB_EFFECT"),
        registryEntry("Art", "org.bukkit.Art", "PAINTING_VARIANT"),
        registryEntry("Particle", "org.bukkit.Particle", "PARTICLE_TYPE"),
        registryEntry("Pig.Variant", "org.bukkit.entity.Pig", "org.bukkit.entity.Pig.Variant", "PIG_VARIANT"),
        registryEntry("PotionType", "org.bukkit.potion.PotionType", "POTION"),
        registryEntry("Sound", "org.bukkit.Sound", "SOUND_EVENT"),
        registryEntry("Structure", "org.bukkit.structure.Structure", "STRUCTURE"),
        registryEntry("TrimMaterial", "org.bukkit.inventory.meta.trim.TrimMaterial", "TRIM_MATERIAL"),
        registryEntry("TrimPattern", "org.bukkit.inventory.meta.trim.TrimPattern", "TRIM_PATTERN"),
        registryEntry("Villager.Profession", "org.bukkit.entity.Villager", "org.bukkit.entity.Villager.Profession", "VILLAGER_PROFESSION"),
        registryEntry("Villager.Type", "org.bukkit.entity.Villager", "org.bukkit.entity.Villager.Type", "VILLAGER_TYPE"),
        registryEntry("Wolf.SoundVariant", "org.bukkit.entity.Wolf", "org.bukkit.entity.Wolf.SoundVariant", "WOLF_SOUND_VARIANT"),
        registryEntry("Wolf.Variant", "org.bukkit.entity.Wolf", "org.bukkit.entity.Wolf.Variant", "WOLF_VARIANT")
    );
    //</editor-fold>

    private static final Map<String, BiFunction<VariableElement, String, BrigadierArgumentType>> CONVERSION_MAP = new HashMap<>();

    static {
        //<editor-fold desc="Primitive argument types">
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
        //</editor-fold>

        //<editor-fold desc="Location arguments">
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
        //</editor-fold>

        //<editor-fold desc="Entity and player arguments">
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

        // All registry related arguments
        REGISTRY_ENTRIES.forEach(BrigadierArgumentConversion::addResourceAndResourceKeyArguments);

        // "Paper"-related arguments
        Stream.of(
            simpleEntry("blockState", "BlockState", "org.bukkit.block.BlockState"),
            simpleEntry("itemStack", "ItemStack", "org.bukkit.inventory.ItemStack"),
            simpleEntry("namespacedKey", "NamespacedKey", "org.bukkit.NamespacedKey"),
            simpleEntry("uuid", "UUID", "java.util.UUID"),
            simpleEntry("objectiveCriteria", "Criteria", "org.bukkit.scoreboard.Criteria")
        ).forEach(BrigadierArgumentConversion::putSimple);

        //<editor-fold desc=Predicate arguments">
        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.doubleRange()",
            "ctx.getArgument(\"%s\", DoubleRangeProvider.class)".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.range.DoubleRangeProvider"
            )
        ), "io.papermc.paper.command.brigadier.argument.range.DoubleRangeProvider", "io.papermc.paper.command.brigadier.argument.range.RangeProvider<java.lang.Double>");

        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.doubleRange()",
            "ctx.getArgument(\"%s\", DoubleRangeProvider.class).range()".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.range.DoubleRangeProvider"
            )
        ), "com.google.common.collect.Range<java.lang.Double>");

        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.doubleRange()",
            "ctx.getArgument(\"%s\", IntegerRangeProvider.class)".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.range.IntegerRangeProvider"
            )
        ), "io.papermc.paper.command.brigadier.argument.range.IntegerRangeProvider", "io.papermc.paper.command.brigadier.argument.range.RangeProvider<java.lang.Integer>");

        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.doubleRange()",
            "ctx.getArgument(\"%s\", IntegerRangeProvider.class).range()".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.range.IntegerRangeProvider"
            )
        ), "com.google.common.collect.Range<java.lang.Integer>");

        putSimple(simpleEntry("itemPredicate", "ItemStackPredicate", "io.papermc.paper.command.brigadier.argument.predicate.ItemStackPredicate"));
        //</editor-fold>

        //<editor-fold desc="Adventure arguments">
        Stream.of(
            simpleEntry("component", "Component", "net.kyori.adventure.text.Component"),
            simpleEntry("key", "Key", "net.kyori.adventure.key.Key"),
            simpleEntry("namedColor", "NamedTextColor", "net.kyori.adventure.text.format.NamedTextColor"),
            simpleEntry("style", "Style", "net.kyori.adventure.text.format.Style")
        ).forEach(BrigadierArgumentConversion::putSimple);

        CONVERSION_MAP.put("java.util.concurrent.CompletableFuture<net.kyori.adventure.chat.SignedMessage>", (p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.signedMessage()",
            "ctx.getArgument(\"%s\", SignedMessageResolver.class).resolveSignedMessage(\"%s\", ctx)".formatted(name, name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.SignedMessageResolver"
            )
        ));

        CONVERSION_MAP.put("io.papermc.paper.command.brigadier.argument.SignedMessageResolver", (p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.signedMessage()",
            "ctx.getArgument(\"%s\", SignedMessageResolver.class)".formatted(name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.command.brigadier.argument.SignedMessageResolver"
            )
        ));
        //</editor-fold>
    }

    private static void addResourceAndResourceKeyArguments(RegistryEntry entry) {
        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.resource(RegistryKey.%s)".formatted(entry.registryKeyName()),
            "ctx.getArgument(\"%s\", %s.class)".formatted(name, entry.type()),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.registry.RegistryKey",
                entry.typeImport()
            )
        ), entry.fullType());

        putFor((p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.resourceKey(RegistryKey.%s)".formatted(entry.registryKeyName()),
            "RegistryArgumentExtractor.getTypedKey(ctx, RegistryKey.%s, \"%s\")".formatted(entry.type(), name),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                "io.papermc.paper.registry.RegistryKey",
                "io.papermc.paper.command.brigadier.argument.RegistryArgumentExtractor",
                entry.typeImport()
            )
        ), "io.papermc.paper.registry.TypedKey<%s>".formatted(entry.fullType()));
    }

    private static void putFor(BiFunction<VariableElement, String, BrigadierArgumentType> value, String... keys) {
        for (String key : keys) {
            CONVERSION_MAP.put(key, value);
        }
    }

    @Nullable
    public static BrigadierArgumentType getAsArgumentType(VariableElement parameter, String argumentName, String type, MessagerWrapper messager) {
        CustomArg customArg = parameter.getAnnotation(CustomArg.class);
        if (customArg != null) {
            TypeMirror mirror = Utils.getAnnotationMirror(parameter, CustomArg.class, "value");
            if (mirror != null) {
                return new BrigadierArgumentType("new " + mirror + "()", "ctx.getArgument(\"" + argumentName + "\", " + type + ".class)", Set.of());
            } else {
                messager.errorElement("Invalid value for @CustomArg annotation.", parameter);
            }
        }

        if (!CONVERSION_MAP.containsKey(type)) {
            messager.errorElement("Cannot find Brigadier equivalent for argument of type {}.", parameter, type);
            return null;
        }

        // We *have* to give the time argument precedence, since its return type is also 'int'.        
        TimeArg timeArg = parameter.getAnnotation(TimeArg.class);
        if (timeArg != null) {
            if (!type.equals("int") && !type.equals("java.lang.Integer")) {
                messager.errorElement("An argument annotated with @TimeArg has to be of type 'int'", parameter);
                return null;
            }

            return BrigadierArgumentType.of(
                "ArgumentTypes.time()",
                "IntegerArgumentType.getInteger(ctx, \"%s\")".formatted(argumentName),
                Set.of(
                    "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                    "com.mojang.brigadier.arguments.IntegerArgumentType"
                )
            );
        }

        BrigadierArgumentType out = CONVERSION_MAP.get(type).apply(parameter, argumentName);
        if (out != null) {
            return out;
        }

        messager.errorElement("An unexpected error occurred whilst converting type {} to Brigadier equivalent.", parameter, type);
        return null;
    }

    private static void putSimple(SimpleEntry entry) {
        CONVERSION_MAP.put(entry.classImport(), (p, name) -> BrigadierArgumentType.of(
            "ArgumentTypes.%s()".formatted(entry.argumentTypeName()),
            "ctx.getArgument(\"%s\", %s.class)".formatted(name, entry.className()),
            Set.of(
                "io.papermc.paper.command.brigadier.argument.ArgumentTypes",
                entry.classImport()
            )
        ));
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

    record SimpleEntry(String argumentTypeName, String className, String classImport) {
        public static SimpleEntry simpleEntry(String argumentTypeName, String className, String classImport) {
            return new SimpleEntry(argumentTypeName, className, classImport);
        }
    }

    record RegistryEntry(String type, String typeImport, String fullType, String registryKeyName) {
        public static RegistryEntry registryEntry(String type, String typeImport, String registryKeyName) {
            return new RegistryEntry(type, typeImport, typeImport, registryKeyName);
        }

        public static RegistryEntry registryEntry(String type, String typeImport, String fullType, String registryKeyName) {
            return new RegistryEntry(type, typeImport, fullType, registryKeyName);
        }
    }
}
