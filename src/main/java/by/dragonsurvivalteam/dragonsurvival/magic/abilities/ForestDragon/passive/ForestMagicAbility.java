package by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.passive;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.passive.MagicAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.resources.ResourceLocation;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

@Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
        "■ Magic points (Mana) are used for dragon magic. Restores under direct sunlight and on grass.\n",
        "■ Current amount of §2%s§r mana:",
        " - §2%s§r from «Forest Magic»",
        " - §2%s§r from body type",
        " - §2%s§r from experience"
})
@Translation(type = Translation.Type.ABILITY, comments = "Forest Magic")
@RegisterDragonAbility
public class ForestMagicAbility extends MagicAbility {
    @Translation(key = "forest_magic", type = Translation.Type.CONFIGURATION, comments = "Enable / Disable the forest magic ability")
    @ConfigOption(side = ConfigSide.SERVER, category = {"forest_dragon", "magic", "abilities", "passive"}, key = "forest_magic")
    public static Boolean forestMagic = true;

    @Override
    public String getName() {
        return "forest_magic";
    }

    @Override
    public int getSortOrder() {
        return 1;
    }

    @Override
    public AbstractDragonType getDragonType() {
        return DragonTypes.FOREST;
    }

    @Override
    public ResourceLocation[] getSkillTextures() {
        return new ResourceLocation[]{
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_0.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_1.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_2.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_3.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_4.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_5.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_6.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_7.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_8.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_9.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_10.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_11.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_12.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_13.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_14.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_15.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_16.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_17.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_18.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_19.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_magic_20.png")
        };
    }

    @Override
    public boolean isDisabled() {
        return super.isDisabled() || !forestMagic;
    }
}