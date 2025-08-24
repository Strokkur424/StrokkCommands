package net.strokkur.commands.internal.intermediate.paths;

import net.strokkur.commands.internal.arguments.RequiredCommandArgument;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public class RecordPathImpl extends SimpleCommandPathImpl<RequiredCommandArgument> implements RecordPath {

    private final TypeMirror recordType;

    public RecordPathImpl(final List<RequiredCommandArgument> arguments, final TypeMirror recordType) {
        super(arguments);
        this.recordType = recordType;
    }

    @Override
    SimpleCommandPathImpl<RequiredCommandArgument> createLeftSplit(final List<RequiredCommandArgument> args) {
        return new RecordPathImpl(args, recordType);
    }

    @Override
    public TypeMirror getRecordType() {
        return recordType;
    }
}
