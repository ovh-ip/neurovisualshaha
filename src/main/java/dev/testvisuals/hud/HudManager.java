package dev.testvisuals.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import dev.testvisuals.font.CustomFontRenderer;
import dev.testvisuals.hud.components.EffectsHud;
import dev.testvisuals.hud.components.KeybindHud;
import dev.testvisuals.hud.components.NotificationHud;
import dev.testvisuals.hud.components.TargetHud;
import dev.testvisuals.hud.components.TopInfoHud;
import dev.testvisuals.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;

public final class HudManager {

    private static HudManager instance;

    private Renderer2D renderer;
    private CustomFontRenderer font;
    private boolean initialized;

    private final List<HudComponent> components = new ArrayList<>();
    private HudComponent dragging;

    public static HudManager get() {
        if (instance == null) {
            instance = new HudManager();
        }
        return instance;
    }

    private HudManager() {
    }

    public Renderer2D renderer() {
        ensureInitialized();
        return renderer;
    }

    public CustomFontRenderer font() {
        ensureInitialized();
        return font;
    }

    public List<HudComponent> components() {
        return components;
    }

    private void ensureInitialized() {
        if (initialized) {
            return;
        }
        initialized = true;
        renderer = new Renderer2D();
        font = CustomFontRenderer.get();
        components.add(new TopInfoHud());
        components.add(new EffectsHud());
        components.add(new KeybindHud());
        components.add(new TargetHud());
        components.add(new NotificationHud());
        loadPositions();
    }

    public void render(float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }
        ensureInitialized();

        boolean editMode = client.currentScreen instanceof ChatScreen;
        handleDragging(editMode);

        float screenWidth = client.getWindow().getScaledWidth();
        float screenHeight = client.getWindow().getScaledHeight();

        dev.testvisuals.gl.GLUtil.enableBlend();
        dev.testvisuals.gl.GLUtil.disableDepth();
        renderer.begin(screenWidth, screenHeight);
        renderer.pushScissor(0f, 0f, screenWidth, screenHeight);

        List<HudComponent> sorted = new ArrayList<>(components);
        sorted.sort(Comparator.comparing(c -> c == dragging ? 1 : 0));

        for (HudComponent component : sorted) {
            if (!component.enabled) {
                continue;
            }
            component.render(renderer, screenWidth, screenHeight, delta, editMode);
            if (editMode) {
                renderEditOverlay(component);
            }
        }
        renderer.flush();

        renderer.popScissor();
        renderer.flush();
        dev.testvisuals.gl.GLUtil.restoreState();
    }

    private void renderEditOverlay(HudComponent component) {
        int color = component == dragging ? HudStyle.DRAG_HIGHLIGHT : HudStyle.BORDER;
        renderer.roundedOutline(component.screenX, component.screenY,
                component.getWidth(), component.getHeight(), 4f, 1f, color);
        font.draw(renderer, component.getDisplayName(),
                component.screenX + 4f, component.screenY + 2f, 0.5f, HudStyle.TEXT_DIM);
    }

    private void handleDragging(boolean editMode) {
        MinecraftClient client = MinecraftClient.getInstance();
        long handle = client.getWindow().getHandle();
        boolean leftDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;

        float mouseX = (float) (client.mouse.getX() * client.getWindow().getScaledWidth() / client.getWindow().getWidth());
        float mouseY = (float) (client.mouse.getY() * client.getWindow().getScaledHeight() / client.getWindow().getHeight());

        if (!editMode) {
            if (dragging != null) {
                dragging = null;
            }
            return;
        }

        if (leftDown && dragging == null) {
            for (int i = components.size() - 1; i >= 0; i--) {
                HudComponent component = components.get(i);
                if (component.enabled && component.contains(mouseX, mouseY)) {
                    dragging = component;
                    break;
                }
            }
        } else if (dragging != null && !leftDown) {
            dragging = null;
            savePositions();
        }

        if (dragging != null) {
            dragging.dragTo(mouseX, mouseY,
                    client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        }
    }

    public void toggle(String id) {
        for (HudComponent component : components) {
            if (component.getId().equals(id)) {
                component.enabled = !component.enabled;
                savePositions();
                return;
            }
        }
    }

    public boolean isEnabled(String id) {
        for (HudComponent component : components) {
            if (component.getId().equals(id)) {
                return component.enabled;
            }
        }
        return false;
    }

    public void resetPositions() {
        components.forEach(component -> component.position = new HudPosition());
        savePositions();
    }

    public void notify(String text) {
        ensureInitialized();
        for (HudComponent component : components) {
            if (component instanceof NotificationHud notificationHud) {
                notificationHud.push(text);
                return;
            }
        }
    }

    private void loadPositions() {
        for (HudComponent component : components) {
            Config.ComponentData data = Config.components().get(component.getId());
            if (data == null) {
                continue;
            }
            component.enabled = data.enabled;
            component.position.anchor = parseAnchor(data.anchor);
            component.position.offsetX = data.offsetX;
            component.position.offsetY = data.offsetY;
        }
    }

    private void savePositions() {
        for (HudComponent component : components) {
            Config.ComponentData data = new Config.ComponentData();
            data.enabled = component.enabled;
            data.anchor = component.position.anchor.name();
            data.offsetX = component.position.offsetX;
            data.offsetY = component.position.offsetY;
            Config.components().put(component.getId(), data);
        }
        Config.save();
    }

    private static Anchor parseAnchor(String name) {
        try {
            return Anchor.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Anchor.TOP_LEFT;
        }
    }
}