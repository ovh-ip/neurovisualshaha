package dev.testvisuals.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.testvisuals.menu.ClickGuiScreen;
import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Unique
    private static boolean testvisuals$rightShiftWasDown = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void testvisuals$tick(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        long handle = client.getWindow().getHandle();
        boolean down = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (down && !testvisuals$rightShiftWasDown && client.currentScreen == null) {
            client.setScreen(new ClickGuiScreen());
        }
        testvisuals$rightShiftWasDown = down;
    }
}