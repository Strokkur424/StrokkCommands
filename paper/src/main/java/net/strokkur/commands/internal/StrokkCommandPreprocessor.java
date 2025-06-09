package net.strokkur.commands.internal;

import com.google.auto.service.AutoService;
import net.strokkur.commands.annotations.Aliases;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Description;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
import net.strokkur.commands.annotations.Literal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.strokkur.commands.internal.Classnames.COMMAND_SENDER;
import static net.strokkur.commands.internal.Classnames.ENTITY;
import static net.strokkur.commands.internal.Classnames.PLAYER;

@NullMarked
@AutoService(Processor.class)
public class StrokkCommandPreprocessor extends AbstractProcessor {

    private static final String[] BASIC_IMPORTS = {
        "com.mojang.brigadier.Command",
        "com.mojang.brigadier.arguments.FloatArgumentType",
        "com.mojang.brigadier.arguments.IntegerArgumentType",
        "com.mojang.brigadier.arguments.StringArgumentType",
        "com.mojang.brigadier.tree.LiteralCommandNode",
        "org.bukkit.entity.Entity",
        "org.bukkit.entity.Player",
        "org.bukkit.command.CommandSender",
        "io.papermc.paper.command.brigadier.CommandSourceStack",
        "io.papermc.paper.command.brigadier.Commands",
        "java.util.List"
    };

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
    
