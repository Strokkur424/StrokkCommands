package net.strokkur.commands.internal;

import com.google.auto.service.AutoService;
import net.strokkur.commands.annotations.Aliases;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Description;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
import net.strokkur.commands.annotations.Literal;
import net.strokkur.commands.annotations.Permission;
import net.strokkur.commands.annotations.RequiresOP;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static net.strokkur.commands.internal.Classnames.COMMAND_SENDER;
import static net.strokkur.commands.internal.Classnames.ENTITY;
import static net.strokkur.commands.internal.Classnames.PLAYER;

@NullMarked
@AutoService(Processor.class)
public class StrokkCommandsPreprocessor extends AbstractProcessor {

    private static final List<String> BASIC_IMPORTS = List.of(
        "com.mojang.brigadier.Command",
        "com.mojang.brigadier.tree.LiteralCommandNode",
        "org.bukkit.entity.Entity",
        "org.bukkit.entity.Player",
        "org.bukkit.command.CommandSender",
        "io.papermc.paper.command.brigadier.CommandSourceStack",
        "io.papermc.paper.command.brigadier.Commands",
        "java.util.List"
    );

    private static @Nullable Messager MESSENGER = null;

    static Optional<Messager> getMessenger() {
        return Optional.ofNullable(MESSENGER);
    }

    @NullUnmarked
    private static @NonNull List<Requirement> getAnnotatedRequirements(@NonNull Element element) {
        Permission permission = element.getAnnotation(Permission.class);
        RequiresOP requiresOP = element.getAnnotation(RequiresOP.class);

        List<Requirement> requirements = new ArrayList<>(2);
        if (permission != null) {
            requirements.add(Requirement.ofPermission(permission.value()));
        }
        if (requiresOP != null) {
            requirements.add(Requirement.IS_OP);
        }
        return requirements;
    }

    static void info(String format, Object... arguments) {
        // We don't need this outside dev
        //getMessenger().ifPresent(e -> e.printMessage(Diagnostic.Kind.NOTE, format.replaceAll("\\{}", "%s").formatted(arguments)));
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
            Command.class.getCanonicalName()
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_21;
    }

    @Override
    @NullUnmarked
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        MESSENGER = super.processingEnv.getMessager();

