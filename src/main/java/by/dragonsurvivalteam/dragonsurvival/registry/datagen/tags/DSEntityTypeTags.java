package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DSEntityTypeTags extends EntityTypeTagsProvider {
    @Translation(comments = "Animal Avoid Blacklist for Dragons")
    public static final TagKey<EntityType<?>> ANIMAL_AVOID_BLACKLIST = key("animal_avoid_blacklist");
    @Translation(comments = "Vehicle Whitelist for Dragons")
    public static final TagKey<EntityType<?>> VEHICLE_WHITELIST = key("vehicle_whitelist");
    @Translation(comments = "Charged Effect Spread Blacklist")
    public static final TagKey<EntityType<?>> CHARGED_SPREAD_BLACKLIST = key("charged_spread_blacklist");
    @Translation(comments = "Confounded Effect Target Redirection Blacklist")
    public static final TagKey<EntityType<?>> CONFOUNDED_TARGET_BLACKLIST = key("confounded_target_blacklist");

    @Translation(comments = "Hunter Targets")
    public static final TagKey<EntityType<?>> HUNTER_TARGETS = key("hunter_targets");
    @Translation(comments = "hunter_faction")
    public static final TagKey<EntityType<?>> HUNTER_FACTION = key("hunter_faction");

    @Translation(comments = "Dragons")
    public static final TagKey<EntityType<?>> DRAGONS = key("dragons");

    @Translation(comments = "Can fly")
    public static final TagKey<EntityType<?>> CAN_FLY = key("can_fly");

    @Translation(comments = "Drops Dragon Heart Shard")
    public static final TagKey<EntityType<?>> DROPS_DRAGON_HEART_SHARD = key("drops_dragon_heart_shard");
    @Translation(comments = "Drops Weak Dragon Heart")
    public static final TagKey<EntityType<?>> DROPS_WEAK_DRAGON_HEART = key("drops_weak_dragon_heart");
    @Translation(comments = "Drops Elder Dragon Heart")
    public static final TagKey<EntityType<?>> DROPS_ELDER_DRAGON_HEART = key("drops_elder_dragon_heart");

    public DSEntityTypeTags(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider, @Nullable final ExistingFileHelper helper) {
        super(output, provider, DragonSurvival.MODID, helper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        tag(ANIMAL_AVOID_BLACKLIST)
                .add(EntityType.WOLF)
                .add(EntityType.HOGLIN)
                .addOptional(ResourceLocation.fromNamespaceAndPath("cobblemon", "pokemon"));

        tag(VEHICLE_WHITELIST)
                .addTag(Tags.EntityTypes.BOATS)
                .addTag(Tags.EntityTypes.MINECARTS)
                .addOptional(ResourceLocation.fromNamespaceAndPath("littlelogistics", "seater_barge"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("create", "seat"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("create", "contraption"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("create", "gantry_contraption"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("create", "stationary_contraption"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("hexerei", "broom"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("botania", "player_mover"));

        tag(CHARGED_SPREAD_BLACKLIST)
                .add(EntityType.ARMOR_STAND)
                .add(EntityType.CAT)
                .add(EntityType.MINECART)
                .add(EntityType.GUARDIAN)
                .add(EntityType.ELDER_GUARDIAN)
                .add(EntityType.ENDERMAN)
                .addOptional(ResourceLocation.fromNamespaceAndPath("upgrade_aquatic", "thrasher"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("upgrade_aquatic", "great_thrasher"));

        tag(HUNTER_TARGETS)
                .add(EntityType.EVOKER)
                .add(EntityType.PILLAGER)
                .add(EntityType.VINDICATOR)
                .add(EntityType.STRAY)
                .add(EntityType.SKELETON)
                .add(EntityType.SPIDER)
                .add(EntityType.CAVE_SPIDER)
                .add(EntityType.ZOMBIE)
                .add(EntityType.ZOMBIE_VILLAGER)
                .add(EntityType.HUSK)
                .add(EntityType.ZOMBIFIED_PIGLIN)
                .add(EntityType.BLAZE)
                .add(EntityType.DROWNED)
                .add(EntityType.ENDERMITE)
                .add(EntityType.HOGLIN)
                .add(EntityType.MAGMA_CUBE)
                .add(EntityType.RAVAGER)
                .add(EntityType.SLIME)
                .add(EntityType.WITCH)
                .add(EntityType.WITHER_SKELETON)
                .add(EntityType.WITHER)
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("zombiemobs", "zombie_animals"));

        // Used in 'curse_of_kindness' enchantment
        tag(HUNTER_FACTION)
                .add(EntityType.VILLAGER)
                .add(EntityType.IRON_GOLEM)
                .add(DSEntities.HUNTER_AMBUSHER.value())
                .add(DSEntities.HUNTER_GRIFFIN.value())
                .add(DSEntities.HUNTER_HOUND.value())
                .add(DSEntities.HUNTER_KNIGHT.value())
                .add(DSEntities.HUNTER_LEADER.value())
                .add(DSEntities.HUNTER_SPEARMAN.value());

        tag(DRAGONS).add(EntityType.ENDER_DRAGON);

        tag(CAN_FLY)
                .add(EntityType.ALLAY)
                .add(EntityType.BAT)
                .add(EntityType.BEE)
                .add(EntityType.BLAZE)
                .add(EntityType.ENDER_DRAGON)
                .add(EntityType.GHAST)
                .add(EntityType.PARROT)
                .add(EntityType.PHANTOM)
                .add(EntityType.VEX)
                .add(EntityType.WITHER);

        tag(DROPS_DRAGON_HEART_SHARD).add(EntityType.ARMOR_STAND);
        tag(DROPS_WEAK_DRAGON_HEART).add(EntityType.ARMOR_STAND);
        tag(DROPS_ELDER_DRAGON_HEART).add(EntityType.ARMOR_STAND);
    }

    private static TagKey<EntityType<?>> key(@NotNull final String path) {
        return TagKey.create(Registries.ENTITY_TYPE, DragonSurvival.res(path));
    }
}
