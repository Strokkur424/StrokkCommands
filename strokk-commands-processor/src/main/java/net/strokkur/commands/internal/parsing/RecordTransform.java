package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Subcommand;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.RecordPath;
import net.strokkur.commands.internal.intermediate.paths.RecordPathImpl;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;

class RecordTransform extends ClassTransform {

    public RecordTransform(final CommandParser parser, final MessagerWrapper messager) {
        super(parser, messager);
    }

    @Override
    protected void addAccessAttribute(final CommandPath<?> path, final TypeElement element) {
        // no impl
    }

    @Override
    protected List<CommandPath<?>> parseRecordComponents(final CommandPath<?> parent, final Element element) {
        final List<? extends Element> enclosedElements = element.getEnclosedElements();

        final List<VariableElement> recordComponents = new ArrayList<>(enclosedElements.size());
        for (final Element enclosed : enclosedElements) {
            if (enclosed.getKind() == ElementKind.RECORD_COMPONENT) {
                recordComponents.add((VariableElement) enclosed);
            }
        }

        final List<List<CommandArgument>> possibleArguments = this.parser.parseArguments(recordComponents, (TypeElement) element);
        final List<CommandPath<?>> paths = new ArrayList<>(possibleArguments.size());

        for (final List<CommandArgument> arguments : possibleArguments) {
            final RecordPath recordPath = new RecordPathImpl(arguments);
            parent.addChild(recordPath);
            paths.add(recordPath);
        }

        return paths;
    }

    @Override
    public boolean canTransform(final Element element) {
        if (!(element instanceof TypeElement type && type.getKind() == ElementKind.RECORD)) {
            return false;
        }

        //noinspection ConstantValue
        return type.getAnnotation(Command.class) != null || type.getAnnotation(Subcommand.class) != null;
    }
}
