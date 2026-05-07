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
package net.strokkur.testplugin.guice.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import net.strokkur.testplugin.guice.MyMagicNumber;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public class GuiceModule extends AbstractModule {
  private final JavaPlugin plugin;

  public GuiceModule(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  protected void configure() {
    bind(JavaPlugin.class).toInstance(plugin);
    bind(Logger.class).toInstance(plugin.getSLF4JLogger());
  }

  @Provides
  @MyMagicNumber
  public int provideMagicNumber() {
    return 7;
  }
}
