package dev.testvisuals;

import net.fabricmc.api.ClientModInitializer;

import dev.testvisuals.hud.Config;
import dev.testvisuals.menu.ThemeManager;

public final class TestVisualsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Config.load();
        ThemeManager.init();
    }
}