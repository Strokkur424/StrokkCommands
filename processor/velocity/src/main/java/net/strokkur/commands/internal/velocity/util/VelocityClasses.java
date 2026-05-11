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
package net.strokkur.commands.internal.velocity.util;

import net.strokkur.commands.internal.codegen.CodeType;
import net.strokkur.commands.internal.codegen.as.AsCodeType;
import net.strokkur.commands.internal.util.Classes;

import java.util.Arrays;

public enum VelocityClasses implements AsCodeType<CodeType.ClassType> {
  COMMAND_SOURCE("com.velocitypowered.api.command.CommandSource"),
  PLAYER("com.velocitypowered.api.proxy.Player"),
  CONSOLE_COMMAND_SOURCE("com.velocitypowered.api.proxy.ConsoleCommandSource"),

  BRIGADIER_COMMAND("com.velocitypowered.api.command.BrigadierCommand"),
  COMMAND_META("com.velocitypowered.api.command.CommandMeta"),

  PROXY_INITIALIZE_EVENT("com.velocitypowered.api.event.proxy.ProxyInitializeEvent"),
  PROXY_SERVER("com.velocitypowered.api.proxy.ProxyServer"),

  TYPED_LITERAL_COMMAND_NODE(Classes.LITERAL_COMMAND_NODE, COMMAND_SOURCE);

  private final CodeType.ClassType classType;

  VelocityClasses(String fqn) {
    this.classType = CodeType.ofClass(fqn);
  }

  @SafeVarargs
  VelocityClasses(AsCodeType<CodeType.ClassType> base, AsCodeType<CodeType.ClassType>... types) {
    this.classType = CodeType.ofClassTyped(base.getAsCodeType().codeClass(), Arrays.stream(types)
        .map(AsCodeType::getAsCodeType)
        .toArray(CodeType[]::new)
    );
  }

  @Override
  public CodeType.ClassType getAsCodeType() {
    return classType;
  }
}
