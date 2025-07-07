package net.strokkur.commands.internal;

import net.strokkur.commands.internal.arguments.RequiredArgumentInformation;
import net.strokkur.commands.internal.intermediate.CommandInformation;
import net.strokkur.commands.internal.intermediate.CommandTree;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ClassFilePrinter {
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

    private final String commandClassName;
    private final CommandInformation info;
    private final CommandTree commandTree;
    private final MessagerWrapper messager;

    public ClassFilePrinter(String commandClassName, CommandInformation info, CommandTree commandTree, MessagerWrapper messager) {
        this.commandClassName = commandClassName;
        this.info = info;
        this.commandTree = commandTree;
        this.messager = messager;
    }

    public void print(Filer filer) {
        try {
            createBrigadierSourceFile(filer);
        } catch (IOException ex) {
            messager.error("A fatal error occurred while processing java source file for {}: {}", info.commandName(), ex.getMessage());
        }
    }


    private void createBrigadierSourceFile(Filer filer) throws IOException {
        String newClassPackageName = commandClassName + "Brigadier";

        messager.info("Printing class file for {}", newClassPackageName);
        JavaFileObject obj = filer.createSourceFile(newClassPackageName);

        List<String> packagesAndClass = new ArrayList<>(List.of(commandClassName.split("\\.")));
        String className = packagesAndClass.getLast();
        String newClassName = packagesAndClass.getLast() + "Brigadier";

        packagesAndClass.removeLast();
        String packageName = String.join(".", packagesAndClass);

        Set<String> imports = new HashSet<>(BASIC_IMPORTS);
        commandTree.visitEach(branch -> {
            if (branch.getArgument() instanceof RequiredArgumentInformation reqInfo) {
                imports.addAll(reqInfo.getType().imports());
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
            out.print(commandTree.printAsBrigadier(2));
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
