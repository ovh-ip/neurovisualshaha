package dev.testvisuals.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.testvisuals.menu.CustomMainMenu;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void testvisuals$init(CallbackInfo ci) {
        CustomMainMenu.setParentScreen((Screen) (Object) this);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void testvisuals$render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        CustomMainMenu.render(mouseX, mouseY, delta);
        ci.cancel();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void testvisuals$mouseClicked(double mouseX, double mouseY, int button,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (CustomMainMenu.onClick(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }
}