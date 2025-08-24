package net.strokkur.commands.internal.intermediate.paths;

import net.strokkur.commands.internal.arguments.CommandArgument;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public class RecordPathImpl extends SimpleCommandPathImpl<CommandArgument> implements RecordPath {

    private final TypeMirror recordType;

    public RecordPathImpl(final List<CommandArgument> arguments, final TypeMirror recordType) {
        super(arguments);
        this.recordType = recordType;
    }

    @Override
    SimpleCommandPathImpl<CommandArgument> createLeftSplit(final List<CommandArgument> args) {
        return new RecordPathImpl(args, recordType);
    }

    @Override
    public TypeMirror getRecordType() {
        return recordType;
    }
}
