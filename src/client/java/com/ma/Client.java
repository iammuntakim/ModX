Package com.ma.client;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraft.network.chat.Component;
public class Client implements ClientModInitializer {
private static KeyMapping toggleKey;
private static boolean displayHud = true;
@Override
public void onInitializeClient() {
toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
"key.ma_client.toggle",
InputConstants.Type.KEYSYM,
GLFW.GLFW_KEY_H,
"category.ma_client.general"
));
HudElementRegistry.register(
ResourceLocation.fromNamespaceAndPath("ma_client", "hud_overlay"),
Client::drawHudOverlay
);
}
private static void drawHudOverlay(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker deltaTracker) {
Minecraft client = Minecraft.getInstance();
if (client.player == null || client.options.hideGui || !displayHud) {
return;
}
while (toggleKey.consumeClick()) {
displayHud = !displayHud;
}
graphics.drawString(
client.font,
"Client Active",
10,
10,
0xFFFFFF,
true
);
}
}