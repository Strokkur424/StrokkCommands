package net.strokkur.testmod.fabric.server;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.strokkur.testmod.fabric.server.commands.FoodCommandBrigadier;

public class TestMod implements ModInitializer {
  @Override
  public void onInitialize() {
    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
      FoodCommandBrigadier.register(dispatcher, registryAccess);
    });
  }
}
