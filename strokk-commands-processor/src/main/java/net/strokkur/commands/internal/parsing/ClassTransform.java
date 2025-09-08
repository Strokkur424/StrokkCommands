package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Subcommand;
import net.strokkur.commands.internal.intermediate.access.InstanceAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class ClassTransform implements PathTransform, ForwardingMessagerWrapper {

    private static final Set<ElementKind> ENCLOSED_ELEMENTS_TO_PARSE = Set.of(
        ElementKind.METHOD,
        ElementKind.FIELD,
        ElementKind.CLASS,
        ElementKind.RECORD
    );

    protected final CommandParser parser;
    private final MessagerWrapper messager;

    public ClassTransform(final CommandParser parser, final MessagerWrapper messager) {
        this.parser = parser;
        this.messager = messager;
    }

    @Override
    public void transform(final CommandPath<?> parent, final Element element) {
        debug("> ClassTransform: parsing {}...", element);

        final CommandPath<?> thisPath = this.createThisPath(parent, this.parser, element);
        addAccessAttribute(thisPath, (TypeElement) element);

        final List<CommandPath<?>> relevant = parseRecordComponents(thisPath, element);

        for (final Element enclosed : element.getEnclosedElements()) {
            if (!ENCLOSED_ELEMENTS_TO_PARSE.contains(enclosed.getKind())) {
                continue;
            }

            for (final CommandPath<?> recordPath : relevant) {
                this.parser.populateRequirements(recordPath, element);
                this.parser.weakParse(recordPath, enclosed);
            }
        }
    }

    protected void addAccessAttribute(final CommandPath<?> path, final TypeElement element) {
        path.setAttribute(AttributeKey.ACCESS_STACK, (InstanceAccess) () -> element);
    }

    protected List<CommandPath<?>> parseRecordComponents(final CommandPath<?> parent, final Element element) {
        return Collections.singletonList(parent);
    }

    @Override
    public boolean hardRequirement(final Element element) {
        return element.getKind() == ElementKind.CLASS;
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
