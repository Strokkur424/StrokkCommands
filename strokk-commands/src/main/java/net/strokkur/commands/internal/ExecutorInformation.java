package net.strokkur.commands.internal;

import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

record ExecutorInformation(TypeElement classElement, ExecutableElement methodElement, ExecutorType type, String @Nullable [] initialLiterals,
                           List<ArgumentInformation> arguments, List<Requirement> requirements) {

    public String className() {
        return classElement.getSimpleName().toString();
    }

    public String methodName() {
        return methodElement.getSimpleName().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExecutorInformation that = (ExecutorInformation) o;
        return type() == that.type() && Objects.equals(classElement(), that.classElement()) && Objects.equals(requirements(), that.requirements()) && Objects.equals(methodElement(), that.methodElement()) && Objects.deepEquals(initialLiterals(), that.initialLiterals()) && Objects.equals(arguments(), that.arguments());
    }

    @Override
    public int hashCode() {
        return Objects.hash(classElement(), methodElement(), type(), Arrays.hashCode(initialLiterals()), arguments(), requirements());
    }

    @Override
    public String toString() {
        return "ExecutorInformation{" +
               "classElement=" + classElement +
               ", methodElement=" + methodElement +
               ", type=" + type +
               ", initialLiterals=" + Arrays.toString(initialLiterals) +
               ", arguments=" + arguments +
               ", requirements=" + requirements +
               '}';
    }
}
