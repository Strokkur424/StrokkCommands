package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Subcommand;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.EmptyCommandPath;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.List;

class ClassTransform implements PathTransform, ForwardingMessagerWrapper {

    protected final CommandParser parser;
    private final MessagerWrapper messager;

    public ClassTransform(final CommandParser parser, final MessagerWrapper messager) {
        this.parser = parser;
        this.messager = messager;
    }

    @Override
    public void transform(final CommandPath<?> parent, final Element element) {
        debug("> ClassTransform: parsing {}...", element);
        CommandPath<?> thisPath = this.parser.getLiteralPath(element, Command.class, Command::value);
        if (thisPath == null) {
            thisPath = this.parser.getLiteralPath(element, Subcommand.class, Subcommand::value);
        }
        if (thisPath == null) {
            thisPath = new EmptyCommandPath();
        }

        final List<CommandPath<?>> relevant = parseRecordComponents(thisPath, element);

        for (final Element enclosed : element.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.METHOD || enclosed.getKind() == ElementKind.FIELD) {
                for (final CommandPath<?> recordPath : relevant) {
                    thisPath.addChild(recordPath);
                    this.parser.populateRequirements(recordPath, element);
                    this.parser.parse(recordPath, enclosed);
                }
            }
        }

        parent.addChild(thisPath);
    }

    protected List<CommandPath<?>> parseRecordComponents(final CommandPath<?> parent, final Element element) {
        return Collections.singletonList(parent);
    }

    @Override
    public boolean canTransform(final Element element) {
        if (!(element instanceof TypeElement type && type.getKind() == ElementKind.CLASS)) {
            return false;
        }

        //noinspection ConstantValue
        return type.getAnnotation(Command.class) != null || type.getAnnotation(Subcommand.class) != null;
    }

    @Override
    public MessagerWrapper delegateMessager() {
        return this.messager;
    }
}
