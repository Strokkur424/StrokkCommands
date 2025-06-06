package net.strokkur.commands.reflection;

import net.strokkur.commands.annotations.Aliases;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Description;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
import net.strokkur.commands.exceptions.AnnotationMissingException;
import net.strokkur.commands.exceptions.ReflectionException;
import net.strokkur.commands.objects.ArgumentInformation;
import net.strokkur.commands.objects.CommandInformation;
import net.strokkur.commands.objects.ExecutorInformation;
import net.strokkur.commands.objects.ExecutorType;
import net.strokkur.commands.utils.BrigadierUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NullUnmarked
public class ReflectionHelper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionHelper.class);

    public static CommandInformation getInformation(@NonNull Class<?> clazz) {
        return run(() -> {
            Command command = clazz.getAnnotation(Command.class);
            if (command == null) {
                throw new AnnotationMissingException(Command.class, clazz);
            }

            Aliases aliases = clazz.getAnnotation(Aliases.class);
            Description description = clazz.getAnnotation(Description.class);

            return new CommandInformation(
                command.value(),
                description != null ? description.value() : null,
                aliases != null ? aliases.value() : null
            );
        }).orElseThrow(() -> new ReflectionException("Failed to get command annotations for " + clazz.getName()));
    }

    public static List<ExecutorInformation> getCommandExecutors(@NonNull Class<?> command) {
        return run(() -> {
            List<ExecutorInformation> executors = new ArrayList<>();

            for (Method method : command.getDeclaredMethods()) {
                Map<Class<? extends Annotation>, Annotation> annotations = Stream.of(method.getAnnotations())
                    .collect(Collectors.toMap(Annotation::annotationType, Function.identity()));
                
                annotations.forEach((type, annotation) -> LOGGER.info("Found annotation of type {} at {}", type.getName(), method.getName()));

                if (annotations.containsKey(Executes.class)) {
                    LOGGER.info("Method {} of {} is an executor!", method.getName(), command.getName());
                    List<Parameter> parameters = List.of(method.getParameters());

                    ExecutorType executorType = parameters.stream()
                        .filter(param -> param.getAnnotation(Executor.class) != null)
                        .findFirst()
                        .map(ReflectionHelper::getExecutorType).orElse(ExecutorType.NONE);
                    
                    LOGGER.info("| ExecutorType: {}", executorType);
                    
                    List<ArgumentInformation> arguments = new ArrayList<>();
                    for (int i = executorType == ExecutorType.NONE ? 1 : 2; i < parameters.size(); i++) {
                        Parameter param = parameters.get(i);
                        arguments.add(new ArgumentInformation(param.getType(), param, BrigadierUtils.getAsArgumentType(param, command, method)));
                        LOGGER.info("Added '{} {}' as an argument!", param.getType().getSimpleName(), param.getName());
                    }

                    executors.add(new ExecutorInformation(method, executorType, arguments));
                }
            }

            return executors;
        }).orElseThrow(() -> new ReflectionException("Failed to get executors for " + command.getName()));
    }

    private static @NotNull ExecutorType getExecutorType(@Nullable Parameter parameter) {
        if (parameter == null) {
            return ExecutorType.NONE;
        }

        Class<?> executorParameterType = parameter.getType();
        if (executorParameterType == Player.class) {
            return ExecutorType.PLAYER;
        } else if (executorParameterType == Entity.class) {
            return ExecutorType.ENTITY;
        }

        return ExecutorType.NONE;
    }
    
    public static void silenceReflectiveOperation(SilentReflectiveOperation operation) {
        try {
            operation.run();
        }
        catch (ReflectiveOperationException exception) {
            LOGGER.warn("Failed to do reflective operation:", exception);
        }
        catch (Exception ex) {
            LOGGER.error("Uncaught exception:", ex);
        }
    }

    @NullMarked
    private static <T> Optional<T> run(ReflectiveOperation<T> operation) {
        try {
            return Optional.of(operation.run());
        } catch (ReflectiveOperationException exception) {
            return Optional.empty();
        }
    }

    @FunctionalInterface
    private interface ReflectiveOperation<T> {
        T run() throws ReflectiveOperationException;
    }
    
    @FunctionalInterface
    public interface SilentReflectiveOperation {
        void run() throws ReflectiveOperationException;
    }
}
