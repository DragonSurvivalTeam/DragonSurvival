package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Glow;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.List;

public record GlowEffect(List<Glow> glows) implements AbilityEntityEffect {
    public static final MapCodec<GlowEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Glow.CODEC.listOf().fieldOf("glows").forGetter(GlowEffect::glows)
    ).apply(instance, GlowEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        glows.forEach(glow -> glow.apply(dragon, ability, target));
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        glows.forEach(glow -> glow.remove(entity));
    }

    public static List<GlowEffect> only(final Glow modifier) {
        return List.of(new GlowEffect(List.of(modifier)));
    }

    public static GlowEffect single(final Glow modifier) {
        return new GlowEffect(List.of(modifier));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