    /*
    @Command(...)
    public void MyCommand {
    
        @Executes
        public void execute(CommandSender sender, @Executor Player player) {
            // logic ...
        }
    
        @Executes
        public void execute(CommandSender sender, @Executor Player player, String wordInput) {
            // logic ...
        }
        
        @Executes
        public void execute(CommandSender sender, String wordInput, int exp) {
            // logic ...
        }
    }
    
    <--->
    
    public void MyCommandBrigadierImpl {
        
        private static final MyCommand instance = new MyCommand();
        
        public static LiteralCommandNode<CommandSourceStack> build() {
            return Commands.literal(...)
                .requires(stack -> stack.getExecutor() instanceof Player)
                .executes(ctx -> {
                    instance.execute(ctx.getSource().getSender(), ctx.getSource().getExecutor());
                    return Command.SINGLE_SUCCESS;
                })
            
                .then(Commands.argument("wordInput", StringArgumentType.word())
                    .requires(stack -> stack.getExecutor() instanceof Player)
                    .executes(ctx -> {
                        instance.execute(ctx.getSource().getSender(), ctx.getSource().getExecutor(), StringArgumentType.getString(ctx, "wordInput"));
                        return Command.SINGLE_SUCCESS;
                    })
                )
                
                // This would happen twice~. We need to build up our own tree which combines these things first, so that we can build the correct
                // Brigadier tree out of our command annotations.
                .then(Command.argument("wordInput", StringArgumentType.word())
                    .then(Commands.argument("exp", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            instance.execute(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "wordInput"), IntegerArgumentType.getInt(ctx, "exp"));
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
                .build();
        }
    }
    
    
    ============ Structure Update ==============
    @Executes
    private void noArgs(CommandSender sender, @Executor Player executor) {
        sender.sendRichMessage("<green>Success! The executing player is " + executor.getName());
        executor.sendRichMessage("<gradient:gold:yellow>Hehe very good!");
    }
    
    @Executes("name")
    private void nameArg(CommandSender sender, String name) {
        sender.sendRichMessage("<green>You put in: <name>", Placeholder.unparsed("name", name));
    }
    
    @Executes("float")
    private void floatArg(CommandSender sender, float value) {
        sender.sendRichMessage("<transition:red:blue:%s>This is some transitioning text :P (%s)".formatted(value, value));
    }
    
    @Executes("literal-test")
    private void literalTest(CommandSender sender, @Literal({"one", "two", "three"}) String literal, @IntArg(min = 1, max = 3) int value) {
        int actual = switch (literal) {
            case "one" -> 1;
            case "two" -> 2;
            case "three" -> 3;
            default -> throw new UnsupportedOperationException("Incorrect literal");
        };
        
        sender.sendPlainMessage("Your input is " + literal + ", which " + (actual == value ? "matches" : "doesn't match") + " your input value!");
    }
    
    ---
    Should convert to:
    ---
    
    Commands.literal("simplecommand")
        .requires(stack -> stack.getExecutor() instanceof Player)
        .executes(ctx -> {
            instance.noArgs(ctx.getSource().getSender(), (Player) ctx.getSource().getExecutor())
            return Command.SINGLE_SUCCESS;
        })
        
        .then(Commands.literal("name")
            .then(Commands.argument("name", StringArgumentType.word())
                .executes(ctx -> {
                    instance.nameArg(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "name"));
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        
        .then(Commands.literal("float")
            .then(Commands.argument("value", FloatArgumentType.float())
                .executes(ctx -> {
                    instance.floatArg(ctx.getSource().getSender(), FloatArgumentType.getFloat(ctx, "value"));
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        
        .then(Commands.literal("literal-test")
            .then(Commands.literal("one")
                .then(Commands.argument("value", IntegerArgumentType.integer(1, 3))
                    .executes(ctx -> {
                        instance.literalTest(ctx.getSource().getSender(), "one", IntegerArgumentType.getInteger(ctx, "value"));
                        return Command.SINGLE_SUCCESS;
                    });
                )
            )
            .then(Commands.literal("two")
                .then(Commands.argument("value", IntegerArgumentType.integer(1, 3))
                    .executes(ctx -> {
                        instance.literalTest(ctx.getSource().getSender(), "two", IntegerArgumentType.getInteger(ctx, "value"));
                        return Command.SINGLE_SUCCESS;
                    });
                )
            )
            .then(Commands.literal("three")
                .then(Commands.argument("value", IntegerArgumentType.integer(1, 3))
                    .executes(ctx -> {
                        instance.literalTest(ctx.getSource().getSender(), "three", IntegerArgumentType.getInteger(ctx, "value"));
                        return Command.SINGLE_SUCCESS;
                    });
                )
            )
        )
    
    
    
     */

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Command.class)) {
            info("Currently processing {}...", element);

            CommandInformation information = getCommandInformation(element);
            List<ExecutorInformation> executorInformation = getExecutorInformation(element);

            CommandTree tree = new CommandTree(null);
            tree.setName(information.commandName());

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
    private @NonNull List<ExecutorInformation> getExecutorInformation(@NonNull Element classElement) {
        return classElement.getEnclosedElements().stream()
            .filter(element -> element.getAnnotation(Executes.class) != null)
            .map(methodElement -> {
                String name = methodElement.getSimpleName().toString();
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
                        argumentInformation.add(new LiteralArgumentInformation(variableElements.get(i).toString(), literalAnnotation.value()));
                        continue;
                    }

                    argumentInformation.add(new RequiredArgumentInformation(
                        variableElements.get(i).toString(),
                        BrigadierArgumentConversion.getAsArgumentType(variableElements.get(i), variableElements.get(i).toString(), parameterClassNames.get(i))
                    ));
                }

                String initialLiteralsString = methodElement.getAnnotation(Executes.class).value();
                String[] initialLiterals;
                if (initialLiteralsString == null || initialLiteralsString.isBlank()) {
                    initialLiterals = null;
                } else {
                    initialLiterals = initialLiteralsString.split(" ");
                }

                return new ExecutorInformation(name, executorType, initialLiterals, argumentInformation);
            }).toList();
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

        try (PrintWriter out = new PrintWriter(obj.openWriter())) {
            out.println("package " + packageName + ";");
            out.println();

            for (String importString : BASIC_IMPORTS) {
                out.println("import " + importString + ";");
            }
            out.println();

            out.println("public class " + newClassName + " {");
            out.println();
            out.println("    private static " + className + " instance = new " + className + "();");
            out.println();
            out.print("""
                    public static void register(Commands commands) {
                        commands.register(create(), "%s", List.of(%s));
                    }
                """.formatted(info.description(), info.aliases() == null ? "" : '"' + String.join("\" , \"", info.aliases()) + '"'));
            out.println();
            out.println("    public static LiteralCommandNode<CommandSourceStack> create() {");
            out.print("        return ");
            out.print(command.printAsBrigadier(2));
            out.println(".build();");
            out.println("    }");
            out.println("}");
        }
    }

    private void info(String format, Object... arguments) {
        super.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, format.replaceAll("\\{}", "%s").formatted(arguments));
    }
}
