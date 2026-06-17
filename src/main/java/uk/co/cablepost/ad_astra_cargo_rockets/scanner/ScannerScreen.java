package uk.co.cablepost.ad_astra_cargo_rockets.scanner;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ScannerScreen extends Screen {

    private static final int WIDTH = 360;
    private static final int HEIGHT = 220;
    private static final int LIST_WIDTH = 140;
    private static final int ROW_HEIGHT = 16;

    private List<RocketInfo> rockets = new ArrayList<>();
    private RocketInfo selected = null;
    private int scrollOffset = 0;
    private EditBox nameBox;
    private Button renameButton;
    private long lastRequestTime = 0;

    public ScannerScreen() {
        super(Component.literal("Rocket Scanner"));
    }

    /** アイテム右クリック時に呼ばれる: 画面を開く（初回のリクエストはrender()の自動更新ロジックに任せる） */
    public static void openAndRequest() {
        ScannerScreen screen = new ScannerScreen();
        Minecraft.getInstance().setScreen(screen);
    }

    @Override
    protected void init() {
        super.init();
        int ox = (width - WIDTH) / 2;
        int oy = (height - HEIGHT) / 2;

        nameBox = new EditBox(font, ox + LIST_WIDTH + 20, oy + HEIGHT - 30, 130, 16, Component.literal("name"));
        nameBox.setMaxLength(32);
        addRenderableWidget(nameBox);

        renameButton = Button.builder(Component.literal("Rename"), b -> doRename())
                .bounds(ox + LIST_WIDTH + 155, oy + HEIGHT - 31, 50, 18)
                .build();
        addRenderableWidget(renameButton);

        addRenderableWidget(Button.builder(Component.literal("Refresh"), b -> refresh())
                .bounds(ox + 8, oy + HEIGHT - 24, LIST_WIDTH - 8, 16)
                .build());

        updateSelectionFields();
    }

    private void refresh() {
        ScannerNetwork.CHANNEL.sendToServer(new RequestRocketListPacket());
        lastRequestTime = System.currentTimeMillis();
    }

    private void doRename() {
        if (selected == null) return;
        ScannerNetwork.CHANNEL.sendToServer(new RenameRocketPacket(selected.entityId, nameBox.getValue()));
        selected.name = nameBox.getValue();
    }

    /** ネットワークパケット受信時にサーバーから呼ばれる (RocketListResponsePacket経由) */
    public void updateRocketList(List<RocketInfo> newList) {
        // 選択中のロケットがまだリストにあれば選択を維持する
        Integer keepId = selected != null ? selected.entityId : null;
        this.rockets = newList;
        this.selected = null;
        if (keepId != null) {
            for (RocketInfo r : rockets) {
                if (r.entityId == keepId) { selected = r; break; }
            }
        }
        updateSelectionFields();
    }

    private void updateSelectionFields() {
        if (nameBox != null) {
            nameBox.setValue(selected != null ? selected.name : "");
        }
        if (renameButton != null) {
            renameButton.active = selected != null;
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // 2秒ごとに自動更新（手動Refreshボタンも併用可）
        long now = System.currentTimeMillis();
        if (now - lastRequestTime > 2000) {
            refresh();
        }

        renderBackground(g);
        int ox = (width - WIDTH) / 2;
        int oy = (height - HEIGHT) / 2;

        // パネル背景
        g.fill(ox, oy, ox + WIDTH, oy + HEIGHT, 0xFF1A1A1A);
        g.fill(ox, oy, ox + WIDTH, oy + 20, 0xFF2A2A2A);
        g.drawString(font, "Rocket Scanner", ox + 8, oy + 6, 0xFFFFFF, false);

        // 左: ロケット一覧
        drawRocketList(g, ox, oy, mouseX, mouseY);

        // 右: 詳細パネル
        drawDetailPanel(g, ox, oy);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawRocketList(GuiGraphics g, int ox, int oy, int mouseX, int mouseY) {
        int listX = ox + 4;
        int listY = oy + 24;
        int listH = HEIGHT - 24 - 28;

        g.fill(listX, listY, listX + LIST_WIDTH, listY + listH, 0xFF0F0F0F);

        if (rockets.isEmpty()) {
            g.drawString(font, "No rockets found", listX + 4, listY + 6, 0xAAAAAA, false);
            return;
        }

        int rowY = listY;
        for (RocketInfo r : rockets) {
            if (rowY + ROW_HEIGHT > listY + listH) break;
            boolean isSelected = selected != null && selected.entityId == r.entityId;
            boolean isHover = mouseX >= listX && mouseX <= listX + LIST_WIDTH
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            if (isSelected) g.fill(listX, rowY, listX + LIST_WIDTH, rowY + ROW_HEIGHT, 0xFF3A3A5A);
            else if (isHover) g.fill(listX, rowY, listX + LIST_WIDTH, rowY + ROW_HEIGHT, 0xFF2A2A2A);

            String label = r.name != null && !r.name.isEmpty() ? r.name : ("Rocket #" + r.entityId);
            g.drawString(font, trimToWidth(label, LIST_WIDTH - 8), listX + 4, rowY + 4, 0xFFFFFF, false);
            rowY += ROW_HEIGHT;
        }
    }

    private String trimToWidth(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        String trimmed = text;
        while (trimmed.length() > 1 && font.width(trimmed + "...") > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + "...";
    }

    private void drawDetailPanel(GuiGraphics g, int ox, int oy) {
        int dx = ox + LIST_WIDTH + 12;
        int dy = oy + 24;

        if (selected == null) {
            g.drawString(font, "Select a rocket from the list", dx, dy, 0xAAAAAA, false);
            return;
        }

        g.drawString(font, "Tier " + selected.tier + "  |  " + selected.dimension, dx, dy, 0xCCCCCC, false);
        g.drawString(font, "Pos: " + selected.x + ", " + selected.y + ", " + selected.z, dx, dy + 11, 0xCCCCCC, false);

        String stateLabel = describeFlightState(selected.flightState);
        g.drawString(font, "State: " + stateLabel, dx, dy + 26, 0xFFFFAA, false);

        String reason = !selected.statusOverride.isEmpty()
                ? selected.statusOverride
                : describeWaitReason(selected.autoWaitReason);
        g.drawString(font, "Waiting on: " + reason, dx, dy + 37, 0xFFAA88, false);

        g.drawString(font, "Inventory:", dx, dy + 52, 0xCCCCCC, false);
        int invY = dy + 63;
        if (selected.inventory.isEmpty()) {
            g.drawString(font, "(empty)", dx + 4, invY, 0x888888, false);
        } else {
            for (RocketInfo.SlotInfo slot : selected.inventory) {
                if (invY > oy + HEIGHT - 40) {
                    g.drawString(font, "...", dx + 4, invY, 0x888888, false);
                    break;
                }
                g.drawString(font, "[" + slot.slot + "] " + slot.displayName + " x" + slot.count,
                        dx + 4, invY, 0xDDDDDD, false);
                invY += 10;
            }
        }

        g.drawString(font, "Name:", dx, oy + HEIGHT - 30 - 1, 0xCCCCCC, false);
    }

    private static String describeFlightState(String state) {
        return switch (state) {
            case "ascending" -> "Ascending";
            case "descending" -> "Descending";
            case "grounded" -> "Grounded";
            default -> "Unknown";
        };
    }

    private static String describeWaitReason(String reason) {
        return switch (reason) {
            case "not_enough_energy" -> "Not enough energy";
            case "not_enough_fuel" -> "Not enough fuel";
            case "no_rocket" -> "N/A";
            case "in_flight" -> "Currently in flight";
            case "idle" -> "Ready (idle)";
            default -> "Unknown";
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int ox = (width - WIDTH) / 2;
        int oy = (height - HEIGHT) / 2;
        int listX = ox + 4;
        int listY = oy + 24;
        int listH = HEIGHT - 24 - 28;

        if (mouseX >= listX && mouseX <= listX + LIST_WIDTH && mouseY >= listY && mouseY < listY + listH) {
            int rowIndex = (int) ((mouseY - listY) / ROW_HEIGHT);
            if (rowIndex >= 0 && rowIndex < rockets.size()) {
                selected = rockets.get(rowIndex);
                updateSelectionFields();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
