package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Subcommand;
import net.strokkur.commands.internal.StrokkCommandsPreprocessor;
import net.strokkur.commands.internal.intermediate.access.FieldAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;

class FieldTransform implements PathTransform, ForwardingMessagerWrapper {

    private final CommandParser parser;
    private final MessagerWrapper messager;

    public FieldTransform(final CommandParser parser, final MessagerWrapper messager) {
        this.parser = parser;
        this.messager = messager;
    }

    @Override
    public void transform(final CommandPath<?> parent, final Element element) {
        debug("> FieldTransform: {}.{}", element.getEnclosingElement().getSimpleName(), element.getSimpleName());
        final CommandPath<?> thisPath = createThisPath(parent, this.parser, element);
        thisPath.setAttribute(AttributeKey.ACCESS_STACK, (FieldAccess) () -> (VariableElement) element);

        this.parser.hardParse(thisPath, StrokkCommandsPreprocessor.getTypes().asElement(element.asType()));
    }

    @Override
    public boolean hardRequirement(final Element element) {
        return element.getKind() == ElementKind.FIELD;
    }

    @Override
    public boolean weakRequirement(final Element element) {
        //noinspection ConstantValue
        return element.getAnnotation(Command.class) != null || element.getAnnotation(Subcommand.class) != null;
    }

    @Override
    public MessagerWrapper delegateMessager() {
        return this.messager;
    }
}
