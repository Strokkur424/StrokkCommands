package net.strokkur.commands.internal.intermediate.paths;

import net.strokkur.commands.internal.arguments.CommandArgument;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class SimpleCommandPathImpl<S extends CommandArgument> implements CommandPath<S> {

    protected final List<CommandPath<?>> children;
    protected @Nullable CommandPath<?> parent;
    protected List<S> arguments;

    public SimpleCommandPathImpl(final List<S> arguments) {
        this.children = new ArrayList<>();
        this.parent = null;
        this.arguments = arguments;
    }

    abstract SimpleCommandPathImpl<S> createLeftSplit(final List<S> args);

    @Override
    public @UnmodifiableView List<S> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    @Override
    public @Nullable CommandPath<?> getParent() {
        return this.parent;
    }

    @Override
    public @UnmodifiableView List<CommandPath<?>> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void removeChild(final CommandPath<?> child) {
        child.setParent(null);
        this.children.remove(child);
    }

    @Override
    public void addChild(final CommandPath<?> child) {
        if (child.getParent() != null) {
            // Just a bit of cleanup to avoid dangling references
            child.getParent().removeChild(child);
        }

        this.children.add(child);
        child.setParent(this);
    }

    @Override
    public void setParent(final @Nullable CommandPath<?> parent) {
        this.parent = parent;
    }

    @Override
    public CommandPath<S> splitPath(final int index) {
        List<S> left = new ArrayList<>(arguments.subList(0, index));
        arguments = new ArrayList<>(arguments.subList(index, arguments.size()));
        SimpleCommandPathImpl<S> leftPath = createLeftSplit(left);

        if (this.parent != null) {
            this.parent.removeChild(this);
            this.parent.addChild(leftPath);
        }

        leftPath.children.clear();
        leftPath.addChild(this);

        return leftPath;
    }
}