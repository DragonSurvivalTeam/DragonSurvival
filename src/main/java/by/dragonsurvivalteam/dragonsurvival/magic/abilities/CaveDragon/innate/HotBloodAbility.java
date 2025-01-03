package by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.innate;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.CaveDragonConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.innate.InnateDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

@Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
        "■ Cave dragons take §cdamage from water§r, snow, rain and other liquids due to their fiery nature.\n",
        "■ The skill «Contrast Shower» and effect «Cave Fire» §7could make your life easier.\n",
        "■ Damage: §c%s points in %ss§r"
})
@Translation(type = Translation.Type.ABILITY, comments = "Hot Blood")
@RegisterDragonAbility
public class HotBloodAbility extends InnateDragonAbility {
    @Override
    public Component getDescription() {
        return Component.translatable(Translation.Type.ABILITY_DESCRIPTION.wrap(getName()), CaveDragonConfig.caveWaterDamage, 0.5);
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public String getName() {
        return "hot_blood";
    }

    @Override
    public AbstractDragonType getDragonType() {
        return DragonTypes.CAVE;
    }

    @Override
    public ResourceLocation[] getSkillTextures() {
        return new ResourceLocation[]{
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/cave/hot_blood_0.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/cave/hot_blood_1.png")
        };
    }

    @Override
    public int getLevel() {
        return ServerConfig.penaltiesEnabled && CaveDragonConfig.caveWaterDamage != 0 ? 1 : 0;
    }

    @Override
    public boolean isDisabled() {
        return super.isDisabled() || !ServerConfig.penaltiesEnabled || CaveDragonConfig.caveWaterDamage == 0;
    }

    @Override
    public int getSortOrder() {
        return 4;
    }
}