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
package net.strokkur.commands.internal.util;

import net.strokkur.commands.annotations.Permission;
import net.strokkur.commands.annotations.RequiresOP;
import net.strokkur.commands.internal.intermediate.Requirement;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface Utils {

    @Nullable
    static TypeMirror getAnnotationMirror(Element element, Class<? extends Annotation> annotationClass) {
        return getAnnotationMirror(element, annotationClass, "value");
    }

    @Nullable
    static TypeMirror getAnnotationMirror(Element element, Class<? extends Annotation> annotationClass, String fieldName) {
        String annotationName = annotationClass.getName();

        Optional<? extends AnnotationMirror> out = element.getAnnotationMirrors().stream()
            .filter(mirror -> ((TypeElement) mirror.getAnnotationType().asElement()).getQualifiedName().contentEquals(annotationName))
            .findFirst();

        return out.flatMap(mirror -> mirror.getElementValues().entrySet().stream()
                .filter(entry -> entry.getKey().getSimpleName().contentEquals(fieldName))
                .map(entry -> (TypeMirror) entry.getValue().getValue())
                .findFirst())
            .orElse(null);
    }

    @NullUnmarked
    static List<Requirement> getAnnotatedRequirements(Element element) {
        Permission permission = element.getAnnotation(Permission.class);
        RequiresOP requiresOP = element.getAnnotation(RequiresOP.class);

        List<Requirement> requirements = new ArrayList<>(2);
        if (permission != null) {
            requirements.add(Requirement.ofPermission(permission.value()));
        }
        if (requiresOP != null) {
            requirements.add(Requirement.IS_OP);
        }
        return requirements;
    }
}