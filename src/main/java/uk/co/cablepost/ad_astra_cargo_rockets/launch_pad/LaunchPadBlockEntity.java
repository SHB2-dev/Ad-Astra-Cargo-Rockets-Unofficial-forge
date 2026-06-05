package uk.co.cablepost.ad_astra_cargo_rockets.launch_pad;

import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import uk.co.cablepost.ad_astra_cargo_rockets.AbstractFluidMachineBlockEntity;
import uk.co.cablepost.ad_astra_cargo_rockets.AdAstraCargoRockets;
import uk.co.cablepost.ad_astra_cargo_rockets.ModConfig;
import uk.co.cablepost.ad_astra_cargo_rockets.cargo_rocket.CargoRocketEntity;

import java.util.*;

public class LaunchPadBlockEntity extends AbstractFluidMachineBlockEntity implements MenuProvider {

    private final Set<IComputerAccess> computers = new HashSet<>();
    public static final TagKey<Item> DENIED_ITEMS = net.minecraft.tags.ItemTags.create(
            new net.minecraft.resources.ResourceLocation(AdAstraCargoRockets.MOD_ID, "denied_in_launch_pad"));

    public LaunchPadBlockEntity(BlockPos pos, BlockState state) {
        super(
            AdAstraCargoRockets.LAUNCH_PAD.getBlockEntity().get(),
            pos, state,
            new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8 },
            new int[]{ 9,10,11,12,13,14,15,16,17 },
            50000, 1000, 0, false
        );
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LaunchPadBlockEntity be) {
        // No active processing needed for the launch pad
    }

    @Override public int getMaxProcessProgress() { return 0; }
    @Override public int processEnergyConsumption() { return 0; }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.ad_astra_cargo_rockets.launch_pad");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new LaunchPadMenu(syncId, playerInventory, this);
    }

    // --- Peripheral ---
    public void addComputer(IComputerAccess computer) { computers.add(computer); }
    public void removeComputer(IComputerAccess computer) { computers.remove(computer); }

    // --- Item moving ---
    public @Nullable ItemMoveFailReason moveStackFromRocketToLaunchPad(int rocketSlotIndex, int launchPadSlotIndex) {
        @Nullable CargoRocketEntity rocket = getRocket();
        if (rocket == null) return ItemMoveFailReason.NO_ROCKET;

        ItemStack rocketStack, launchPadStack;
        try {
            rocketStack = rocket.getInventory().getItem(rocketSlotIndex - 1);
            launchPadStack = _inventory.get(launchPadSlotIndex - 1);
        } catch (Exception ignored) { return ItemMoveFailReason.INVALID_SLOT; }

        if (rocketStack.is(DENIED_ITEMS)) return ItemMoveFailReason.TARGET_FULL;
        if (!launchPadStack.isEmpty() && (!rocketStack.getItem().equals(launchPadStack.getItem()) || launchPadStack.getCount() >= launchPadStack.getMaxStackSize()))
            return ItemMoveFailReason.TARGET_FULL;

        int maxCanMove = Math.min(launchPadStack.getMaxStackSize() - launchPadStack.getCount(), rocketStack.getCount());
        if (maxCanMove == 0) return null;

        if (launchPadStack.isEmpty()) {
            _inventory.set(launchPadSlotIndex - 1, rocketStack.copy());
            _inventory.get(launchPadSlotIndex - 1).setCount(maxCanMove);
            rocketStack.shrink(maxCanMove);
        } else {
            launchPadStack.grow(maxCanMove);
            rocketStack.shrink(maxCanMove);
        }
        return null;
    }

    public @Nullable ItemMoveFailReason moveStackFromLaunchPadToRocket(int launchPadSlotIndex, int rocketSlotIndex) {
        @Nullable CargoRocketEntity rocket = getRocket();
        if (rocket == null) return ItemMoveFailReason.NO_ROCKET;

        ItemStack rocketStack, launchPadStack;
        try {
            rocketStack = rocket.getInventory().getItem(rocketSlotIndex - 1);
            launchPadStack = _inventory.get(launchPadSlotIndex - 1);
        } catch (Exception ignored) { return ItemMoveFailReason.INVALID_SLOT; }

        if (!rocketStack.isEmpty() && (!launchPadStack.getItem().equals(rocketStack.getItem()) || rocketStack.getCount() >= rocketStack.getMaxStackSize()))
            return ItemMoveFailReason.TARGET_FULL;

        int maxCanMove = Math.min(rocketStack.getMaxStackSize() - rocketStack.getCount(), launchPadStack.getCount());
        if (maxCanMove == 0) return null;

        if (rocketStack.isEmpty()) {
            rocket.getInventory().setItem(rocketSlotIndex - 1, launchPadStack.copy());
            rocket.getInventory().getItem(rocketSlotIndex - 1).setCount(maxCanMove);
            launchPadStack.shrink(maxCanMove);
        } else {
            rocketStack.grow(maxCanMove);
            launchPadStack.shrink(maxCanMove);
        }
        setChanged();
        return null;
    }

    public @Nullable CargoRocketEntity getRocket() {
        if (level == null) return null;
        List<CargoRocketEntity> nearby = level.getEntitiesOfClass(
                CargoRocketEntity.class, new AABB(worldPosition).inflate(2), CargoRocketEntity::isAlive);
        nearby = nearby.stream()
                .filter(x -> x.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(worldPosition).add(0, 0.5, 0)) < 1f)
                .toList();
        return nearby.size() == 1 ? nearby.get(0) : null;
    }

    public int calculateDifficulty(String planet) {
        Map<String, Integer> valid = getValidDestinations();
        String currentDim = level.dimension().location().toString();
        int currentTier = valid.getOrDefault(currentDim, 1);
        int targetTier  = valid.getOrDefault(planet, 1);
        return Math.max(1, Math.abs(targetTier - currentTier));
    }

    public @Nullable LaunchFailReason launch(String planet) {
        @Nullable CargoRocketEntity rocket = getRocket();
        if (rocket == null) return LaunchFailReason.NO_ROCKET;

        Map<String, Integer> valid = getValidDestinations();
        if (!valid.containsKey(planet)) return LaunchFailReason.INVALID_PLANET;

        int difficulty = calculateDifficulty(planet);
        if (difficulty > rocket.getTier()) return LaunchFailReason.ROCKET_TIER_TOO_LOW;
        if ((long) getEnergyRequiredForLaunch() * difficulty > _energyStorage.getEnergyStored())
            return LaunchFailReason.NOT_ENOUGH_ENERGY;

        FluidStack fluid = fluidTank.getFluid();
        String fluidId = ForgeRegistries.FLUIDS.getKey(fluid.getFluid()).toString();
        double perf = ModConfig.INSTANCE.fuels.getOrDefault(fluidId, 1.0);
        if (perf <= 0) perf = 1.0;

        int actualFuel = (int) ((getFuelRequiredForLaunch() * difficulty) / perf);
        if (fluid.getAmount() < actualFuel) return LaunchFailReason.NOT_ENOUGH_FUEL;

        FluidStack drained = fluidTank.drain(actualFuel, net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        if (drained.getAmount() == actualFuel) {
            _energyStorage.extractEnergy(getEnergyRequiredForLaunch() * difficulty, false);
            rocket.targetPlanet = planet;
            setChanged();
            return null;
        }
        return LaunchFailReason.NOT_ENOUGH_FUEL;
    }

    public int getEnergyRequiredForLaunch() { return 5000; }
    public int getFuelRequiredForLaunch() { return 600000; }

    public int getEnergy() { return _energyStorage.getEnergyStored(); }
    public int getMaxEnergy() { return _energyStorage.getMaxEnergyStored(); }
    public int getFuel() { return fluidTank.getFluidAmount(); }

    public Map<String, Integer> getValidDestinations() {
        if (level == null || level.getServer() == null) return new HashMap<>();
        Map<String, Integer> tierIndex = ModConfig.INSTANCE.validDestinations;
        Map<String, Integer> result = new HashMap<>();
        for (var world : level.getServer().getAllLevels()) {
            String id = world.dimension().location().toString();
            if (tierIndex.containsKey(id)) result.put(id, tierIndex.get(id));
        }
        return result;
    }

    public void destroyRocket() {
        CargoRocketEntity rocket = getRocket();
        if (rocket != null) rocket.killRocket();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !stack.is(DENIED_ITEMS);
    }
}
