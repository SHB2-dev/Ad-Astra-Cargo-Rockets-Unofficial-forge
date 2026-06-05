package uk.co.cablepost.ad_astra_cargo_rockets.cargo_rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import uk.co.cablepost.ad_astra_cargo_rockets.AdAstraCargoRockets;

import java.util.List;
import java.util.Objects;

public class CargoRocketEntity extends Entity {
    public String targetPlanet = "";

    // SimpleInventory equivalent using a NonNullList
    private final net.minecraft.core.NonNullList<ItemStack> inventory =
            net.minecraft.core.NonNullList.withSize(9, ItemStack.EMPTY);

    private static final EntityDataAccessor<Integer> TRACKED_TIER =
            SynchedEntityData.defineId(CargoRocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TRACKED_LAUNCH_TICKS =
            SynchedEntityData.defineId(CargoRocketEntity.class, EntityDataSerializers.INT);

    private boolean hasPlayedLandingSound = false;

    public CargoRocketEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TRACKED_TIER, 0);
        this.entityData.define(TRACKED_LAUNCH_TICKS, 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        entityData.set(TRACKED_TIER, nbt.getInt("Tier"));
        entityData.set(TRACKED_LAUNCH_TICKS, nbt.getInt("LaunchTicks"));
        targetPlanet = nbt.getString("TargetPlanet");
        ContainerHelper.loadAllItems(nbt, inventory);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("Tier", entityData.get(TRACKED_TIER));
        nbt.putInt("LaunchTicks", entityData.get(TRACKED_LAUNCH_TICKS));
        nbt.putString("TargetPlanet", targetPlanet);
        ContainerHelper.saveAllItems(nbt, inventory);
    }

    public void setTier(int tier) { entityData.set(TRACKED_TIER, tier); }
    public int getTier() { return entityData.get(TRACKED_TIER); }
    public void setLaunchTicks(int ticks) { entityData.set(TRACKED_LAUNCH_TICKS, ticks); }
    public int getLaunchTicks() { return entityData.get(TRACKED_LAUNCH_TICKS); }

    /** Expose inventory as a simple Container for peripheral use */
    public net.minecraft.world.SimpleContainer getInventory() {
        // Wrap NonNullList in a SimpleContainer each call is fine for our use
        net.minecraft.world.SimpleContainer c = new net.minecraft.world.SimpleContainer(9) {
            @Override public ItemStack getItem(int slot) { return inventory.get(slot); }
            @Override public void setItem(int slot, ItemStack stack) { inventory.set(slot, stack); }
            @Override public int getContainerSize() { return 9; }
        };
        return c;
    }

