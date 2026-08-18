package dev.testvisuals.menu;

import java.util.Optional;
import java.util.UUID;

import dev.testvisuals.hud.Config;
import dev.testvisuals.mixin.SessionAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import net.minecraft.util.Uuids;

public final class AltManager {

    private AltManager() {
    }

    public static void use(int index) {
        if (index < 0 || index >= Config.alts().size()) {
            return;
        }
        String name = Config.alts().get(index).name;
        if (name == null || name.isBlank()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        UUID uuid = Uuids.getOfflinePlayerUuid(name);
        Session session = new Session(name, uuid, "0", Optional.empty(), Optional.empty(),
                Session.AccountType.MOJANG);
        ((SessionAccessor) (Object) client).setSession(session);
        if (client.getNetworkHandler() != null) {
            client.disconnect();
        }
        if (client.currentScreen != null) {
            client.setScreen(null);
        }
    }

    public static boolean contains(String name) {
        for (Config.AltData alt : Config.alts()) {
            if (alt.name != null && alt.name.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static void add(String name) {
        if (name == null || name.isBlank() || contains(name)) {
            return;
        }
        Config.addAlt(name.trim());
    }

    public static void remove(int index) {
        Config.removeAlt(index);
    }
}