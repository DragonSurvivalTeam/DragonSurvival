package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Repairable;
import org.jetbrains.annotations.Nullable;

public class ClawsAndTeeth {
    public static @Nullable Identifier constructClawTexture(final Player player) {
        DragonStateHandler handler = DragonStateProvider.getData(player);
        Identifier model = handler.getModel();
        String texturePath = "textures/armor/" + model.getPath() + "/";
        ItemStack clawItem = ClawInventoryData.getData(player).get(handler.species().value().miscResources().clawTextureSlot());

        if (clawItem.isEmpty()) {
            return null;
        }

        return Identifier.fromNamespaceAndPath(model.getNamespace(), getMaterial(texturePath, clawItem) + "dragon_claws.png");
    }

    public static @Nullable Identifier constructTeethTexture(final Player player) {
        Identifier model = DragonStateProvider.getData(player).getModel();
        String texturePath = "textures/armor/" + model.getPath() + "/";
        ItemStack swordItem = ClawInventoryData.getData(player).getContainer().getItem(0);

        if (swordItem.isEmpty()) {
            return null;
        }

        return Identifier.fromNamespaceAndPath(model.getNamespace(), getMaterial(texturePath, swordItem) + "dragon_teeth.png");
    }

    private static String getMaterial(final String texture, final ItemStack itemStack) {
        Repairable repairable = itemStack.get(DataComponents.REPAIRABLE);

        if (repairable != null) {
            if (repairable.isValidRepairItem(new ItemStack(Items.NETHERITE_INGOT))) {
                return texture + "netherite_";
            }

            if (repairable.isValidRepairItem(new ItemStack(Items.DIAMOND))) {
                return texture + "diamond_";
            }

            if (repairable.isValidRepairItem(new ItemStack(Items.IRON_INGOT))) {
                return texture + "iron_";
            }

            if (repairable.isValidRepairItem(new ItemStack(Items.GOLD_INGOT))) {
                return texture + "gold_";
            }

            if (repairable.isValidRepairItem(new ItemStack(Items.COBBLESTONE))) {
                return texture + "stone_";
            }

            if (repairable.isValidRepairItem(new ItemStack(Items.OAK_PLANKS))) {
                return texture + "wooden_";
            }
        }

        return texture + "modded_";
    }
}
