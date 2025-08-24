package net.strokkur.commands.internal.intermediate.paths;

import net.strokkur.commands.internal.arguments.RequiredCommandArgument;

import javax.lang.model.type.TypeMirror;

public interface RecordPath extends CommandPath<RequiredCommandArgument> {
    TypeMirror getRecordType();
}