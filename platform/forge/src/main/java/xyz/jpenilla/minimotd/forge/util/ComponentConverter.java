package xyz.jpenilla.minimotd.forge.util;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.util.text.ITextComponent;

public class ComponentConverter {
  public static ITextComponent toNative(net.kyori.adventure.text.Component component) {
    return ITextComponent.Serializer.fromJsonLenient(GsonComponentSerializer.gson().serialize(component));
  }
}
