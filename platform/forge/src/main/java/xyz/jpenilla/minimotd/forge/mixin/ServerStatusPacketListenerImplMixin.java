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
package xyz.jpenilla.minimotd.forge.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.status.ServerStatusNetHandler;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.jpenilla.minimotd.common.MiniMOTD;
import xyz.jpenilla.minimotd.common.PingResponse;
import xyz.jpenilla.minimotd.common.config.MiniMOTDConfig;
import xyz.jpenilla.minimotd.forge.MiniMOTDForge;
import xyz.jpenilla.minimotd.forge.util.ComponentConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

@Mixin(ServerStatusNetHandler.class)
abstract class ServerStatusPacketListenerImplMixin {

  @Redirect(
    method = "handleStatusRequest",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getStatus()Lnet/minecraft/network/ServerStatusResponse;")
  )
  public ServerStatusResponse injectHandleStatusRequest(final MinecraftServer server) {
    try {
      final MiniMOTDForge miniMOTDForge = MiniMOTDForge.get();
      final ServerStatusResponse status = Objects.requireNonNull(server.getStatus(), "status");

      final MiniMOTD<String> miniMOTD = miniMOTDForge.miniMOTD();
      final MiniMOTDConfig config = miniMOTD.configManager().mainConfig();

      final PingResponse<String> response = miniMOTD.createMOTD(
        config,
        server.getPlayerCount(),
        server.getMaxPlayers()
      );

      response.motd(motd -> {
        status.setDescription(ComponentConverter.toNative(motd));
      });
      response.icon(status::setFavicon);

      if (response.hidePlayerCount()) {
        status.setPlayers(null);
      } else {
        final ServerStatusResponse.Players newPlayers = new ServerStatusResponse.Players(
          response.playerCount().maxPlayers(),
          response.playerCount().onlinePlayers());
        if (!response.disablePlayerListHover()) {
          ArrayList<PlayerEntity> players = new ArrayList<>(server.getPlayerList().getPlayers());
          Collections.shuffle(players);

          GameProfile[] gameProfiles = players.stream().map(PlayerEntity::getGameProfile).limit(12).toArray(GameProfile[]::new);
          newPlayers.setSample(gameProfiles);
        }
        status.setPlayers(newPlayers);
      }

      return status;
    } catch (Exception e) {
      MiniMOTDForge.get().logger().error("Error processing motd", e);
      throw e;
    }
  }
}
