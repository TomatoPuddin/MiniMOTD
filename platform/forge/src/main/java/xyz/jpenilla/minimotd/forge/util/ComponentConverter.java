package xyz.jpenilla.minimotd.forge.util;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component;

public class ComponentConverter {
  public static Component toNative(net.kyori.adventure.text.Component component) {
    return Component.Serializer.fromJson(GsonComponentSerializer.gson().serialize(component));
  }
}
