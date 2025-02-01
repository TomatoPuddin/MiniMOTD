package xyz.jpenilla.minimotd.neoforge.util;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;

public class ComponentConverter {
  public static Component toNative(net.kyori.adventure.text.Component component, HolderLookup.Provider provider) {
    return Component.Serializer.fromJson(GsonComponentSerializer.gson().serialize(component), provider);
  }
}
