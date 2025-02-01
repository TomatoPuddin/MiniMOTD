package xyz.jpenilla.minimotd.neoforge.mixin;

import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Component.Serializer.class)
public class ComponentSerializerMixin {
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "com/google/gson/GsonBuilder.disableHtmlEscaping()Lcom/google/gson/GsonBuilder;", remap = false),
      remap = false)
    private static GsonBuilder adventure$injectGson(GsonBuilder builder) {
        GsonComponentSerializer.gson().populator().apply(builder);
      return builder;
    }
}
