package uk.co.cablepost.ad_astra_cargo_rockets.launch_pad;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import uk.co.cablepost.ad_astra_cargo_rockets.AdAstraCargoRockets;

public class LaunchPadMenu extends AbstractContainerMenu {

    @Nullable
    private final LaunchPadBlockEntity blockEntity;

    public LaunchPadMenu(int syncId, Inventory playerInventory, @Nullable LaunchPadBlockEntity blockEntity) {
        super(AdAstraCargoRockets.LAUNCH_PAD.getMenuType().get(), syncId);
        this.blockEntity = blockEntity;

        if (blockEntity != null) {
            // 入力スロット 9個 (y=28)
            for (int i = 0; i < 9; i++) {
                addSlot(new Slot(blockEntity, i, 9 + i * 18, 29));
            }
            // 出力スロット 9個 (y=62)
            for (int i = 0; i < 9; i++) {
                addSlot(new Slot(blockEntity, 9 + i, 9 + i * 18, 63));
            }
        }

        // プレイヤーインベントリ 3行 (y=97)
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 9 + col * 18, 98 + row * 18));

        // ホットバー (y=170)
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInventory, col, 9 + col * 18, 170));
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity == null || blockEntity.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            int containerSize = blockEntity != null ? 18 : 0;
            if (index < containerSize) {
                // コンテナ→プレイヤー
                if (!moveItemStackTo(stack, containerSize, this.slots.size(), true))
                    return ItemStack.EMPTY;
            } else {
                // プレイヤー→入力スロット
                if (!moveItemStackTo(stack, 0, 9, false))
                    return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }
}
