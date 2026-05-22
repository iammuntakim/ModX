package com.ma.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirJumpClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("airjump");
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private int level;
    private boolean maintainLevel = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("AirJump Client Mod initialized!");

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                tick();
            }
        }).start();
    }

    private void tick() {
        if (mc.player == null || mc.player.isOnGround()) return;

        KeyBinding jumpKey = mc.options.jumpKey;
        KeyBinding sneakKey = mc.options.sneakKey;

        if (jumpKey.isPressed()) {
            level = mc.player.getBlockPos().getY();
            mc.player.jump();
        }

        if (sneakKey.isPressed()) {
            level--;
        }

        if (maintainLevel && mc.player.getBlockPos().getY() == level && jumpKey.isPressed()) {
            mc.player.jump();
        }
    }
}
