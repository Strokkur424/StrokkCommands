package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Subcommand;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

class FieldTransform implements PathTransform, ForwardingMessagerWrapper {

    private final CommandParser parser;
    private final MessagerWrapper messager;

    public FieldTransform(final CommandParser parser, final MessagerWrapper messager) {
        this.parser = parser;
        this.messager = messager;
    }

    @Override
    public void transform(final CommandPath<?> parent, final Element element) {




    }

    @Override
    public boolean canTransform(final Element element) {
        //noinspection ConstantValue
        return element.getKind() == ElementKind.FIELD && (element.getAnnotation(Command.class) != null || element.getAnnotation(Subcommand.class) != null);
    }

    @Override
    public MessagerWrapper delegateMessager() {
        return this.messager;
    }
}
