package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.server.level.ServerPlayer;

public record DragonAbilityHolder(HolderSet<DragonAbility> abilities, boolean isRemoval) {
    public static final Codec<DragonAbilityHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(DragonAbility.REGISTRY).fieldOf("abilities").forGetter(DragonAbilityHolder::abilities),
            Codec.BOOL.optionalFieldOf("is_removal", false).forGetter(DragonAbilityHolder::isRemoval)
    ).apply(instance, DragonAbilityHolder::new));

    public boolean use(final ServerPlayer player, final MagicData magic) {
        boolean wasUsed = false;

        for (Holder<DragonAbility> ability : abilities) {
            DragonAbilityInstance current = magic.getAbility(ability.getKey());

            if (isRemoval && current == null || !isRemoval && current != null) {
                continue;
            }

            if (isRemoval) {
                magic.removeAbility(ability.getKey());
            } else {
                magic.addAbility(player, ability);
            }

            wasUsed = true;
        }

        return wasUsed;
    }
}
