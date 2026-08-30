/*
 * StrokkCommands - A super simple annotation based zero-shade Paper command API library.
 * Copyright (C) 2025 Strokkur24
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see <https://www.gnu.org/licenses/>.
 */
package net.strokkur.commands.internal;

import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.exceptions.UnknownSenderException;
import net.strokkur.commands.internal.intermediate.executable.CommandParameter;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.prototype.PrototypeNode;
import net.strokkur.commands.internal.prototype.requirements.ComparableRequirement;
import net.strokkur.commands.internal.prototype.requirements.PermissionRequirement;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.expression.builder.InvocationChainBuilder;
import net.strokkur.jap.code.type.CodeClassType;
import net.strokkur.jap.source.annotation.AnnotationsHolder;
import net.strokkur.jap.source.classmodel.SourceParameterLike;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

public interface PlatformUtils {

  static PlatformUtils get() {
    class Holder {
      @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
      static final Optional<PlatformUtils> INSTANCE = ServiceLoader.load(
        PlatformUtils.class, PlatformUtils.class.getClassLoader()
      ).findFirst();
    }

    return Holder.INSTANCE.orElseThrow(() -> new RuntimeException("No PlatformUtils provider registered."));
  }

  default void populateExecutesNode(Executable executable, CommandNode rootNode, CommandNode endNode, List<CommandParameter> parameters) throws UnknownSenderException {
    // noop
  }

  default boolean mayParameterBeArgument(SourceParameterLike param) {
    return true;
  }

  default void processCommandArgument(RequiredCommandArgument commandArgument, SourceParameterLike parameter) {
    // noop
  }

  default boolean isArgumentOptional(CommandParameter param) {
    return false;
  }

  CodeClassType platformType();

  default void populateNode(AnnotationsHolder element, CommandNode node) {
    // noop
  }

  private void clearChildPermsAlreadyPresentOnParent(PrototypeNode node) {
    if (!(node.requires().get("permissions") instanceof PermissionRequirement parentPerm)) {
      return;
    }

    node.forEachChild(sub -> {
      if (sub.requires().remove("permissions") instanceof PermissionRequirement subPerm) {
        final ComparableRequirement after = subPerm.reduceBy(parentPerm);
        if (after != null) {
          sub.requires().put("permissions", after);
        }
      }
    });
  }

  /// Pre-processes a prototype node before it gets printed.
  @MustBeInvokedByOverriders
  default void preProcess(PrototypeNode node) {
    if (node.children().isEmpty()) {
      return;
    }

    // Handle permissions   --   NOTE: not all implementations must have permissions, however this is still safe
    // - if this node has permissions, first reduce all child nodes by those permissions
    // - if any child node has no permissions set, then there's nothing we should change; exit.
    // - compute the common permission nodes and add these to this node's permission set.
    // - subtract the common set from all child nodes
    // - finally, add all child permissions into a parent set, but don't modify child nodes
    final boolean oneChildHasNoPerms = node.children().stream().anyMatch(sub -> !sub.requires().containsKey("permissions"));

    clearChildPermsAlreadyPresentOnParent(node);
    if (oneChildHasNoPerms || node.executes != null) {
      // Further operations would result in blocking off permission-free paths.
      return;
    }

//    final ComparableRequirement common = ComparableRequirement.commonPartOfAll(node.children().stream()
//      .map(sub -> sub.requires().get("permissions"))
//      .toList());
//    if (common != null) {
//      final ComparableRequirement oldThis = node.requires().remove("permissions");
//      final ComparableRequirement newThis = oldThis == null ? common : oldThis.addTo(common);
//      node.requires().put("permissions", newThis);
//      node.children().forEach(sub -> {
//        final ComparableRequirement reduced = sub.requires().remove("permissions").reduceBy(common);
//        if (reduced != null) {
//          sub.requires().put(
//            "permissions",
//            reduced
//          );
//        }
//      });
//    }

    if (!node.requires().containsKey("permissions")) {
      // We still don't have any permission nodes, however the child nodes all have permissions. Construct
      // a new set with all child permissions added onto this one.
      node.requires().put("permissions", permissionRequirement(node.children().stream()
        .flatMap(sub -> Optional.ofNullable(sub.requires().get("permissions")).stream()
          .map(PermissionRequirement.class::cast)
          .flatMap(req -> req.permissions().stream()))
        .collect(Collectors.toSet())
      ));
      clearChildPermsAlreadyPresentOnParent(node);
    }
  }

  PermissionRequirement permissionRequirement(Set<String> permissions);

  InvocationChainBuilder literalBuilder(ConvertToExpression name);

  InvocationChainBuilder argumentBuilder(ConvertToExpression name, ConvertToExpression argument);
}
