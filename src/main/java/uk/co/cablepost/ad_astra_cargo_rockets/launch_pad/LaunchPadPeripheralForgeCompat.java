package uk.co.cablepost.ad_astra_cargo_rockets.launch_pad;

import dan200.computercraft.api.ForgeComputerCraftAPI;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LaunchPadPeripheralForgeCompat {
    public static void regPer() {
        ForgeComputerCraftAPI.registerPeripheralProvider((level, pos, side) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LaunchPadBlockEntity launchPad) {
                return new LaunchPadBlockPeripheral(launchPad, side);
            }
            return null;
        });
    }
}
