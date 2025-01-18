package by.dragonsurvivalteam.dragonsurvival.common.items;


import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SpinGrantItem extends TooltipItem {
    @Translation(comments = "You feel empowered with the ability to spin through the air.")
    private static final String SPIN_GRANT_GAINED = Translation.Type.GUI.wrap("spin_grant_gained");

    public SpinGrantItem(final Properties properties, final String key) {
        super(properties, key);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (handler.isDragon() && !handler.spinWasGranted) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable(SPIN_GRANT_GAINED));
                handler.spinWasGranted = true;
                player.getItemInHand(hand).consume(1, player);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1, 0);
                return InteractionResultHolder.success(player.getItemInHand(hand));
            }
        }

        return super.use(level, player, hand);
    }
}