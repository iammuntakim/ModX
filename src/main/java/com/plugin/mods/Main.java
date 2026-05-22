package com.plugin.mods;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main implements ClientModInitializer {

    private static final Config CONFIG = new Config();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static KeyMapping exitKeyMapping;

    @Override
    public void onInitializeClient() {
        CONFIG.load(Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("mods_exit.json"));
        
        exitKeyMapping = new KeyMapping(
                "key.mods.exit_app",
                InputConstants.Type.KEYSYM,
                CONFIG.isComboMode ? GLFW.GLFW_KEY_E : CONFIG.singleKey,
                "key.categories.ui"
        );
    }

    public static void client_tick(Minecraft client) {
        if (client.player == null) return;

        com.mojang.blaze3d.platform.Window window = client.getWindow();
        
        if (CONFIG.isComboMode) {
            boolean isF3Down = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_F3);
            boolean isEDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_E);
            
            if (isF3Down && isEDown) {
                exitApplication(client);
            }
        } else {
            while (exitKeyMapping.consumeClick()) {
                exitApplication(client);
            }
        }
    }

    private static void exitApplication(Minecraft client) {
        client.stop();
        System.exit(0);
    }

    public static Screen createSettingsScreen(Screen parent) {
        return new ExitSettingsScreen(parent);
    }

    public static KeyMapping getExitKeyMapping() {
        return exitKeyMapping;
    }

    private static class Config {
        private transient Path path;
        public boolean isComboMode = true;
        public int singleKey = GLFW.GLFW_KEY_UNKNOWN;

        private void load(Path path) {
            this.path = path;
            if (!Files.exists(path)) {
                save();
                return;
            }
            try (Reader reader = Files.newBufferedReader(path)) {
                Config loaded = GSON.fromJson(reader, Config.class);
                if (loaded != null) {
                    isComboMode = loaded.isComboMode;
                    singleKey = loaded.singleKey;
                }
            } catch (IOException | RuntimeException ignored) {
                save();
            }
        }

        private void save() {
            if (path == null) return;
            try {
                Files.createDirectories(path.getParent());
                try (Writer writer = Files.newBufferedWriter(path)) {
                    GSON.toJson(this, writer);
                }
            } catch (IOException ignored) {
            }
        }
    }

    private static class ExitSettingsScreen extends Screen {
        private final Screen parent;
        private boolean waitingForKey = false;

        private ExitSettingsScreen(Screen parent) {
            super(Component.literal("Emergency Exit Settings"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int centerX = width / 2;
            int y = height / 2 - 40;

            addRenderableWidget(Button.builder(getModeLabel(), button -> {
                CONFIG.isComboMode = !CONFIG.isComboMode;
                CONFIG.save();
                button.setMessage(getModeLabel());
                recreateControls();
            }).bounds(centerX - 100, y, 200, 20).build());

            if (!CONFIG.isComboMode) {
                addRenderableWidget(Button.builder(getKeyLabel(), button -> {
                    waitingForKey = true;
                    button.setMessage(Component.literal("> Press Any Key <"));
                }).bounds(centerX - 100, y + 24, 200, 20).build());
            }

            addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                    .bounds(centerX - 100, y + 56, 200, 20).build());
        }

        private void recreateControls() {
            clearWidgets();
            init();
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (waitingForKey) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    CONFIG.singleKey = GLFW.GLFW_KEY_UNKNOWN;
                } else {
                    CONFIG.singleKey = keyCode;
                }
                exitKeyMapping.setKey(InputConstants.Type.KEYSYM.getOrCreate(CONFIG.singleKey));
                CONFIG.save();
                waitingForKey = false;
                recreateControls();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
            super.extractRenderState(graphics, mouseX, mouseY, tickDelta);
            int centerX = width / 2;
            int y = height / 2 - 40;
            graphics.centeredText(font, title, centerX, y - 24, 0xFFFFFF);
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }

        @Override
        public void onClose() {
            if (minecraft != null) {
                minecraft.setScreen(parent);
            }
        }

        private Component getModeLabel() {
            return Component.literal("Exit Mode: " + (CONFIG.isComboMode ? "F3 + E Combo" : "Custom Single Key"));
        }

        private Component getKeyLabel() {
            String keyName = CONFIG.singleKey == GLFW.GLFW_KEY_UNKNOWN ? "NONE" : InputConstants.Type.KEYSYM.getOrCreate(CONFIG.singleKey).getDisplayName().getString();
            return Component.literal("Current Key: " + keyName);
        }
    }
}
