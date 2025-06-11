package net.strokkur.commands.internal.intermediate;

import net.strokkur.commands.internal.arguments.ArgumentInformation;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Objects;

public record ExecutorInformation(TypeElement classElement, ExecutableElement methodElement, ExecutorType type,
                                  List<ArgumentInformation> arguments, List<Requirement> requirements) {

    public String className() {
        return classElement.getSimpleName().toString();
    }

    public String methodName() {
        return methodElement.getSimpleName().toString();
    }

    @Override
    public String toString() {
        return "ExecutorInformation{" +
               "classElement=" + classElement +
               ", methodElement=" + methodElement +
               ", type=" + type +
               ", arguments=" + arguments +
               ", requirements=" + requirements +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExecutorInformation that = (ExecutorInformation) o;
        return type() == that.type() && Objects.equals(classElement(), that.classElement()) && Objects.equals(requirements(), that.requirements()) && Objects.equals(methodElement(), that.methodElement()) && Objects.equals(arguments(), that.arguments());
    }

    @Override
    public int hashCode() {
        return Objects.hash(classElement(), methodElement(), type(), arguments(), requirements());
    }
}
