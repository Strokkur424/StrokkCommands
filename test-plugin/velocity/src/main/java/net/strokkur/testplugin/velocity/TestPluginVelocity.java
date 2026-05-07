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
package net.strokkur.testplugin.velocity;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.strokkur.testplugin.velocity.reference.TestCommandBrigadier;
import org.slf4j.Logger;

import javax.inject.Inject;

@Plugin(
    id = "strokkcommands-testplugin",
    name = "TestPlugin",
    version = "1.0.0",
    url = "https://commands.strokkur.net",
    authors = "Strokkur24",
    description = "The Velocity test plugin for StrokkCommands (Velocity)"
)
public class TestPluginVelocity {
  private final ProxyServer proxy;
  private final Logger logger;

  @Inject
  public TestPluginVelocity(ProxyServer proxy, Logger logger) {
    this.proxy = proxy;
    this.logger = logger;
  }

  @Subscribe
  void onProxyInitialize(ProxyInitializeEvent event) {
    final Injector injector = Guice.createInjector(new InjectionModule());
    injector.getInstance(TestCommandBrigadier.class).register(this.proxy, this);
  }

  private class InjectionModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(ProxyServer.class).toInstance(proxy);
      bind(Logger.class).toInstance(logger);
      bind(TestPluginVelocity.class).toInstance(TestPluginVelocity.this);
    }
  }

  public Logger logger() {
    return logger;
  }
}
