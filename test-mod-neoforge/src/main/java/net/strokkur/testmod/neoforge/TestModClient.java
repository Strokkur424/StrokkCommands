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
package net.strokkur.testmod.neoforge;

import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@Mod(value = "testmod", dist = Dist.CLIENT)
public class TestModClient {
  public TestModClient() {
    TestMod.LOGGER.info("Loaded client mod!");
  }

  @SubscribeEvent
  void registerClientCommands(final RegisterClientCommandsEvent event) {
    event.getDispatcher().register(Commands.literal("client-cmd")
        .executes(ctx -> Command.SINGLE_SUCCESS)
    );
  }
}
