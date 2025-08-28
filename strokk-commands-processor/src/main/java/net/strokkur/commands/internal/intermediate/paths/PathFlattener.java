package net.strokkur.commands.internal.intermediate.paths;

import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;

import java.util.List;
import java.util.Set;

public class PathFlattener implements ForwardingMessagerWrapper {

    private final MessagerWrapper messager;

    public PathFlattener(final MessagerWrapper messager) {
        this.messager = messager;
    }

    public void flattenPath(CommandPath<?> path) {
        // The idea here is that if during parsing, the following structure occurs:
        //
        // main
        // | sub
        // | | arg1
        // | sub
        // | | arg2
        //
        // You would expect it to be flattened down to just this:
        //
        // main
        // | sub
        // | | arg1
        // | | arg2
        //
        // We do this by smartly checking a node's parent node path and merging them in case
        // some parts overlap. This merging step happens for both literal nodes and argument nodes.

        // 1. Attempt to merge sibling nodes which start with the same argument combinations.
        int i = 0;
        while (i < path.getChildren().size()) {
            boolean tryAgain = false;
            final CommandPath<?> child = path.getChildren().get(i);

            for (int j = i + 1; j < path.getChildren().size(); j++) {
                final CommandPath<?> sibling = path.getChildren().get(j);

                if (child == sibling) {
                    continue;
                }

                final int same = child.getSameArguments(sibling);
                debug("'%s' and '%s' have %d args in common.", child.toStringNoChildren(), sibling.toStringNoChildren(), same);
                if (same == 0) {
                    continue;
                }

                // The nodes have common arguments, split the one with less amount of arguments and add the
                // remained of the longer one to it.
                final CommandPath<?> less;
                final CommandPath<?> more;

                if (child.getArguments().size() < sibling.getArguments().size()) {
                    less = child;
                    more = sibling;
                } else {
                    less = sibling;
                    more = child;
                }

                final CommandPath<?> newParent;
                if (less.getArguments().size() == same) {
                    newParent = less;
                } else {
                    newParent = less.splitPath(same);
                }

                path.removeChild(more);
                more.splitPath(same);
                newParent.addChild(more);
                tryAgain = true;
                break;
            }

            if (!tryAgain) {
                i++;
            }
        }

        for (final CommandPath<?> commandPath : path.getChildren()) {
            flattenPath(commandPath);
        }
    }

    public void cleanupPath(CommandPath<?> path) {
        // Depth first.
        for (final CommandPath<?> commandPath : path.getChildren()) {
            cleanupPath(commandPath);
        }

        // The relevant attributes are the 'permission', 'requires_op', 'executor, and 'requirement'
        // attributes, since these add some sort of `.requires` clause, which works the best the higher
        // up it is in the tree. Certain attribute values also cause the parent value to be written to
        // explicitly in order to **avoid** merging values. A.e. a NONE executor will force itself to
        // the parent in order to avoid being swallowed by a sister path, which may have an executor requirement.

        // Once an attribute is passed on, it will be removed from the child node in order to help the
        // source file printer a bit.

        handleExecutor(path);
        handleOperator(path);
        handlePermissions(path);
    }

    /**
     * Here, the case is similar to the REQUIRES_OP attribute, which the slight difference that
     * the **minimum requirement** will be used, meaning if a child has an ENTITY, and another a PLAYER
     * executor requirement, the parent path will declare an ENTITY executor requirement as well.
     *
     * @param path the path to handle
     */
    private void handleExecutor(final CommandPath<?> path) {
        ExecutorType lowestRequirement = path.hasAttribute(AttributeKey.EXECUTOR_TYPE) ? path.getAttribute(AttributeKey.EXECUTOR_TYPE) : null;
        for (final CommandPath<?> child : path.getChildren()) {
            final ExecutorType childExecutorType = child.getAttributeNotNull(AttributeKey.EXECUTOR_TYPE);

            if (lowestRequirement == null) {
                lowestRequirement = childExecutorType;
                continue;
            }

            if (childExecutorType == ExecutorType.NONE) {
                lowestRequirement = ExecutorType.NONE;
                break; // We cannot set an executor requirement on the parent node
            }

            if (lowestRequirement.isMoreRestrictiveOrEqualThan(childExecutorType)) {
                lowestRequirement = childExecutorType;
            }
        }

        if (lowestRequirement == ExecutorType.NONE || lowestRequirement == null) {
            path.removeAttribute(AttributeKey.EXECUTOR_TYPE);
        } else {
            path.setAttribute(AttributeKey.EXECUTOR_TYPE, lowestRequirement);
            for (final CommandPath<?> child : path.getChildren()) {
                if (lowestRequirement.isMoreRestrictiveOrEqualThan(child.getAttributeNotNull(AttributeKey.EXECUTOR_TYPE))) {
                    if (child instanceof ExecutablePath) {
                        child.setAttribute(AttributeKey.EXECUTOR_HANDLED, true);
                    } else {
                        child.removeAttribute(AttributeKey.EXECUTOR_TYPE);
                    }
                }
            }
        }
    }

    private void handleOperator(final CommandPath<?> path) {
        if (!path.hasAttribute(AttributeKey.REQUIRES_OP)) {
            final List<CommandPath<?>> children = path.getChildren();

            if (children.size() == 1) {
                if (children.getFirst().getAttributeNotNull(AttributeKey.REQUIRES_OP)) {
                    path.setAttribute(AttributeKey.REQUIRES_OP, true);
                    path.forEachChild(child -> child.removeAttribute(AttributeKey.REQUIRES_OP));
                }
            } else if (children.size() > 1) {
                boolean allOfThemAreTrue = false;
                for (CommandPath<?> child : children) {
                    if (child.getAttributeNotNull(AttributeKey.REQUIRES_OP)) {
                        allOfThemAreTrue = true;
                    } else {
                        allOfThemAreTrue = false;
                        break;
                    }
                }

                if (allOfThemAreTrue) {
                    path.setAttribute(AttributeKey.REQUIRES_OP, true);
                    path.forEachChild(child -> child.removeAttribute(AttributeKey.REQUIRES_OP));
                }
            }
        }
    }

    private void handlePermissions(final CommandPath<?> path) {
        if (!path.hasAttribute(AttributeKey.PERMISSIONS)) {
            final Set<String> thisPermissions = path.getAttributeNotNull(AttributeKey.PERMISSIONS);
            final List<CommandPath<?>> children = path.getChildren();

            for (final CommandPath<?> child : path.getChildren()) {
                if (!child.hasAttribute(AttributeKey.PERMISSIONS)) {
                    // If a child doesn't have permissions, do not set any permissions for the parent
                    thisPermissions.clear();
                    break;
                }

                thisPermissions.addAll(child.getAttributeNotNull(AttributeKey.PERMISSIONS));
                if (children.size() == 1) {
                    // We only remove the permissions if this path only has one child, meaning no cross-merging happens.
                    child.removeAttribute(AttributeKey.PERMISSIONS);
                }
            }

            if (!thisPermissions.isEmpty()) {
                path.setAttribute(AttributeKey.PERMISSIONS, thisPermissions);
            }
        }
    }

    @Override
    public MessagerWrapper delegateMessager() {
        return messager;
    }
}