package net.strokkur.commands.internal.intermediate.paths;

import net.strokkur.commands.internal.arguments.CommandArgument;

import javax.lang.model.type.TypeMirror;

public interface RecordPath extends CommandPath<CommandArgument> {
    TypeMirror getRecordType();
}