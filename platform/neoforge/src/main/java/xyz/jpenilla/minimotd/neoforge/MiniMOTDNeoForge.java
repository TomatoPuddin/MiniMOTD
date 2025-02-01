/*
 * This file is part of MiniMOTD, licensed under the MIT License.
 *
 * Copyright (c) 2020-2023 Jason Penilla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.jpenilla.minimotd.neoforge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.jpenilla.minimotd.common.CommandHandler;
import xyz.jpenilla.minimotd.common.Constants;
import xyz.jpenilla.minimotd.common.MiniMOTD;
import xyz.jpenilla.minimotd.common.MiniMOTDPlatform;
import xyz.jpenilla.minimotd.neoforge.access.ServerStatusFaviconAccess;
import xyz.jpenilla.minimotd.neoforge.util.ComponentConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

import static net.minecraft.commands.Commands.literal;

@Mod(Constants.PluginMetadata.ID)
public final class MiniMOTDNeoForge implements MiniMOTDPlatform<ServerStatus.Favicon> {
  private static MiniMOTDNeoForge instance = null;

  private final Logger logger = LoggerFactory.getLogger(MiniMOTD.class);
  private final Path dataDirectory = FMLPaths.CONFIGDIR.get().resolve("MiniMOTD");
  private final MiniMOTD<ServerStatus.Favicon> miniMOTD;

  private MinecraftServer server;

  public MiniMOTDNeoForge() {
    if(FMLEnvironment.dist != Dist.DEDICATED_SERVER) {
      miniMOTD = null;
      return;
    }
    if (instance != null)
      throw new IllegalStateException("Cannot create a second instance of " + this.getClass().getName());

    instance = this;
    NeoForge.EVENT_BUS.register(this);
    miniMOTD = new MiniMOTD<>(this);
    this.miniMOTD.logger().info("Done initializing MiniMOTD");
  }

  public @NonNull MiniMOTD<ServerStatus.Favicon> miniMOTD() {
    return this.miniMOTD;
  }

  @OnlyIn(Dist.DEDICATED_SERVER)
  @SubscribeEvent
  public void serverStarting(ServerStartingEvent event) {
    this.server = event.getServer();
  }

  @OnlyIn(Dist.DEDICATED_SERVER)
  @SubscribeEvent
  public void serverStopped(ServerStoppedEvent event) {
    this.server = null;
  }

  @OnlyIn(Dist.DEDICATED_SERVER)
  @SubscribeEvent
  public void registerCommand(RegisterCommandsEvent event) {
    final class WrappingExecutor implements Command<CommandSourceStack> {
      private final CommandHandler.Executor handler;

      WrappingExecutor(final CommandHandler.@NonNull Executor handler) {
        this.handler = handler;
      }

      @Override
      public int run(final @NonNull CommandContext<CommandSourceStack> context) {
        this.handler.execute((text, success) -> {
          var source = context.getSource();
          if(source.isSilent())
            return;
          if(success && source.source.acceptsSuccess())
            source.sendSystemMessage(ComponentConverter.toNative(text, source.registryAccess()));
          else if(!success)
            source.sendFailure(ComponentConverter.toNative(text, source.registryAccess()));
        });
        return Command.SINGLE_SUCCESS;
      }
    }

    final CommandHandler handler = new CommandHandler(this.miniMOTD);
    event.getDispatcher().register(
      literal("minimotd")
        .requires(source -> source.hasPermission(4))
        .then(literal("reload").executes(new WrappingExecutor(handler::reload)))
        .then(literal("about").executes(new WrappingExecutor(handler::about)))
        .then(literal("help").executes(new WrappingExecutor(handler::help)))
    );
  }

  public static @NonNull MiniMOTDNeoForge get() {
    return instance;
  }

  public @NonNull MinecraftServer requireServer() {
    if (this.server == null) {
      throw new IllegalStateException("Server requested before started");
    }
    return this.server;
  }

  @Override
  public @NonNull Path dataDirectory() {
    return this.dataDirectory;
  }

  @Override
  public @NonNull Logger logger() {
    return this.logger;
  }

  @Override
  public ServerStatus.@NonNull Favicon loadIcon(final @NonNull BufferedImage bufferedImage) throws Exception {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "PNG", out);
    final ServerStatus.Favicon favicon = new ServerStatus.Favicon(out.toByteArray());
    ((ServerStatusFaviconAccess) (Object) favicon).cacheEncodedIcon();
    return favicon;
  }
}
