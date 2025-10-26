package net.strokkur.commands.internal.util;

import net.strokkur.commands.internal.abstraction.SourceParameter;

import java.util.function.Function;

public record PrintParamsHolder(
    String createJdParams,
    String createParams,
    String createParamNames,
    String registerJdParams,
    String registerParams,
    Function<SourceParameter, String> callTransform
) {
}
