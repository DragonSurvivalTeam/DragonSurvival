package by.dragonsurvivalteam.dragonsurvival.data;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class DSEntityTypeTags extends EntityTypeTagsProvider {
    public static final TagKey<EntityType<?>> ANIMAL_AVOID_BLACKLIST = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(DragonSurvivalMod.MODID, "animal_avoid_blacklist"));

    public DSEntityTypeTags(final DataGenerator generator, @Nullable final ExistingFileHelper helper) {
        super(generator, DragonSurvivalMod.MODID, helper);
    }

    @Override
    protected void addTags() {
        tag(ANIMAL_AVOID_BLACKLIST)
                .add(EntityType.WOLF)
                .add(EntityType.HOGLIN);
    }
}
