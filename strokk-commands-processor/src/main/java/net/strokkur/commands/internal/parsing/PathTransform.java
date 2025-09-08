package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Subcommand;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.EmptyCommandPath;

import javax.lang.model.element.Element;

interface PathTransform {

    void transform(CommandPath<?> parent, Element element);

    boolean hardRequirement(Element element);

    boolean weakRequirement(Element element);

    default boolean shouldTransform(Element element) {
        return hardRequirement(element) && weakRequirement(element);
    }

    default CommandPath<?> createThisPath(CommandPath<?> parent, CommandParser parser, Element element) {
        CommandPath<?> thisPath = parser.getLiteralPath(element, Command.class, Command::value);
        if (thisPath == null) {
            thisPath = parser.getLiteralPath(element, Subcommand.class, Subcommand::value);
        }
        if (thisPath == null) {
            thisPath = new EmptyCommandPath();
        }

        parent.addChild(thisPath);
        return thisPath;
    }
}
