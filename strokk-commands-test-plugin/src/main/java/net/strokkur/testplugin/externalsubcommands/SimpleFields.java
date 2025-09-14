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
package net.strokkur.testplugin.externalsubcommands;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Subcommand;

@Command("simplefields")
class SimpleFields {

    @Subcommand("hello")
    ExternalSimpleSubBlueprint hello = new ExternalSimpleSubBlueprint("Hey <sender>, how are you?");

    @Subcommand("weather")
    ExternalSimpleSubBlueprint weather = new ExternalSimpleSubBlueprint("The weather is nice today, isn't it?");

    @Subcommand("balance")
    ExternalSimpleSubBlueprint balance = new ExternalSimpleSubBlueprint("You're balance is: <green>$0</green>. Unfortunate.");

    @Subcommand("default")
    ExternalSimpleSubBlueprint defaultSub;
}
