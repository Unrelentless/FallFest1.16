package com.unrelentless.fallfest116.entity;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.unrelentless.fallfest116.FallFest116;
import com.unrelentless.fallfest116.block.FallenLeavesBlock;
import com.unrelentless.fallfest116.component.EntityComponents;
import com.unrelentless.fallfest116.mixin.StatusEffectMixin;
import com.unrelentless.fallfest116.util.FallenColour;
import com.unrelentless.fallfest116.util.LeafType;

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
import net.minecraft.text.LiteralText;
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
    private static final TrackedData<Boolean> SHOOTING = DataTracker.registerData(GhostEntity.class,
            TrackedDataHandlerRegistry.BOOLEAN);

    public GhostEntity(EntityType<? extends GolemEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createGhostAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2D).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 4.0D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new GhostProjectileAttackGoal(this, 1.25D, 60, 2.0F));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0D, 1.0000001E-5F));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1,
                new GhostFollowTargetGoal(this, PlayerEntity.class, 10, true, false, (livingEntity) -> {
                    if (livingEntity instanceof PlayerEntity) {
                        int value = EntityComponents.GHOST_COOLDOWN.get(livingEntity).getValue();
                        return !this.world.isDay() && value == 0;
                    }
                    return false;
                }));
    }

    @Override
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
        if (world.isClient) {
            return ActionResult.CONSUME;
        }

        ItemStack stack = player.getStackInHand(hand);

        if (world.isDay() || !stack.isFood()) {
            return ActionResult.SUCCESS;
        }

        int cooldownValue = EntityComponents.GHOST_COOLDOWN.get(player).getValue();

        if (cooldownValue == 0) {
            float saturation = stack.getItem().getFoodComponent().getSaturationModifier();
            int hunger = stack.getItem().getFoodComponent().getHunger();
            float aggregateScore = hunger * saturation;

            EntityComponents.GHOST_COOLDOWN.get(player).resetValue();

            treatEntity(player, aggregateScore);
            stack.decrement(1);
        } else if (!player.world.isClient) {
            player.sendMessage(new LiteralText("Cannot trick or treat for another " + cooldownValue / 20 + " seconds."),
                    false);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SHOOTING, false);
    }

    @Override
    public void attack(LivingEntity target, float pullProgress) {
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

    @Environment(EnvType.CLIENT)
    public boolean isShooting() {
        return (Boolean) this.dataTracker.get(SHOOTING);
    }

    public void setShooting(boolean shooting) {
        this.dataTracker.set(SHOOTING, shooting);
    }

    private void treatEntity(LivingEntity target, float aggregate) {
        boolean shouldUseBadPotion = aggregate <= 3;
        int randomIndex = (int) (Math.random()
                * (shouldUseBadPotion ? this.badPotions.length : this.goodPotions.length));
        Potion potion = shouldUseBadPotion ? this.badPotions[randomIndex] : this.goodPotions[randomIndex];

        potion.getEffects().forEach(effect -> target.addStatusEffect(effect));
    }

    private void spreadFallOnGround() {
        for (int surroundingBlocks = 0; surroundingBlocks < 4; ++surroundingBlocks) {
            double surroundingOffset = (double) ((float) (surroundingBlocks % 2 * 2 - 1) * 0.25F);
            int xPos = MathHelper.floor(this.getX() + surroundingOffset);
            int yPos = MathHelper.floor(this.getY());
            int zPos = MathHelper.floor(this.getZ() + surroundingOffset);
            BlockPos blockPos = new BlockPos(xPos, yPos, zPos);

            BlockState blockState = FallFest116.FALLEN_LEAVES_BLOCK.getDefaultState()
                    .with(FallenLeavesBlock.TYPE, LeafType.typeForBiome(world.getBiome(blockPos).getCategory()))
                    .with(FallenColour.COLOUR, FallenColour.COLOURS[new Random().nextInt(3)]);

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
                        if (!blockState.get(GhostEntity.FALLED)) {

                            BlockState newBlockState = blockState.with(GhostEntity.FALLED, true)
                                    .with(FallenColour.COLOUR, FallenColour.COLOURS[new Random().nextInt(3)]);
                            world.setBlockState(newBlockPos, newBlockState);
                        }
                    }
                }
            }
        }
    }

    private static Potion[] getPotionsForType(StatusEffectType type) {
        List<Potion> potions = Registry.POTION.getEntries().stream().map(item -> item.getValue())
                .filter(potion -> !potion.getEffects().isEmpty()).collect(Collectors.toList());

        Potion[] filteredPotions = potions.stream()
                .filter(potion -> ((StatusEffectMixin) potion.getEffects().get(0).getEffectType()).getStatusEffectType()
                        .equals(type))
                .toArray(Potion[]::new);

        return filteredPotions;
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
