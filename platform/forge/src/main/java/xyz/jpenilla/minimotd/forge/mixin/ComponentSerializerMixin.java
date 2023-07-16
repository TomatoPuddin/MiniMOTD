package xyz.jpenilla.minimotd.forge.mixin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ITextComponent.Serializer.class)
public class ComponentSerializerMixin {

    @Inject(method = "*()Lcom/google/gson/Gson;", at = @At(value = "INVOKE_ASSIGN", target = "com/google/gson/GsonBuilder.disableHtmlEscaping()Lcom/google/gson/GsonBuilder;", remap = false),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION, remap = false)
    private static void adventure$injectGson(final CallbackInfoReturnable<Gson> cir, final GsonBuilder gson) {
        GsonComponentSerializer.gson().populator().apply(gson);
    }
}
