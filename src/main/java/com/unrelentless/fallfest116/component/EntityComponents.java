package com.unrelentless.fallfest116.component;

import com.unrelentless.fallfest116.FallFest116;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public final class EntityComponents implements EntityComponentInitializer {
    public static final ComponentType<IntComponent> GHOST_COOLDOWN = ComponentRegistry.INSTANCE
            .registerIfAbsent(new Identifier(FallFest116.MODID, "ghost-cooldown"), IntComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(GHOST_COOLDOWN, player -> new GhostCooldownIntComponent(),
                RespawnCopyStrategy.ALWAYS_COPY);
    }
}