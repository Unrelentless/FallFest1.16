package com.unrelentless.fallfest116.entity;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.unrelentless.fallfest116.FallFest116;
import com.unrelentless.fallfest116.block.FallenGrassBlock;
import com.unrelentless.fallfest116.block.FallenGrassBlock.LeafType;

import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.fabricmc.api.EnvType;

public class GhostEntity extends SnowGolemEntity {

    private Potion[] goodPotions = getPotionsForType(StatusEffectType.BENEFICIAL);
    private Potion[] badPotions = getPotionsForType(StatusEffectType.HARMFUL);

    public static final BooleanProperty FALLED = BooleanProperty.of("falled");

    private static Potion[] getPotionsForType(StatusEffectType type) {
        List<Potion> potions = Registry.POTION.getEntries().stream().map(item -> item.getValue())
                .filter(potion -> !potion.getEffects().isEmpty()).collect(Collectors.toList());

        Potion[] filteredPotions = potions.stream()
                .filter(potion -> potion.getEffects().get(0).getEffectType().getType().equals(type))
                .toArray(Potion[]::new);

        return filteredPotions;
    }

    private static final TrackedData<Boolean> SHOOTING;
    private static final Hashtable<String, Integer> playerData = new Hashtable<>();

    public GhostEntity(EntityType<? extends SnowGolemEntity> entityType, World world) {
        super(entityType, world);
    }

    public static void genTestData() {
        for (int i = 0; i < 1000; i++) {
            playerData.put(UUID.randomUUID().toString(), 600);

        }
    }

    protected void initGoals() {
        this.goalSelector.add(1, new GhostProjectileAttackGoal(this, 1.25D, 60, 2.0F));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0D, 1.0000001E-5F));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1,
                new FollowTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, (livingEntity) -> {
                    return !this.world.isDay() && livingEntity instanceof PlayerEntity
                            && !playerData.containsKey(livingEntity.getName().asString());
                }));
    }

    public static DefaultAttributeContainer.Builder createGhostAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2D).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 4.0D);
    }

    public boolean hurtByWater() {
        return false;
    }

    public void tickMovement() {
        super.tickMovement();
        if (!this.world.isClient) {
            if (!this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                return;
            }
            spreadFallOnGround();
            spreadFallOnTrees();
        }
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!world.isDay() && stack.isFood() && !playerData.containsKey(player.getName().asString())) {
            float saturation = stack.getItem().getFoodComponent().getSaturationModifier();
            int hunger = stack.getItem().getFoodComponent().getHunger();
            float aggregateScore = hunger * saturation;

            attackEntity(player, aggregateScore);
            putPlayerInData(player);
            stack.decrement(1);
        }
        return ActionResult.SUCCESS;
    }

    @Environment(EnvType.CLIENT)
    public boolean isShooting() {
        return (Boolean) this.dataTracker.get(SHOOTING);
    }

    public void setShooting(boolean shooting) {
        this.dataTracker.set(SHOOTING, shooting);
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SHOOTING, false);
    }

    @Override
    public void attack(LivingEntity target, float pullProgress) {

    }

    private void attackEntity(LivingEntity target, float aggregate) {
        double scale = Math.pow(10, 1);
        double random = Math.round(Math.random() * scale) / scale;
        boolean shouldUseBadPotion = random == 0.5;

        int randomIndex = (int) (Math.random()
                * (shouldUseBadPotion ? this.badPotions.length : this.goodPotions.length));

        Potion potion = shouldUseBadPotion ? this.badPotions[randomIndex] : this.goodPotions[randomIndex];

        target.applyStatusEffect(potion.getEffects().get(0));
    }

    private void putPlayerInData(PlayerEntity player) {
        String name = player.getName().asString();
        if (!playerData.containsKey(name)) {
            playerData.put(name, 600); // TODO: put real time in
        }
    }

    public static void updatePlayerData() {
        ArrayList<String> playersToRemove = new ArrayList<String>();
        playerData.entrySet().forEach(set -> {
            set.setValue(set.getValue() - 1);

            if (set.getValue() <= 0) {
                playersToRemove.add(set.getKey());
            }
        });
        playersToRemove.forEach(player -> playerData.remove(player));
    }

    private void spreadFallOnGround() {
        for (int l = 0; l < 4; ++l) {
            int i = MathHelper.floor(this.getX() + (double) ((float) (l % 2 * 2 - 1) * 0.25F));
            int j = MathHelper.floor(this.getY());
            int k = MathHelper.floor(this.getZ() + (double) ((float) (l / 2 % 2 * 2 - 1) * 0.25F));
            BlockPos blockPos = new BlockPos(i, j, k);

            BlockState blockState = FallFest116.FALLEN_GRASS_BLOCK.getDefaultState().with(FallenGrassBlock.TYPE,
                    LeafType.typeForBiome(world.getBiome(blockPos).getCategory()));

            if ((this.world.getBlockState(blockPos).isAir() && blockState.canPlaceAt(this.world, blockPos))
                    || this.world.getBlockState(blockPos).equals(Blocks.SNOW.getDefaultState())) {
                this.world.setBlockState(blockPos, blockState);
            }
        }
    }

    private void spreadFallOnTrees() {
        for (int yPos = 0; yPos < 30; ++yPos) {
            for (int xPos = -4; xPos < 4; ++xPos) {
                for (int zPos = -4; zPos < 4; ++zPos) {
                    int newXPos = MathHelper.floor(this.getX()) + xPos;
                    int newYPos = MathHelper.floor(this.getY()) + yPos;
                    int newZPos = MathHelper.floor(this.getZ()) + zPos;

                    BlockPos newBlockPos = new BlockPos(newXPos, newYPos, newZPos);
                    BlockState blockState = this.world.getBlockState(newBlockPos);
                    if (blockState.getBlock() instanceof LeavesBlock) {
                        BlockState newBlockState = blockState.with(GhostEntity.FALLED, true);
                        this.world.setBlockState(newBlockPos, newBlockState);
                    }
                }
            }
        }
    }

    static {
        SHOOTING = DataTracker.registerData(GhostEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    private class GhostProjectileAttackGoal extends ProjectileAttackGoal {
        private final GhostEntity mob;

        public GhostProjectileAttackGoal(RangedAttackMob mob, double mobSpeed, int intervalTicks, float maxShootRange) {
            super(mob, mobSpeed, intervalTicks, maxShootRange);
            this.mob = (GhostEntity) mob;
        }

        @Override
        public boolean shouldContinue() {
            return !this.mob.world.isDay() && super.shouldContinue();
        }

        public void start() {
            this.mob.setShooting(true);
            super.start();
        }

        public void stop() {
            this.mob.setShooting(false);
            super.stop();
        }
    }
}
