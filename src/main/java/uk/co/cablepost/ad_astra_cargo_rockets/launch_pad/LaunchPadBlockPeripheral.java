package uk.co.cablepost.ad_astra_cargo_rockets.launch_pad;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
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

    @Override
    public void attach(IComputerAccess computer) { blockEntity.addComputer(computer); }
    @Override
    public void detach(IComputerAccess computer) { blockEntity.removeComputer(computer); }
    @Override
    public String getType() { return "cargo_rocket_launch_pad"; }
    @Override
    public boolean equals(IPeripheral other) { return other instanceof LaunchPadBlockPeripheral; }

    @LuaFunction(mainThread = true)
    public final void launch(String planet) throws LuaException {
        @Nullable CargoRocketEntity rocket = blockEntity.getRocket();
        @Nullable LaunchFailReason reason = blockEntity.launch(planet);

        if (reason == null) {
            if (rocket != null) {
                var level = rocket.level();
                var sound = ForgeRegistries.SOUND_EVENTS.getValue(
                        new net.minecraft.resources.ResourceLocation("ad_astra", "launch"));
                if (sound != null) {
                    level.playSound(null, rocket.blockPosition(), sound, SoundSource.NEUTRAL, 1f, 1f);
                } else {
                    level.playSound(null, rocket.blockPosition(),
                            SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 1f, 1f);
                }
            }
            return;
        }

        switch (reason) {
            case NO_ROCKET -> throw new LuaException("No rocket found");
            case INVALID_PLANET -> throw new LuaException(planet + " is not a valid planet");
            case NOT_ENOUGH_ENERGY -> throw new LuaException("Not enough energy to launch");
            case NOT_ENOUGH_FUEL -> throw new LuaException("Not enough fuel to launch.");
            case ROCKET_TIER_TOO_LOW -> {
                int diff = blockEntity.calculateDifficulty(planet);
                throw new LuaException(planet + " requires a Tier " + diff + " rocket");
            }
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
            if (!stack.isEmpty()) {
                result.put(i + 1, Map.of(
                    "name", stack.getHoverName().getString(),
                    "id", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString(),
                    "count", stack.getCount(),
                    "max_count", stack.getMaxStackSize()
                ));
            }
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
    public Boolean isRocketPresent() { return blockEntity.getRocket() != null; }

    @LuaFunction(mainThread = true)
    public @Nullable Map<Integer, Map<String, ?>> listRocketInventory() {
        @Nullable CargoRocketEntity rocket = blockEntity.getRocket();
        if (rocket == null) return null;
        Map<Integer, Map<String, ?>> result = new HashMap<>();
        for (int i = 0; i < rocket.getInventory().getContainerSize(); i++) {
            var stack = rocket.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                result.put(i + 1, Map.of(
                    "name", stack.getHoverName().getString(),
                    "id", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString(),
                    "count", stack.getCount(),
                    "max_count", stack.getMaxStackSize()
                ));
            }
        }
        return result;
    }

    @LuaFunction(mainThread = true)
    public void destroyRocket() { blockEntity.destroyRocket(); }

    @LuaFunction(mainThread = true)
    public final void loadAllItems(IArguments args) throws LuaException {
        if (blockEntity.getRocket() == null) throw new LuaException("No rocket found");
        String filter = args.optString(0, null);
        String filterId = parseItemId(filter);
        for (int i = 1; i <= 9; i++) {
            var stack = blockEntity.getStack(i - 1);
            if (stack.isEmpty()) continue;
            if (filterId != null && !ForgeRegistries.ITEMS.getKey(stack.getItem()).toString().equals(filterId)) continue;
            @Nullable ItemMoveFailReason r = blockEntity.moveStackFromLaunchPadToRocket(i, i);
            if (r != null && r != ItemMoveFailReason.TARGET_FULL)
                switch (r) {
                    case INVALID_SLOT -> throw new LuaException("Invalid slot: " + i);
                    case NO_ROCKET -> throw new LuaException("No rocket found");
                    default -> {}
                }
        }
    }

    @LuaFunction(mainThread = true)
    public final void unloadAllItems(IArguments args) throws LuaException {
        @Nullable CargoRocketEntity rocket = blockEntity.getRocket();
        if (rocket == null) throw new LuaException("No rocket found");
        String filter = args.optString(0, null);
        String filterId = parseItemId(filter);
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
        }
    }

    private static @Nullable String parseItemId(@Nullable String filter) {
        if (filter == null || filter.isEmpty()) return null;
        int bracket = filter.indexOf('[');
        return bracket >= 0 ? filter.substring(0, bracket) : filter;
    }
}
