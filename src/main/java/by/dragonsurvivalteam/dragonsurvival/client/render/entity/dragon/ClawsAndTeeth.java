package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import org.jetbrains.annotations.Nullable;

public class ClawsAndTeeth {
    public static @Nullable ResourceLocation constructClawTexture(final Player player) {
        DragonStateHandler handler = DragonStateProvider.getData(player);

        ResourceLocation model = handler.getModel();
        String texturePath = "textures/armor/" + model.getPath() + "/";

        ItemStack clawItem = ClawInventoryData.getData(player).get(handler.species().value().miscResources().clawTextureSlot());

        if (!clawItem.isEmpty()) {
            texturePath = getMaterial(texturePath, clawItem);
        } else {
            return null;
        }

        return ResourceLocation.fromNamespaceAndPath(model.getNamespace(), texturePath + "dragon_claws.png");
    }

    public static @Nullable ResourceLocation constructTeethTexture(final Player player) {
        ResourceLocation model = DragonStateProvider.getData(player).getModel();

        String texturePath = "textures/armor/" + model.getPath() + "/";
        ItemStack swordItem = ClawInventoryData.getData(player).getContainer().getItem(0);

        if (!swordItem.isEmpty()) {
            texturePath = getMaterial(texturePath, swordItem);
        } else {
            return null;
        }

        return ResourceLocation.fromNamespaceAndPath(model.getNamespace(), texturePath + "dragon_teeth.png");
    }

    private static String getMaterial(String texture, ItemStack clawItem) {
        if (clawItem.getItem() instanceof TieredItem item) {
            Tier tier = item.getTier();

            switch (tier) {
                case Tiers.NETHERITE -> texture += "netherite_";
                case Tiers.DIAMOND -> texture += "diamond_";
                case Tiers.IRON -> texture += "iron_";
                case Tiers.GOLD -> texture += "gold_";
                case Tiers.STONE -> texture += "stone_";
                case Tiers.WOOD -> texture += "wooden_";
                default -> texture += "modded_";
            }

            return texture;
        }

        return texture + "modded_";
    }
}