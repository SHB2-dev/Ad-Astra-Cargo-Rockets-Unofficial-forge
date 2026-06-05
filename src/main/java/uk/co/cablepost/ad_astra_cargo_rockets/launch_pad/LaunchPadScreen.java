package uk.co.cablepost.ad_astra_cargo_rockets.launch_pad;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import uk.co.cablepost.ad_astra_cargo_rockets.AdAstraCargoRockets;

public class LaunchPadScreen extends AbstractContainerScreen<LaunchPadMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            AdAstraCargoRockets.MOD_ID, "textures/gui/launch_pad.png");

    public LaunchPadScreen(LaunchPadMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 202;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Simple fallback background (grey box) – replace with a real GUI texture if available
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
    }
}
