package com.plugin.mods;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class Main implements ClientModInitializer {

    private static boolean enabled = false;
    private static long last = 0;
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

        WorldRenderEvents.LAST.register(context -> {
            if (!enabled) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) return;

            Vec3d cam = context.camera().getPos();

            for (Entity e : client.world.getEntities()) {

                if (!(e instanceof LivingEntity)) continue;
                if (e == client.player) continue;

                Box box = e.getBoundingBox().offset(-cam.x, -cam.y, -cam.z);

                draw(box);
            }
        });
    }

    private static void toggle() {
        long now = System.currentTimeMillis();
        if (now - last < 250) return;
        last = now;
        enabled = !enabled;
    }

    private static void draw(Box box) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        VertexConsumerProvider.Immediate immediate =
                client.getBufferBuilders().getEntityVertexConsumers();

        VertexConsumer vc = immediate.getBuffer(RenderLayer.getLines());

        float r = 1f;
        float g = 0.2f;
        float b = 0.2f;
        float a = 1f;

        WorldRenderer.drawBox(
                vc,
                box.minX, box.minY, box.minZ,
                box.maxX, box.maxY, box.maxZ,
                r, g, b, a
        );

        immediate.draw();
    }
}