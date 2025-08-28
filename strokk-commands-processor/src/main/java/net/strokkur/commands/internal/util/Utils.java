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

import org.jspecify.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
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

    static PackageElement getPackageElement(TypeElement typeElement) {
        if (typeElement.getNestingKind() == NestingKind.TOP_LEVEL) {
            return (PackageElement) typeElement.getEnclosingElement();
        }

        return getPackageElement((TypeElement) typeElement.getEnclosingElement());
    }

    static String getTypeName(Element type) {
        final StringBuilder builder = new StringBuilder();
        final List<String> names = getNestedClassNames(type);

        for (int i = 0, size = names.size(); i < size; i++) {
            final String name = names.get(i);
            builder.append(name);
            if (i + 1 < size) {
                builder.append(".");
            }
        }
        return builder.toString();
    }

    static List<String> getNestedClassNames(Element type) {
        final List<String> names = new ArrayList<>(16);

        do {
            names.add(type.getSimpleName().toString());
            type = type.getEnclosingElement();
        } while (type instanceof TypeElement);

        return names.reversed();
    }
}