package uk.co.cablepost.ad_astra_cargo_rockets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractFluidMachineBlockEntity extends AbstractMachineBlockEntity {

    /** 32 buckets = 32000 mB */
    public static final int FLUID_CAPACITY = 32000;

    public final FluidTank fluidTank = new FluidTank(FLUID_CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            String fluidId = net.minecraftforge.registries.ForgeRegistries.FLUIDS
                    .getKey(stack.getFluid()).toString();
            return ModConfig.INSTANCE.fuels.containsKey(fluidId);
        }

        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> fluidTank);

    public AbstractFluidMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                            int[] inputSlots, int[] outputSlots,
                                            int energyCapacity, int energyMaxInsert,
                                            int energyMaxExtract, boolean doProcessing) {
        super(type, pos, state, inputSlots, outputSlots, energyCapacity, energyMaxInsert, energyMaxExtract, doProcessing);
    }

    @Override
    public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag fluidTag = new CompoundTag();
        fluidTank.writeToNBT(fluidTag);
        tag.put("FluidContent", fluidTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("FluidContent")) {
            fluidTank.readFromNBT(tag.getCompound("FluidContent"));
        }
    }
}
