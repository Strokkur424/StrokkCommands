package net.strokkur.commands.internal.intermediate.paths;

import net.strokkur.commands.internal.arguments.CommandArgument;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface CommandPath<S extends CommandArgument> {

    @UnmodifiableView
    List<S> getArguments();

    @Nullable
    CommandPath<?> getParent();

    @UnmodifiableView
    List<CommandPath<?>> getChildren();

    void removeChild(CommandPath<?> child);

    void addChild(CommandPath<?> child);

    void setParent(@Nullable CommandPath<?> parent);

    /**
     * Splits the argument path of this path and returns an instance of the first half of the split
     * path. The second split path will be the same instance as this path.
     *
     * @param index the index to split at
     * @return the new, left side instance of the split
     */
    CommandPath<S> splitPath(int index);

    /**
     * Returns the number of same arguments at the start of this and the provided path.
     *
     * @param other other path
     * @return number of arguments that are the same at the start
     */
    default int getSameArguments(CommandPath<?> other) {
        final List<?> arguments = getArguments();
        final List<?> otherArguments = other.getArguments();
        final int min = Math.min(arguments.size(), otherArguments.size());

        for (int i = 0; i < min; i++) {
            if (!arguments.get(i).equals(otherArguments.get(i))) {
                return i;
            }
        }

        return min;
    }
}
