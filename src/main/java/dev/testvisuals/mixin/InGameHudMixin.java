package dev.testvisuals.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.testvisuals.hud.HudManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void testvisuals$renderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        // Draw vanilla DrawContext buffer first so hotbar, health, armor, crosshair are pristine
        context.draw();
        HudManager.get().render(tickCounter.getTickDelta(false));
    }
}