        for (Element element : roundEnv.getElementsAnnotatedWith(Command.class)) {
            info("Currently processing {}...", element);

            CommandInformation information = getCommandInformation(element);
            List<ExecutorInformation> executorInformation = getExecutorInformation((TypeElement) element);

            CommandTree tree = new CommandTree(information.commandName(), element, getAnnotatedRequirements(element));
            executorInformation.forEach(tree::insert);
            try {
                createBrigadierSourceFile(element.toString(), information, tree);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }

    @NullUnmarked
    private @NonNull CommandInformation getCommandInformation(@NonNull Element element) {
        Command command = element.getAnnotation(Command.class);

        Description description = element.getAnnotation(Description.class);
        Aliases aliases = element.getAnnotation(Aliases.class);

        return new CommandInformation(command.value(),
            description != null ? description.value() : null,
            aliases != null ? aliases.value() : null
        );
    }

    @NullUnmarked
    private @NonNull List<ExecutorInformation> getExecutorInformation(@NonNull TypeElement classElement) {
        return classElement.getEnclosedElements().stream()
            .filter(element -> element.getAnnotation(Executes.class) != null)
            .map(methodElement -> {
                List<? extends VariableElement> variableElements = ((ExecutableElement) methodElement).getParameters();
                List<? extends TypeMirror> typeMirrors = ((ExecutableType) methodElement.asType()).getParameterTypes();
                List<String> parameterClassNames = typeMirrors.stream().map(TypeMirror::toString).toList();
                List<String> parameterSimpleClassNames = parameterClassNames.stream().map(e -> {
                    String[] split = e.split("\\.");
                    return split[split.length - 1];
                }).toList();

                Preconditions.checkState(!parameterClassNames.isEmpty(), "Method annotated with @Executes must at least declare a CommandSender parameter!");
                Preconditions.checkState(parameterClassNames.getFirst().equals(COMMAND_SENDER), "The first parameter of a method annotated with @Executes must be of type CommandSender!");

                ExecutorType executorType = ExecutorType.NONE;
                if (typeMirrors.size() >= 2 && variableElements.get(1).getAnnotation(Executor.class) != null) {
                    executorType = switch (parameterClassNames.get(1)) {
                        case PLAYER -> ExecutorType.PLAYER;
                        case ENTITY -> ExecutorType.ENTITY;
                        default ->
                            throw new UnsupportedOperationException("The executor has to be either a player or an entity. Found: " + parameterSimpleClassNames.get(1));
                    };
                }

                List<ArgumentInformation> argumentInformation = new ArrayList<>();
                for (int i = executorType == ExecutorType.NONE ? 1 : 2; i < typeMirrors.size(); i++) {
                    Literal literalAnnotation = variableElements.get(i).getAnnotation(Literal.class);
                    if (literalAnnotation != null) {
                        argumentInformation.add(new LiteralArgumentInformation(
                            variableElements.get(i).toString(),
                            variableElements.get(i),
                            literalAnnotation.value())
                        );
                        continue;
                    }

                    BrigadierArgumentType asBrigadier = BrigadierArgumentConversion.getAsArgumentType(variableElements.get(i), variableElements.get(i).toString(), parameterClassNames.get(i));
                    if (asBrigadier == null) {
                        return null;
                    }

                    argumentInformation.add(new RequiredArgumentInformation(variableElements.get(i).toString(), variableElements.get(i), asBrigadier));
                }

                String initialLiteralsString = methodElement.getAnnotation(Executes.class).value();
                String[] initialLiterals;
                if (initialLiteralsString == null || initialLiteralsString.isBlank()) {
                    initialLiterals = null;
                } else {
                    initialLiterals = initialLiteralsString.split(" ");
                }

                List<Requirement> requirements = getAnnotatedRequirements(methodElement);
                executorType.addRequirement(requirements);

                return new ExecutorInformation(classElement, (ExecutableElement) methodElement, executorType, initialLiterals, argumentInformation, requirements);
            })
            .filter(Objects::nonNull)
            .toList();
    }

    private void createBrigadierSourceFile(String commandClassName, CommandInformation info, CommandTree command) throws IOException {
        String newClassPackageName = commandClassName + "Brigadier";

        info("Printing class file for {}", newClassPackageName);
        JavaFileObject obj = processingEnv.getFiler().createSourceFile(newClassPackageName);

        List<String> packagesAndClass = new ArrayList<>(List.of(commandClassName.split("\\.")));
        String className = packagesAndClass.getLast();
        String newClassName = packagesAndClass.getLast() + "Brigadier";

        packagesAndClass.removeLast();
        String packageName = String.join(".", packagesAndClass);

        Set<String> imports = new HashSet<>(BASIC_IMPORTS);
        command.visitEach(branch -> {
            if (branch.getArgument() instanceof RequiredArgumentInformation reqInfo) {
                imports.addAll(reqInfo.type().imports());
            }
        });

        try (PrintWriter out = new PrintWriter(obj.openWriter())) {
            out.println("package " + packageName + ";");
            out.println();

            for (String importString : imports.stream().sorted().toList()) {
                out.println("import " + importString + ";");
            }
            out.println();

            out.println("public final class " + newClassName + " {");
            out.println();
            out.println("    private static final " + className + " INSTANCE = new " + className + "();");
            out.println();
            out.print("""
                    public static void register(Commands commands) {
                        commands.register(create(), %s, List.of(%s));
                    }
                """.formatted(
                info.description() != null ? '"' + info.description() + '"' : "null",
                info.aliases() == null ? "" : '"' + String.join("\" , \"", info.aliases()) + '"'));
            out.println();
            out.println("    public static LiteralCommandNode<CommandSourceStack> create() {");
            out.print("        return ");
            out.print(command.printAsBrigadier(2));
            out.println(".build();");
            out.println("    }");
            out.println();
            out.print("""
                    private %s() {
                        throw new UnsupportedOperationException("You cannot instantiate a static class!");
                    }
                """.formatted(newClassName));

            out.println("}");
        }
    }
}
