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
