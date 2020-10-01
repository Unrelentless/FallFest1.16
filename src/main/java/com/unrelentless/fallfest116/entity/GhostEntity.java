package com.unrelentless.fallfest116.entity;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class GhostEntity extends SnowGolemEntity {

    private static Potion[] goodPotions = getPotionsForType(StatusEffectType.BENEFICIAL);
    private static Potion[] badPotions = getPotionsForType(StatusEffectType.HARMFUL);

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
}
