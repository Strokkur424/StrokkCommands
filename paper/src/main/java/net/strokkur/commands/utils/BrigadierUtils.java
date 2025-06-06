package net.strokkur.commands.utils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.strokkur.commands.annotations.arguments.StringArg;
import net.strokkur.commands.exceptions.UnknownArgumentException;
import net.strokkur.commands.objects.CommandInformation;
import net.strokkur.commands.objects.ExecutorInformation;
import net.strokkur.commands.objects.ExecutorType;
import net.strokkur.commands.reflection.ReflectionHelper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@NullUnmarked
public abstract class BrigadierUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BrigadierUtils.class);

    @NonNull
    public static ArgumentType<?> getAsArgumentType(@NonNull Parameter parameter, @NonNull Class<?> command, @NonNull Method method) throws ReflectiveOperationException, UnknownArgumentException {
        Class<?> type = parameter.getType();

        if (type == String.class) {
            StringArg stringAnnotation = parameter.getAnnotation(StringArg.class);
            if (stringAnnotation == null) {
                return StringArgumentType.word();
            }

            return switch (stringAnnotation.value()) {
                case WORD -> StringArgumentType.word();
                case STRING -> StringArgumentType.string();
                case GREEDY -> StringArgumentType.greedyString();
            };
        }

        throw new UnknownArgumentException(type, command, method);
    }

    public static LiteralCommandNode<CommandSourceStack> buildTree(Object instance, CommandInformation command, List<ExecutorInformation> executors) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(command.commandName());

        LOGGER.info("Building tree for {}...", command.commandName());
        for (ExecutorInformation executor : executors) {
            LOGGER.info("Executor {} has {} arguments.", executor.method().getName(), executor.arguments().size());
            if (executor.arguments().isEmpty()) {
                root.requires(executor.type())
                    .executes(ctx -> {
                        ReflectionHelper.silenceReflectiveOperation(() -> {
                            Method method = executor.method();
                            method.setAccessible(true);
                            if (executor.type() == ExecutorType.NONE) {
                                method.invoke(instance, ctx.getSource().getSender());
                            } else {
                                method.invoke(instance, ctx.getSource().getSender(), ctx.getSource().getExecutor());
                            }
                        });
                        return Command.SINGLE_SUCCESS;
                    });
            }
        }

        return root.build();
    }
}
