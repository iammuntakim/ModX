package com.plugin.mods;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class Main implements ClientModInitializer {

    private static boolean debugMode = false;
    private static long lastToggle = 0;
    private static final Set<Integer> keys = new HashSet<>();

    @Override
    public void onInitializeClient() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null || client.player == null) return;

            long window = client.getWindow().getHandle();

            boolean f3 = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_F3);
            boolean d = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_D);

            int k = (GLFW.GLFW_KEY_F3 * 31) + GLFW.GLFW_KEY_D;

            if (f3 && d) {
                if (!keys.contains(k)) {
                    keys.add(k);
                    toggle();
                }
            } else {
                keys.remove(k);
            }
        });

        WorldRenderEvents.LAST.register(ctx -> {
            if (!debugMode) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) return;

            for (Entity e : client.world.getEntities()) {
                if (e instanceof LivingEntity && e != client.player) {
                    render(e);
                }
            }
        });
    }

    private static void toggle() {
        long now = System.currentTimeMillis();
        if (now - lastToggle < 300) return;
        lastToggle = now;
        debugMode = !debugMode;
    }

    private static void render(Entity e) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        double cx = client.gameRenderer.getCamera().getPos().x;
        double cy = client.gameRenderer.getCamera().getPos().y;
        double cz = client.gameRenderer.getCamera().getPos().z;

        Box b = e.getBoundingBox();

        double x1 = b.minX - cx;
        double y1 = b.minY - cy;
        double z1 = b.minZ - cz;

        double x2 = b.maxX - cx;
        double y2 = b.maxY - cy;
        double z2 = b.maxZ - cz;

        System.out.println(x1 + " " + y1 + " " + z1 + " " + x2 + " " + y2 + " " + z2);
    }
}