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
package xyz.jpenilla.minimotd.forge;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.minimotd.common.CommandHandler;
import xyz.jpenilla.minimotd.common.Constants;
import xyz.jpenilla.minimotd.common.MiniMOTD;
import xyz.jpenilla.minimotd.common.MiniMOTDPlatform;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

@Mod(
  modid = Constants.PluginMetadata.ID,
  name = Constants.PluginMetadata.NAME,
  version = Constants.PluginMetadata.VERSION,
  serverSideOnly = true,
  acceptableRemoteVersions = "*"
)
public final class MiniMOTDForge implements MiniMOTDPlatform<String> {
  private static MiniMOTDForge instance = null;

  private final Logger logger = LogManager.getLogger(MiniMOTD.class);
  private final Path dataDirectory = Loader.instance().getConfigDir().toPath().resolve("minimotd");
  private final MiniMOTD<String> miniMOTD;

  private MinecraftServer server;

  public MiniMOTDForge() {
    if (instance != null)
      throw new IllegalStateException("Cannot create a second instance of " + this.getClass().getName());

    instance = this;
    MinecraftForge.EVENT_BUS.register(this);
    miniMOTD = new MiniMOTD<>(this);
    this.miniMOTD.logger().info("Done initializing MiniMOTD");
  }

  public @NonNull MiniMOTD<String> miniMOTD() {
    return this.miniMOTD;
  }

  @SideOnly(Side.SERVER)
  @Mod.EventHandler
  public void serverStarting(FMLServerStartingEvent event) {
    this.server = event.getServer();
    final CommandHandler handler = new CommandHandler(instance.miniMOTD);
    event.registerServerCommand(new MiniMOTDCommand(handler));
  }

  @SideOnly(Side.SERVER)
  @Mod.EventHandler
  public void serverStopped(FMLServerStoppedEvent event) {
    this.server = null;
  }

  public static @NonNull MiniMOTDForge get() {
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
  public @NonNull String loadIcon(final @NonNull BufferedImage bufferedImage) throws Exception {
    final ByteBuf byteBuf = Unpooled.buffer();
    final String icon;
    try {
      ImageIO.write(bufferedImage, "PNG", new ByteBufOutputStream(byteBuf));
      final ByteBuffer base64 = Base64.getEncoder().encode(byteBuf.nioBuffer());
      icon = "data:image/png;base64," + StandardCharsets.UTF_8.decode(base64);
    } finally {
      byteBuf.release();
    }
    return icon;
  }
}
