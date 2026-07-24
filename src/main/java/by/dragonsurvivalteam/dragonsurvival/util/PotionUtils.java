package by.dragonsurvivalteam.dragonsurvival.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.Optional;

import static net.minecraft.core.component.DataComponents.POTION_CONTENTS;

public class PotionUtils {
    // We need to be careful here, as some mods might have items that are instances of
    // PotionItem that do not actually have POTION_CONTENTS, which would crash in previous iterations of this code.
    public static Optional<Potion> getPotion(ItemStack item) {
        PotionContents potionContents = item.get(POTION_CONTENTS);
        if (potionContents == null || potionContents.potion().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(potionContents.potion().get().value());
    }
}
