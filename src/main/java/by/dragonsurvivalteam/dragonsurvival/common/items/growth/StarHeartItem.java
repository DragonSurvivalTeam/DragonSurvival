package by.dragonsurvivalteam.dragonsurvival.common.items.growth;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.items.TooltipItem;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class StarHeartItem extends TooltipItem {
    @Translation(comments = "Star heart state is §cinactive§r")
    private static final String INACTIVE = Translation.Type.GUI.wrap("message.star_heart_inactive");

    @Translation(comments = "Star heart state is §2active§r")
    private static final String ACTIVE = Translation.Type.GUI.wrap("message.star_heart_active");

    public StarHeartItem(final Properties properties, final String key) {
        super(properties, key);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            DSAdvancementTriggers.USE_STAR_HEART.get().trigger(serverPlayer);
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (handler.isDragon()) {
                String message;

                if (handler.starHeartState == State.INACTIVE) {
                    handler.starHeartState = State.ACTIVE;
                    message = ACTIVE;
                } else {
                    handler.starHeartState = State.INACTIVE;
                    message = INACTIVE;
                }

                player.sendSystemMessage(Component.translatable(message));
                return InteractionResultHolder.success(player.getItemInHand(hand));
            }
        }

        return super.use(level, player, hand);
    }

    public enum State implements StringRepresentable {
        INACTIVE("inactive"),
        ACTIVE("active");

        public static final Codec<State> CODEC = StringRepresentable.fromEnum(State::values);

        private final String name;

        State(final String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}