    @Override public boolean canBeCollidedWith() { return true; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean isIgnoringBlockTriggers() { return true; }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide || hand == InteractionHand.OFF_HAND) return InteractionResult.PASS;
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(" ====== Ship inventory ======"));
        for (int i = 0; i < inventory.size(); i++) {
            var stack = inventory.get(i);
            if (!stack.isEmpty())
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "Slot " + i + ": " + stack.getCount() + "x " + stack.getDisplayName().getString()));
        }
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(" =========================="));
        return InteractionResult.SUCCESS;
    }

    private void dropInventory() {
        if (level().isClientSide) return;
        for (int i = 0; i < inventory.size(); i++) {
            spawnAtLocation(inventory.get(i));
            inventory.set(i, ItemStack.EMPTY);
        }
    }

    private void dropSelf() {
        if (level().isClientSide) return;
        int tier = getTier();
        if (tier == 1) { spawnAtLocation(new ItemStack(AdAstraCargoRockets.CARGO_ROCKET_TIER_1_ITEM.get())); return; }
        if (tier == 2) { spawnAtLocation(new ItemStack(AdAstraCargoRockets.CARGO_ROCKET_TIER_2_ITEM.get())); return; }
        if (tier == 3) { spawnAtLocation(new ItemStack(AdAstraCargoRockets.CARGO_ROCKET_TIER_3_ITEM.get())); return; }
        if (tier == 4) { spawnAtLocation(new ItemStack(AdAstraCargoRockets.CARGO_ROCKET_TIER_4_ITEM.get())); return; }
        spawnAtLocation(new ItemStack(Items.DIRT));
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (isInvulnerableTo(source)) return false;
        if (level().isClientSide) return true;
        dropInventory();
        dropSelf();
        kill();
        return true;
    }

    public void killRocket() {
        hurt(damageSources().genericKill(), Float.MAX_VALUE);
    }

    @Override
    public void tick() {
        super.tick();
        setPos(getX(), getY() + getDeltaMovement().y, getZ());

        if (level().isClientSide) {
            clientTick();
            return;
        }
        serverTick();
    }

    private void clientTick() {
        double velY = getDeltaMovement().y;
        if (velY < -0.1) {
            boolean groundNearby = false;
            for (int i = 1; i < 30; i++) {
                BlockPos check = blockPosition().below(i);
                if (!level().getBlockState(check).getCollisionShape(level(), check).isEmpty()) {
                    groundNearby = true;
                    break;
                }
            }
            if (groundNearby) {
                spawnFlameParticles();
                if (!hasPlayedLandingSound) {
                    hasPlayedLandingSound = true;
                    playLaunchSound();
                }
            } else {
                hasPlayedLandingSound = false;
            }
        } else if (getLaunchTicks() > 0) {
            spawnFlameParticles();
            int ticks = getLaunchTicks();
            if (ticks == 1 || ticks == 40) playLaunchSound();
            hasPlayedLandingSound = false;
        } else {
            hasPlayedLandingSound = false;
        }
    }

    private void spawnFlameParticles() {
        for (int i = 0; i < 3; i++) {
            level().addParticle(getParticleOrDefault("ad_astra:large_flame", ParticleTypes.FLAME), true,
                    getX() + (random.nextDouble() - 0.5), getY(), getZ() + (random.nextDouble() - 0.5), 0, -0.2, 0);
            level().addParticle(getParticleOrDefault("ad_astra:large_smoke", ParticleTypes.CAMPFIRE_COSY_SMOKE), true,
                    getX() + (random.nextDouble() - 0.5), getY(), getZ() + (random.nextDouble() - 0.5), 0, -0.2, 0);
        }
    }

    private net.minecraft.core.particles.ParticleOptions getParticleOrDefault(
            String id, net.minecraft.core.particles.ParticleOptions fallback) {
        var pt = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(id));
        return (pt instanceof net.minecraft.core.particles.ParticleOptions po) ? po : fallback;
    }

    private void playLaunchSound() {
        var sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ad_astra", "launch"));
        if (sound != null) {
            level().playSound(getX(), getY(), getZ(), sound, SoundSource.AMBIENT, 2f, 0.5f, false);
        } else {
            level().playSound(getX(), getY(), getZ(),
                    SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 2f, 0.5f, false);
        }
    }

    private void serverTick() {
        // Collision with other rockets
        List<CargoRocketEntity> intersecting = level().getEntitiesOfClass(
                CargoRocketEntity.class, new AABB(blockPosition()).inflate(2), e -> e.isAlive() && e.getId() != getId());
        if (!intersecting.isEmpty()) {
            level().explode(this, getX(), getY() + 2, getZ(), 5, Level.ExplosionInteraction.MOB);
            dropInventory(); dropSelf(); kill(); return;
        }

        if (targetPlanet.isEmpty()) {
            descentTick();
        } else {
            ascentTick();
        }
    }

    private void descentTick() {
        setLaunchTicks(0);

        List<CargoRocketEntity> below = level().getEntitiesOfClass(
                CargoRocketEntity.class, new AABB(blockPosition().below(3)).inflate(2),
                e -> e.isAlive() && e.getId() != getId());
        if (!below.isEmpty()) {
            level().explode(this, getX(), getY() - 0.5, getZ(), 5, Level.ExplosionInteraction.MOB);
            dropInventory(); dropSelf(); kill(); return;
        }

        Integer highestBlockY = null;
        int currentY = blockPosition().getY();
        outer:
        for (int y = currentY; y > currentY - 30; y--) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos check = new BlockPos(blockPosition().getX() + x, y, blockPosition().getZ() + z);
                    if (!level().getBlockState(check).getCollisionShape(level(), check).isEmpty()) {
                        highestBlockY = y;
                        break outer;
                    }
                }
            }
        }

        if (highestBlockY != null) {
            double target = highestBlockY + 1.0;
            double dist = getY() - target;
            if (dist <= 0.1) {
                setDeltaMovement(0, 0, 0);
                setPos(getX(), target, getZ());
            } else {
                setDeltaMovement(0, -Math.min(1.0, Math.max(0.1, dist * 0.1)), 0);
            }
        } else {
            setDeltaMovement(0, -1.0, 0);
        }
    }

    private void ascentTick() {
        int ticks = getLaunchTicks();
        setLaunchTicks(ticks + 1);

        List<CargoRocketEntity> above = level().getEntitiesOfClass(
                CargoRocketEntity.class, new AABB(blockPosition().above(4)).inflate(2),
                e -> e.isAlive() && e.getId() != getId());
        if (!above.isEmpty()) {
            level().explode(this, getX(), getY() - 4, getZ(), 5, Level.ExplosionInteraction.MOB);
            dropInventory(); dropSelf(); kill(); return;
        }

        // Check clear path
        boolean clear = true;
        for (int x = -1; x <= 1 && clear; x++)
            for (int z = -1; z <= 1 && clear; z++)
                if (!level().getBlockState(blockPosition().offset(x, 4, z)).isAir())
                    clear = false;

        if (clear) {
            if (ticks < 40) {
                setDeltaMovement((random.nextDouble() - 0.5) * 0.05, 0, (random.nextDouble() - 0.5) * 0.05);
            } else {
                setDeltaMovement(0, Math.min(1.0, (ticks - 40) * 0.01), 0);
            }
        } else {
            setDeltaMovement(0, 0, 0);
        }

        if (getY() > level().getMaxBuildHeight() + 400) {
            dimensionTransfer();
        }
    }

    private void dimensionTransfer() {
        ServerLevel targetWorld = null;
        for (var world : Objects.requireNonNull(level().getServer()).getAllLevels()) {
            if (world.dimension().location().toString().equals(targetPlanet)) {
                targetWorld = world;
                break;
            }
        }
        targetPlanet = "";
        if (targetWorld == null || targetWorld.equals(level())) return;

        Entity spawned = getType().create(targetWorld);
        if (spawned != null) {
            spawned.restoreFrom(this);
            spawned.moveTo(getX(), targetWorld.getMaxBuildHeight() + 200, getZ(), 0, 0);
            spawned.setDeltaMovement(Vec3.ZERO);
            targetWorld.addFreshEntity(spawned);
        }
        discard();
    }
}
