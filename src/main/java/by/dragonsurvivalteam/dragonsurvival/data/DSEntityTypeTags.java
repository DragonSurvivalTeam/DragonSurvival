package by.dragonsurvivalteam.dragonsurvival.data;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DSEntityTypeTags extends EntityTypeTagsProvider {
    public static final TagKey<EntityType<?>> ANIMAL_AVOID_BLACKLIST = key("animal_avoid_blacklist");

    public static final TagKey<EntityType<?>> DROPS_DRAGON_HEART_SHARD = key("drops_dragon_heart_shard");
    public static final TagKey<EntityType<?>> DROPS_WEAK_DRAGON_HEART = key("drops_weak_dragon_heart");
    public static final TagKey<EntityType<?>> DROPS_ELDER_DRAGON_HEART = key("drops_elder_dragon_heart");

    public DSEntityTypeTags(final DataGenerator generator, @Nullable final ExistingFileHelper helper) {
        super(generator, DragonSurvivalMod.MODID, helper);
    }

    @Override
    protected void addTags() {
        tag(ANIMAL_AVOID_BLACKLIST)
                .add(EntityType.WOLF)
                .add(EntityType.HOGLIN);

        tag(DROPS_DRAGON_HEART_SHARD).add(EntityType.ARMOR_STAND);
        tag(DROPS_WEAK_DRAGON_HEART).add(EntityType.ARMOR_STAND);
        tag(DROPS_ELDER_DRAGON_HEART).add(EntityType.ARMOR_STAND);
    }

    private static TagKey<EntityType<?>> key(@NotNull final String path) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, DragonSurvivalMod.res(path));
    }
}
