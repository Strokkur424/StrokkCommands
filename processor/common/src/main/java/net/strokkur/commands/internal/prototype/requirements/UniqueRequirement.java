package net.strokkur.commands.internal.prototype.requirements;

import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.expression.CodeExpression;
import org.jspecify.annotations.Nullable;

record UniqueRequirement(String key, Object unique, ConvertToExpression expr) implements ComparableRequirement {

  private boolean isSame(ComparableRequirement other) {
    return other instanceof UniqueRequirement(String otherKey, Object otherUnique, ConvertToExpression otherExpr)
      && unique.equals(otherUnique);
  }

  @Override
  public @Nullable ComparableRequirement reduceBy(ComparableRequirement common) {
    return this.isSame(common) ? null : this;
  }

  @Override
  public ComparableRequirement addTo(ComparableRequirement other) {
    throw new UnsupportedOperationException("Cannot add unique requirements together");
  }

  @Override
  public @Nullable ComparableRequirement commonPart(ComparableRequirement other) {
    return isSame(other) ? this : null;
  }

  @Override
  public CodeExpression toExpression() {
    return expr.toExpression();
  }
}
