package net.strokkur.commands.internal;

import net.strokkur.commands.annotations.Aliases;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Description;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
import net.strokkur.commands.annotations.Literal;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.BrigadierArgumentType;
import net.strokkur.commands.internal.arguments.LiteralArgumentInfoImpl;
import net.strokkur.commands.internal.arguments.RequiredArgumentInformation;
import net.strokkur.commands.internal.intermediate.CommandInformation;
import net.strokkur.commands.internal.intermediate.CommandTree;
import net.strokkur.commands.internal.intermediate.ExecutorInformation;
import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.Requirement;
import net.strokkur.commands.internal.multiliterals.MultiLiteralsTree;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.commands.internal.util.Utils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;

import javax.annotation.processing.AbstractProcessor;
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
import java.util.Set;

import static net.strokkur.commands.internal.util.Classes.COMMAND_SENDER;
import static net.strokkur.commands.internal.util.Classes.ENTITY;
import static net.strokkur.commands.internal.util.Classes.PLAYER;

@NullMarked
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

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Command.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_21;
    }

    @Override
    @NullUnmarked
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final MessagerWrapper messagerWrapper = MessagerWrapper.wrap(super.processingEnv.getMessager());

        for (Element element : roundEnv.getElementsAnnotatedWith(Command.class)) {
            messagerWrapper.info("Currently processing {}...", element);

            CommandInformation information = getCommandInformation(element);
            List<ExecutorInformation> executorInformation = getExecutorInformation((TypeElement) element, messagerWrapper);

            CommandTree tree = new CommandTree(information.commandName(), element, Utils.getAnnotatedRequirements(element));
            executorInformation.forEach(info -> tree.insert(info, messagerWrapper));
            try {
                createBrigadierSourceFile(element.toString(), information, tree, messagerWrapper);
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
    private @NonNull List<ExecutorInformation> getExecutorInformation(@NonNull TypeElement classElement, MessagerWrapper messager) {
        List<ExecutorInformation> out = new ArrayList<>();
        BrigadierArgumentConverter converter = new BrigadierArgumentConverter(messager);

        classElement.getEnclosedElements().stream()
            .filter(element -> element.getAnnotation(Executes.class) != null)
            .map(methodElement -> {
                List<? extends VariableElement> parameters = ((ExecutableElement) methodElement).getParameters();
                List<? extends TypeMirror> parameterTypes = ((ExecutableType) methodElement.asType()).getParameterTypes();
                List<String> parameterClassNames = parameterTypes.stream().map(TypeMirror::toString).toList();

                if (parameterClassNames.isEmpty()) {
                    messager.errorElement("Method annotated with @Executes must at least declare a CommandSender parameter!", methodElement);
                    return null;
                }

                if (!parameterClassNames.getFirst().equals(COMMAND_SENDER)) {
                    messager.errorElement("The first parameter of a method annotated with @Executes must be of type CommandSender!", methodElement);
                    return null;
                }

                ExecutorType executorType = ExecutorType.NONE;
                if (parameterTypes.size() >= 2 && parameters.get(1).getAnnotation(Executor.class) != null) {
                    executorType = switch (parameterClassNames.get(1)) {
                        case PLAYER -> ExecutorType.PLAYER;
                        case ENTITY -> ExecutorType.ENTITY;
                        default -> null;
                    };
                }

                if (executorType == null) {
                    messager.errorElement("The executor has to be either an org.bukkit.entity.Player or an org.bukkit.entity.Entity!", parameters.get(1));
                    return null;
                }

                MultiLiteralsTree tree = MultiLiteralsTree.create();
                String initialLiteralsString = methodElement.getAnnotation(Executes.class).value();
                if (!initialLiteralsString.isBlank()) {
                    for (String literals : initialLiteralsString.split(" ")) {
                        tree.insert(new LiteralArgumentInfoImpl(literals, methodElement, literals, false));
                    }
                }

                for (int i = executorType == ExecutorType.NONE ? 1 : 2; i < parameterTypes.size(); i++) {
                    Literal literalAnnotation = parameters.get(i).getAnnotation(Literal.class);
                    if (literalAnnotation != null) {
                        tree.insert(new LiteralArgumentInfoImpl(parameters.get(i).toString(), parameters.get(i), ""), List.of(literalAnnotation.value()));
                        continue;
                    }

                    BrigadierArgumentType asBrigadier = converter.getAsArgumentType(parameters.get(i), parameters.get(i).toString(), parameterClassNames.get(i));
                    if (asBrigadier == null) {
                        return null;
                    }

                    RequiredArgumentInformation argument = new RequiredArgumentInformation(parameters.get(i).toString(), parameters.get(i), asBrigadier);
                    argument.updateSuggestionProvider(classElement, parameters.get(i), messager);
                    tree.insert(argument);
                }

                List<Requirement> requirements = Utils.getAnnotatedRequirements(methodElement);
                executorType.addRequirement(requirements);

                final ExecutorType finalExecutorType = executorType;
                return tree.flatten().stream()
                    .map(arguments -> new ExecutorInformation(classElement, (ExecutableElement) methodElement, finalExecutorType, arguments, requirements))
                    .toList();
            })
            .filter(Objects::nonNull)
            .forEach(out::addAll);

        return out;
    }

    private void createBrigadierSourceFile(String commandClassName, CommandInformation info, CommandTree command, MessagerWrapper messager) throws IOException {
        String newClassPackageName = commandClassName + "Brigadier";

        messager.info("Printing class file for {}", newClassPackageName);
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
