package net.strokkur.testmod.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.strokkur.testmod.fabric.client.commands.SimpleCommandBrigadier;

public class TestModClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    ClientCommandRegistrationCallback.EVENT.register(SimpleCommandBrigadier::register);
  }
}
