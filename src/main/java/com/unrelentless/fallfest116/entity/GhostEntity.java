package com.unrelentless.fallfest116.entity;

import java.util.List;
import java.util.stream.Collectors;

import com.unrelentless.fallfest116.FallFest116;
import com.unrelentless.fallfest116.block.FallenGrassBlock;
import com.unrelentless.fallfest116.block.FallenGrassBlock.LeafType;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class GhostEntity extends SnowGolemEntity {

    private static Potion[] goodPotions = getPotionsForType(StatusEffectType.BENEFICIAL);
    private static Potion[] badPotions = getPotionsForType(StatusEffectType.HARMFUL);

    public static final BooleanProperty FALLED = BooleanProperty.of("falled");

    private static Potion[] getPotionsForType(StatusEffectType type) {
        List<Potion> potions = Registry.POTION.getEntries().stream().map(item -> item.getValue())
                .filter(potion -> !potion.getEffects().isEmpty()).collect(Collectors.toList());

        Potion[] filteredPotions = potions.stream()
                .filter(potion -> potion.getEffects().get(0).getEffectType().getType().equals(type))
                .toArray(Potion[]::new);

        return filteredPotions;
    }

    public GhostEntity(EntityType<? extends SnowGolemEntity> entityType, World world) {
        super(entityType, world);
    }

    protected void initGoals() {
        this.goalSelector.add(1, new ProjectileAttackGoal(this, 1.25D, 120, 15.0F));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0D, 1.0000001E-5F));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1,
                new FollowTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, (livingEntity) -> {
                    return livingEntity instanceof PlayerEntity;
                }));
    }

    public static DefaultAttributeContainer.Builder createGhostAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.20000000298023224D);
    }

    public boolean hurtByWater() {
        return false;
    }

    public void tickMovement() {
        super.tickMovement();
        if (!this.world.isClient) {
            int i = MathHelper.floor(this.getX());
            int j = MathHelper.floor(this.getY());
            int k = MathHelper.floor(this.getZ());
            if (this.world.getBiome(new BlockPos(i, 0, k)).getTemperature(new BlockPos(i, j, k)) > 1.0F) {
                this.damage(DamageSource.ON_FIRE, 1.0F);
            }

            if (!this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                return;
            }
            spreadFallOnGround();
            spreadFallOnTrees();
        }
    }

    public void attack(LivingEntity target, float pullProgress) {
        if (!this.world.isDay()) {
            Vec3d vec3d = target.getVelocity();
            double d = target.getX() + vec3d.x - this.getX();
            double e = target.getEyeY() - 1.100000023841858D - this.getY();
            double f = target.getZ() + vec3d.z - this.getZ();
            float g = MathHelper.sqrt(d * d + f * f);

            double scale = Math.pow(10, 1);
            double random = Math.round(Math.random() * scale) / scale;
            boolean shouldUseBadPotion = random == 0.5;

            int randomIndex = (int) (Math.random()
                    * (shouldUseBadPotion ? GhostEntity.badPotions.length : GhostEntity.goodPotions.length));

            Potion potion = shouldUseBadPotion ? GhostEntity.badPotions[randomIndex]
                    : GhostEntity.goodPotions[randomIndex];
            PotionEntity potionEntity = new PotionEntity(this.world, this);

            potionEntity.setItem(PotionUtil.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
            potionEntity.pitch -= -20.0F;
            potionEntity.setVelocity(d, e + (double) (g * 0.2F), f, 0.75F, 8.0F);

            this.world.spawnEntity(potionEntity);
        }
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
        for (int xPos = -4; xPos < 4; ++xPos) {
            for (int zPos = -4; zPos < 4; ++zPos) {
                for (int yPos = 0; yPos < 20; ++yPos) {
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
}
