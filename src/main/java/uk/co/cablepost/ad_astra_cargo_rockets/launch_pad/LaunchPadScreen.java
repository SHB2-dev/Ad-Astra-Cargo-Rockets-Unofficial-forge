package uk.co.cablepost.ad_astra_cargo_rockets.launch_pad;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class LaunchPadScreen extends AbstractContainerScreen<LaunchPadMenu> {

    static final int SLOT0_Y      = 18;
    static final int SLOT1_Y      = 36;
    static final int PLAYER_INV_Y = 130;
    static final int HOTBAR_Y     = 188;
    static final int IMAGE_H      = 213;

    private static final int BLANK_TOP = 54;
    private static final int PROG_Y    = 110;
    private static final int INV_LBL_Y = 120;

    private static final int COL_BG    = 0xFFC6C6C6;
    private static final int COL_DARK  = 0xFF555555;
    private static final int COL_LIGHT = 0xFFFFFFFF;
    private static final int COL_SLOT  = 0xFF8B8B8B;

    public LaunchPadScreen(LaunchPadMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth      = 176;
        this.imageHeight     = IMAGE_H;
        this.titleLabelX     = 8;
        this.titleLabelY     = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = INV_LBL_Y;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int ox = (width  - imageWidth)  / 2;
        int oy = (height - imageHeight) / 2;
        int r  = ox + imageWidth;

        g.fill(ox, oy, r, oy + IMAGE_H, COL_DARK);
        g.fill(ox+1, oy+1, r-1, oy+IMAGE_H-1, COL_BG);
        g.fill(ox+1, oy+16, r-1, oy+17, COL_DARK);

        drawSlotGrid(g, ox+8, oy+SLOT0_Y, 9, 1);
        drawSlotGrid(g, ox+8, oy+SLOT1_Y, 9, 1);

        int barH    = PROG_Y - BLANK_TOP - 8;
        int barTopY = oy + BLANK_TOP + 4;
        int SEGMENTS = 10;

        // エネルギーバー (右端) 10分割
        renderSegmentBar(g, ox + imageWidth - 22, barTopY, 12, barH,
                menu.getEnergy(), menu.getMaxEnergy(), 0xFFDD2200, SEGMENTS);

        // 燃料バー (エネルギーの左) 10分割
        renderSegmentBar(g, ox + imageWidth - 38, barTopY, 12, barH,
                menu.getFuel(), menu.getMaxFuel(), 0xFF00AADD, SEGMENTS);

        // 貨物液体バー (燃料の左) 10分割
        renderSegmentBar(g, ox + imageWidth - 54, barTopY, 12, barH,
                menu.getCargoFluid(), menu.getMaxCargoFluid(), 0xFF00DD88, SEGMENTS);

        // テキスト
        int tx = ox + 8;
        int ty = oy + BLANK_TOP + 4;
        g.drawString(font, "Energy:", tx, ty,      0x404040, false);
        g.drawString(font, formatVal(menu.getEnergy()) + " / " + formatVal(menu.getMaxEnergy()) + " FE",
                tx, ty + 10, 0x404040, false);
        g.drawString(font, "Fuel:",   tx, ty + 22, 0x404040, false);
        g.drawString(font, menu.getFuel() + " / " + menu.getMaxFuel() + " mB",
                tx, ty + 32, 0x404040, false);
        g.drawString(font, "Cargo:",  tx, ty + 44, 0x404040, false);
        g.drawString(font, menu.getCargoFluid() + " / " + menu.getMaxCargoFluid() + " mB",
                tx, ty + 54, 0x404040, false);

        drawSlotGrid(g, ox+8, oy+PLAYER_INV_Y, 9, 3);
        drawSlotGrid(g, ox+8, oy+HOTBAR_Y,     9, 1);
    }

    /**
     * 10分割セグメントバー（下から上に充填、セグメント間に区切り線）
     */
    private void renderSegmentBar(GuiGraphics g, int barX, int barY, int barW, int barH,
                                   int value, int max, int color, int segments) {
        // 外枠
        g.fill(barX-1, barY-1, barX+barW+1, barY+barH+1, COL_DARK);
        g.fill(barX, barY, barX+barW, barY+barH, 0xFF1A1A1A);

        if (max <= 0) return;

        // 充填量（ピクセル）
        int filled = (int)((long)value * barH / max);

        // 充填部分を描画
        if (filled > 0) {
            g.fill(barX, barY + barH - filled, barX + barW, barY + barH, color);
        }

        // セグメント区切り線（暗い線をオーバーレイ）
        for (int s = 1; s < segments; s++) {
            int lineY = barY + barH - (barH * s / segments);
            g.fill(barX, lineY, barX + barW, lineY + 1, 0xFF000000);
        }
    }

    private static String formatVal(int v) {
        if (v >= 1_000_000) return String.format("%.2fM", v / 1_000_000.0);
        if (v >= 1_000)     return String.format("%.1fk", v / 1_000.0);
        return String.valueOf(v);
    }

    private void drawSlotGrid(GuiGraphics g, int x, int y, int cols, int rows) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int sx = x + col * 18, sy = y + row * 18;
                g.fill(sx-1, sy-1, sx+17, sy+17, COL_BG);
                g.fill(sx-1, sy-1, sx+16, sy,    COL_DARK);
                g.fill(sx-1, sy-1, sx,    sy+16, COL_DARK);
                g.fill(sx+16, sy,  sx+17, sy+17, COL_LIGHT);
                g.fill(sx,   sy+16, sx+17, sy+17, COL_LIGHT);
                g.fill(sx, sy, sx+16, sy+16, COL_SLOT);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title,                titleLabelX,     titleLabelY,  0x404040, false);
        g.drawString(font, "Progress: 0/0",      8,               PROG_Y,       0x404040, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, INV_LBL_Y,   0x404040, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
