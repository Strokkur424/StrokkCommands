package net.strokkur.commands.internal.prototype.requirements;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.expression.CodeExpression;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public abstract class PermissionRequirement implements ComparableRequirement {
  protected final Set<String> permissionStrings = new TreeSet<>();

  public PermissionRequirement(Set<String> permissions) {
    if (permissions.isEmpty()) {
      throw new IllegalStateException("Cannot have empty permissions.");
    }
    permissionStrings.addAll(permissions);
  }

  protected abstract ConvertToExpression permToExpr(String perm);

  @Override
  public String key() {
    return "permissions";
  }

  @UnmodifiableView
  public Set<String> permissions() {
    return Collections.unmodifiableSet(permissionStrings);
  }

  @Override
  public @Nullable ComparableRequirement commonPart(ComparableRequirement other) {
    if (!(other instanceof PermissionRequirement otherPerm)) {
      throw new IllegalStateException("Cannot compute common part between incompatible types.");
    }

    final Set<String> common = permissionStrings.stream()
      .filter(otherPerm.permissionStrings::contains)
      .collect(Collectors.toSet());
    if (common.isEmpty()) {
      return null;
    }
    return PlatformUtils.get().permissionRequirement(common);
  }

  @Override
  public ComparableRequirement addTo(ComparableRequirement other) {
    if (!(other instanceof PermissionRequirement otherPerm)) {
      throw new IllegalStateException("Cannot compute common part between incompatible types.");
    }

    final Set<String> combined = new HashSet<>(permissionStrings);
    combined.addAll(otherPerm.permissionStrings);
    return PlatformUtils.get().permissionRequirement(combined);
  }

  @Override
  public @Nullable ComparableRequirement reduceBy(ComparableRequirement common) {
    if (!(common instanceof PermissionRequirement commonPerm)) {
      throw new IllegalStateException("Cannot reduce by incompatible type.");
    }

    // The following cases can happen:
    // 1. The common permissions are the same as the ones here, in which case no need to do any additional checks.
    // 2. The common permissions may be more permissive, in which case these perms are still needed.
    // 3. The common permissions may be more restrictive, in which case these perms are not needed.
    // 4. Some checks are more permissive, but others are more restrictive; keep the subset of ones not contained in
    //    the common set.

    // Case 1:
    if (permissionStrings.size() == commonPerm.permissionStrings.size() && permissionStrings.containsAll(commonPerm.permissionStrings)) {
      return null;
    }

    // Case 2:
    if (commonPerm.permissionStrings.size() > permissionStrings.size()) {
      return PlatformUtils.get().permissionRequirement(permissionStrings);
    }

    // Case 3/4:
    final Set<String> restrictive = new HashSet<>(permissionStrings);
    restrictive.removeAll(commonPerm.permissionStrings);
    return restrictive.isEmpty() ? null : PlatformUtils.get().permissionRequirement(restrictive);
  }

  @Override
  public CodeExpression toExpression() {
    return permissionStrings.stream()
      .map(this::permToExpr)
      .reduce(ConvertToExpression::or)
      .map(ConvertToExpression::toExpression)
      .orElseThrow();
  }
}
