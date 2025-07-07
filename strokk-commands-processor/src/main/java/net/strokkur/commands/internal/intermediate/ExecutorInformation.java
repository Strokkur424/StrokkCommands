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
package net.strokkur.commands.internal.intermediate;

import net.strokkur.commands.internal.arguments.ArgumentInformation;

import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record ExecutorInformation(ExecutableElement methodElement, ExecutorType type,
                                  List<ArgumentInformation> arguments, List<Requirement> requirements) {

    public ExecutorInformation prepend(List<ArgumentInformation> arguments) {
        List<ArgumentInformation> newArgumentList = new ArrayList<>(arguments);
        newArgumentList.addAll(this.arguments);
        return new ExecutorInformation(methodElement, type, newArgumentList, requirements);
    }
    
    public String methodName() {
        return methodElement.getSimpleName().toString();
    }

    @Override
    public String toString() {
        return "ExecutorInformation{" +
               "methodElement=" + methodElement +
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
        return type() == that.type() && Objects.equals(requirements(), that.requirements()) && Objects.equals(methodElement(), that.methodElement()) && Objects.equals(arguments(), that.arguments());
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodElement(), type(), arguments(), requirements());
    }
}
