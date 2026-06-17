package uk.co.cablepost.ad_astra_cargo_rockets.launch_pad;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;
import javax.annotation.Nullable;
import uk.co.cablepost.ad_astra_cargo_rockets.cargo_rocket.CargoRocketEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaunchPadBlockPeripheral implements IPeripheral {

    private final LaunchPadBlockEntity blockEntity;

    public LaunchPadBlockPeripheral(BlockEntity blockEntity, @Nullable Direction direction) {
        this.blockEntity = (LaunchPadBlockEntity) blockEntity;
    }

    @Override public void attach(IComputerAccess computer) { blockEntity.addComputer(computer); }
    @Override public void detach(IComputerAccess computer) { blockEntity.removeComputer(computer); }
    @Override public String getType() { return "cargo_rocket_launch_pad"; }
    @Override public boolean equals(IPeripheral other) { return other instanceof LaunchPadBlockPeripheral; }

    @LuaFunction(mainThread = true)
    public final void launch(String planet) throws LuaException {
        @Nullable CargoRocketEntity rocket = blockEntity.getRocket();
        @Nullable LaunchFailReason reason = blockEntity.launch(planet);
        if (reason == null) {
            if (rocket != null) {
                var sound = ForgeRegistries.SOUND_EVENTS.getValue(
                        new net.minecraft.resources.ResourceLocation("ad_astra", "launch"));
                if (sound != null)
                    rocket.level().playSound(null, rocket.getX(), rocket.getY(), rocket.getZ(),
                            sound, SoundSource.NEUTRAL, 1f, 1f);
                else
                    rocket.level().playSound(null, rocket.getX(), rocket.getY(), rocket.getZ(),
                            SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 1f, 1f);
            }
            return;
        }
        switch (reason) {
            case NO_ROCKET -> throw new LuaException("No rocket found");
            case INVALID_PLANET -> throw new LuaException(planet + " is not a valid planet");
            case NOT_ENOUGH_ENERGY -> throw new LuaException("Not enough energy to launch");
            case NOT_ENOUGH_FUEL -> throw new LuaException("Not enough fuel to launch.");
            case ROCKET_TIER_TOO_LOW -> throw new LuaException(
                    planet + " requires a Tier " + blockEntity.calculateDifficulty(planet) + " rocket");
        }
    }

    @LuaFunction(mainThread = true)
    public final void moveItemsFromRocketToLaunchPad(int rocketSlot, int launchPadSlot) throws LuaException {
        @Nullable ItemMoveFailReason r = blockEntity.moveStackFromRocketToLaunchPad(rocketSlot, launchPadSlot);
        if (r == null) return;
        switch (r) {
            case NO_ROCKET -> throw new LuaException("No rocket found");
            case TARGET_FULL -> throw new LuaException("Destination full");
            case INVALID_SLOT -> throw new LuaException("Invalid slot");
        }
    }

    @LuaFunction(mainThread = true)
    public final void moveItemsFromLaunchPadToRocket(int launchPadSlot, int rocketSlot) throws LuaException {
        @Nullable ItemMoveFailReason r = blockEntity.moveStackFromLaunchPadToRocket(launchPadSlot, rocketSlot);
        if (r == null) return;
        switch (r) {
            case NO_ROCKET -> throw new LuaException("No rocket found");
            case TARGET_FULL -> throw new LuaException("Destination full");
            case INVALID_SLOT -> throw new LuaException("Invalid slot");
        }
    }

    @LuaFunction(mainThread = true)
    public final int getEnergyRequiredForLaunch() { return blockEntity.getEnergyRequiredForLaunch(); }

    @LuaFunction(mainThread = true)
    public final int getFuelRequiredForLaunch() { return blockEntity.getFuelRequiredForLaunch() / 1000; }

    @LuaFunction(mainThread = true)
    public final long getEnergy() { return blockEntity.getEnergy(); }

    @LuaFunction(mainThread = true)
    public final long getMaxEnergy() { return blockEntity.getMaxEnergy(); }

    @LuaFunction(mainThread = true)
    public final Map<String, Integer> getValidDestinations() { return blockEntity.getValidDestinations(); }

    @LuaFunction(mainThread = true)
    public Map<Integer, Map<String, ?>> listLaunchPadInventory() {
        Map<Integer, Map<String, ?>> result = new HashMap<>();
        for (int i = 0; i < blockEntity.getContainerSize(); i++) {
            var stack = blockEntity.getStack(i);
            if (!stack.isEmpty()) result.put(i + 1, Map.of(
                "name", stack.getHoverName().getString(),
                "id", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString(),
                "count", stack.getCount(),
                "max_count", stack.getMaxStackSize()
            ));
        }
        return result;
    }

    @LuaFunction
    public List<Integer> listLaunchPadInputSlotIndexes() {
        List<Integer> r = new ArrayList<>();
        for (int s : blockEntity._inputSlots) r.add(s + 1);
        return r;
    }

    @LuaFunction
    public List<Integer> listLaunchPadOutputSlotIndexes() {
        List<Integer> r = new ArrayList<>();
        for (int s : blockEntity._outputSlots) r.add(s + 1);
        return r;
    }

    @LuaFunction(mainThread = true)
    public boolean isRocketPresent() { return blockEntity.getRocket() != null; }

    @LuaFunction(mainThread = true)
    public @Nullable Map<Integer, Map<String, ?>> listRocketInventory() {
        @Nullable CargoRocketEntity rocket = blockEntity.getRocket();
        if (rocket == null) return null;
        Map<Integer, Map<String, ?>> result = new HashMap<>();
        for (int i = 0; i < rocket.getInventory().getContainerSize(); i++) {
            var stack = rocket.getInventory().getItem(i);
            if (!stack.isEmpty()) result.put(i + 1, Map.of(
                "name", stack.getHoverName().getString(),
                "id", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString(),
                "count", stack.getCount(),
                "max_count", stack.getMaxStackSize()
            ));
        }
        return result;
    }

    @LuaFunction(mainThread = true)
    public void destroyRocket() { blockEntity.destroyRocket(); }

    @LuaFunction(mainThread = true)
    public final void setRocketStatus(String status) throws LuaException {
        @Nullable CargoRocketEntity rocket = blockEntity.getRocket();
        if (rocket == null) throw new LuaException("No rocket found");
        rocket.statusOverride = status == null ? "" : status;
    }

    @LuaFunction(mainThread = true)
    public final void setRocketName(String name) throws LuaException {
        @Nullable CargoRocketEntity rocket = blockEntity.getRocket();
        if (rocket == null) throw new LuaException("No rocket found");
        rocket.setRocketName(name);
    }

    @LuaFunction(mainThread = true)
    public final int getFuel() { return blockEntity.getFuel(); }

    @LuaFunction(mainThread = true)
    public final int getMaxFuel() { return blockEntity.getMaxFuel(); }

    @LuaFunction(mainThread = true)
    public final int getCargoFluid() { return blockEntity.getCargoFluid(); }

    @LuaFunction(mainThread = true)
    public final int getMaxCargoFluid() { return blockEntity.getMaxCargoFluid(); }

    @LuaFunction(mainThread = true)
    public final String getCargoFluidType() {
        net.minecraftforge.fluids.FluidStack fluid = blockEntity.cargoFluidTank.getFluid();
        if (fluid.isEmpty()) return "empty";
        return net.minecraftforge.registries.ForgeRegistries.FLUIDS.getKey(fluid.getFluid()).toString();
    }

    @LuaFunction(mainThread = true)
    public final String getFuelType() {
        net.minecraftforge.fluids.FluidStack fluid = blockEntity.fluidTank.getFluid();
        if (fluid.isEmpty()) return "empty";
        return net.minecraftforge.registries.ForgeRegistries.FLUIDS.getKey(fluid.getFluid()).toString();
    }

    @LuaFunction(mainThread = true)
    public final int loadAllItems(IArguments args) throws LuaException {
        @Nullable CargoRocketEntity rocket = blockEntity.getRocket();
        if (rocket == null) throw new LuaException("No rocket found");
        String filterId = parseItemId(args.optString(0, null));
        int rocketSize = rocket.getInventory().getContainerSize();
        int remaining = 0;
        for (int i = 1; i <= 9; i++) {
            var stack = blockEntity.getStack(i - 1);
            if (stack.isEmpty()) continue;
            if (filterId != null && !ForgeRegistries.ITEMS.getKey(stack.getItem()).toString().equals(filterId)) continue;

            // 固定スロット対応 (i -> i) だけだと、ロケットの同じ番号のスロットが別アイテムで
            // 埋まっている場合に積めなくなる。空きスロット・同種スロットも含めて探すことで
            // 「まとめて積む」運用で複数種類のアイテムが混在しても正しく積めるようにする。
            // 1. まず同じアイテムが入っていて満杯でないスロットを探す
            // 2. 無ければ空のスロットを探す
            // 3. それでも入らなければ次のスロットへ（出力にTARGET_FULLとして残数加算）
            boolean movedAny = true;
            while (movedAny && !blockEntity.getStack(i - 1).isEmpty()) {
                movedAny = false;
                int targetSlot = findRocketSlotForItem(rocket, blockEntity.getStack(i - 1), rocketSize);
                if (targetSlot == -1) break;
                @Nullable ItemMoveFailReason r = blockEntity.moveStackFromLaunchPadToRocket(i, targetSlot);
                if (r == null) { movedAny = true; continue; }
                if (r == ItemMoveFailReason.TARGET_FULL) break;
                switch (r) {
                    case INVALID_SLOT -> throw new LuaException("Invalid slot: " + i);
                    case NO_ROCKET -> throw new LuaException("No rocket found");
                    default -> {}
                }
            }
            if (!blockEntity.getStack(i - 1).isEmpty()) {
                remaining += blockEntity.getStack(i - 1).getCount();
            }
        }
        return remaining;
    }

    /** ロケット内で、指定アイテムを受け入れられるスロット番号(1始まり)を探す。無ければ-1。 */
    private static int findRocketSlotForItem(CargoRocketEntity rocket, ItemStack toPlace, int rocketSize) {
        // 同種アイテムが入っていて、まだ満杯でないスロットを優先
        for (int s = 1; s <= rocketSize; s++) {
            var existing = rocket.getInventory().getItem(s - 1);
            if (!existing.isEmpty() && existing.getItem().equals(toPlace.getItem())
                    && existing.getCount() < existing.getMaxStackSize()) {
                return s;
            }
        }
        // 空きスロットを探す
        for (int s = 1; s <= rocketSize; s++) {
            if (rocket.getInventory().getItem(s - 1).isEmpty()) return s;
        }
        return -1;
    }

    @LuaFunction(mainThread = true)
    public final int unloadAllItems(IArguments args) throws LuaException {
        @Nullable CargoRocketEntity rocket = blockEntity.getRocket();
        if (rocket == null) throw new LuaException("No rocket found");
        String filterId = parseItemId(args.optString(0, null));
        int remaining = 0;
        for (int i = 1; i <= rocket.getInventory().getContainerSize(); i++) {
            var stack = rocket.getInventory().getItem(i - 1);
            if (stack.isEmpty()) continue;
            if (filterId != null && !ForgeRegistries.ITEMS.getKey(stack.getItem()).toString().equals(filterId)) continue;
            for (int target : blockEntity._outputSlots) {
                @Nullable ItemMoveFailReason r = blockEntity.moveStackFromRocketToLaunchPad(i, target + 1);
                if (r == null) break;
                if (r != ItemMoveFailReason.TARGET_FULL)
                    switch (r) {
                        case INVALID_SLOT -> throw new LuaException("Invalid slot: " + i);
                        case NO_ROCKET -> throw new LuaException("No rocket found");
                        default -> {}
                    }
            }
            // 全ての出力スロットが満杯等で結局移動できなかった分をカウント
            if (!rocket.getInventory().getItem(i - 1).isEmpty()) {
                remaining += rocket.getInventory().getItem(i - 1).getCount();
            }
        }
        return remaining;
    }

    @Nullable
    private static String parseItemId(@Nullable String filter) {
        if (filter == null || filter.isEmpty()) return null;
        int b = filter.indexOf('[');
        return b >= 0 ? filter.substring(0, b) : filter;
    }
}
