package net.strokkur.commands.internal.intermediate.record;

import net.strokkur.commands.internal.intermediate.executable.CommandParameter;
import net.strokkur.jap.source.classmodel.SourceRecord;

import java.util.List;

public record RecordArguments(SourceRecord record, List<CommandParameter> parameters) {
  public static RecordArguments of(SourceRecord record, List<CommandParameter> params) {
    return new RecordArguments(record, params);
  }
}
