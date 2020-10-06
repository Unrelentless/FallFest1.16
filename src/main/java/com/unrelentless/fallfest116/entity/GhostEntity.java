package com.unrelentless.fallfest116.entity;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.unrelentless.fallfest116.FallFest116;
import com.unrelentless.fallfest116.block.FallenGrassBlock;
import com.unrelentless.fallfest116.block.FallenGrassBlock.LeafType;
import com.unrelentless.fallfest116.components.EntityComponents;
import com.unrelentless.fallfest116.components.GhostCooldownIntComponent;

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
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.fabricmc.api.EnvType;

public class GhostEntity extends GolemEntity implements RangedAttackMob {

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

    public GhostEntity(EntityType<? extends GolemEntity> entityType, World world) {
        super(entityType, world);
    }

    protected void initGoals() {
        this.goalSelector.add(1, new GhostProjectileAttackGoal(this, 1.25D, 60, 2.0F));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0D, 1.0000001E-5F));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1,
                new GhostFollowTargetGoal(this, PlayerEntity.class, 10, true, false, (livingEntity) -> {
                    if (livingEntity instanceof PlayerEntity) {
                        boolean firstCheck = !this.world.isDay();
                        int value = EntityComponents.GHOST_COOLDOWN.get(livingEntity).getValue();
                        boolean secondCheck = value == 0;
                        return firstCheck && secondCheck;
                    }
                    return false;
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
        if (!world.isDay() && stack.isFood() && EntityComponents.GHOST_COOLDOWN.get(player).getValue() == 0) {
            float saturation = stack.getItem().getFoodComponent().getSaturationModifier();
            int hunger = stack.getItem().getFoodComponent().getHunger();
            float aggregateScore = hunger * saturation;

            treatEntity(player, aggregateScore);
            EntityComponents.GHOST_COOLDOWN.get(player).setValue(GhostCooldownIntComponent.GHOST_COOLDOWN_VALUE);
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

    private void treatEntity(LivingEntity target, float aggregate) {
        boolean shouldUseBadPotion = aggregate <= 3.5;
        int randomIndex = (int) (Math.random()
                * (shouldUseBadPotion ? this.badPotions.length : this.goodPotions.length));
        Potion potion = shouldUseBadPotion ? this.badPotions[randomIndex] : this.goodPotions[randomIndex];

        potion.getEffects().forEach(effect -> target.applyStatusEffect(effect));
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

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_WITCH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_DOLPHIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_TURTLE_DEATH_BABY;
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

    private class GhostFollowTargetGoal extends FollowTargetGoal<PlayerEntity> {

        public GhostFollowTargetGoal(MobEntity mob, Class<PlayerEntity> targetClass, int reciprocalChance,
                boolean checkVisibility, boolean checkCanNavigate, Predicate<LivingEntity> targetPredicate) {
            super(mob, targetClass, reciprocalChance, checkVisibility, checkCanNavigate, targetPredicate);
        }

        @Override
        public boolean shouldContinue() {
            int value = EntityComponents.GHOST_COOLDOWN.get(this.mob.getTarget()).getValue();
            return super.shouldContinue() && !this.mob.world.isDay() && value == 0;
        }
    }
}
