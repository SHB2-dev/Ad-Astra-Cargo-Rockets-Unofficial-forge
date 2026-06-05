package uk.co.cablepost.ad_astra_cargo_rockets.launch_pad;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class LaunchPadScreen extends AbstractContainerScreen<LaunchPadMenu> {

    public LaunchPadScreen(LaunchPadMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // 18スロット(9入力+9出力) + プレイヤーインベントリ
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 背景
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF8B8B8B);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFC6C6C6);

        // スロット描画ヘルパー
        // 入力スロット (9個, 2行目)
        graphics.drawString(font, Component.translatable("gui.ad_astra_cargo_rockets.input"), x + 7, y + 17, 0x404040, false);
        for (int i = 0; i < 9; i++) {
            int sx = x + 8 + (i % 9) * 18;
            int sy = y + 28;
            drawSlotBg(graphics, sx, sy);
        }

        // 出力スロット (9個)
        graphics.drawString(font, Component.translatable("gui.ad_astra_cargo_rockets.output"), x + 7, y + 51, 0x404040, false);
        for (int i = 0; i < 9; i++) {
            int sx = x + 8 + (i % 9) * 18;
            int sy = y + 62;
            drawSlotBg(graphics, sx, sy);
        }

        // 仕切り線
        graphics.fill(x + 7, y + 86, x + imageWidth - 7, y + 87, 0xFF8B8B8B);

        // プレイヤーインベントリ (3行)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotBg(graphics, x + 8 + col * 18, y + 97 + row * 18);
            }
        }
        // ホットバー
        for (int col = 0; col < 9; col++) {
            drawSlotBg(graphics, x + 8 + col * 18, y + 169);
        }
    }

    private void drawSlotBg(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8B8B8B);
        graphics.fill(x, y, x + 16, y + 16, 0xFF000000);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 1, x + 16, y + 16, 0xFF8B8B8B);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
