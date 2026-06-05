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

    /** Network constructor */
    public LaunchPadMenu(int syncId, Inventory playerInventory, @Nullable LaunchPadBlockEntity blockEntity) {
        super(AdAstraCargoRockets.LAUNCH_PAD.getMenuType().get(), syncId);
        this.blockEntity = blockEntity;

        if (blockEntity != null) {
            // 9 input slots (row 0-1)
            for (int i = 0; i < 9; i++) {
                int col = i % 9, row = i / 9;
                addSlot(new Slot(blockEntity, i, 8 + col * 18, 18 + row * 18));
            }
            // 9 output slots
            for (int i = 0; i < 9; i++) {
                int col = i % 9, row = i / 9;
                addSlot(new Slot(blockEntity, 9 + i, 8 + col * 18, 54 + row * 18));
            }
        }

        // Player inventory
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18 + (blockEntity != null ? 36 : 0)));
        // Player hotbar
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142 + (blockEntity != null ? 36 : 0)));
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity == null || blockEntity.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
