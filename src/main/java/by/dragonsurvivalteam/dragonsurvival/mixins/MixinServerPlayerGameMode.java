package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClawToolHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin( ServerPlayerGameMode.class )
public class MixinServerPlayerGameMode{
	@Redirect( method = "destroyBlock(Lnet/minecraft/core/BlockPos;)Z", at = @At( value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;" ) )
	public ItemStack getTools(ServerPlayer instance){
		instance.detectEquipmentUpdates();
		return ClawToolHandler.getDragonHarvestTool(instance);
	}

	/** Some modded ores use override the `canHarvestBlock` method and circumvent the Forge event */
	@Redirect(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;canHarvestBlock(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)Z"))
	private boolean modifiedHarvestCheck(final BlockState blockState, final BlockGetter blockGetter, final BlockPos position, final Player player) {
		boolean originalCheck = blockState.canHarvestBlock(blockGetter, position, player);
		ResourceLocation location = ForgeRegistries.BLOCKS.getKey(blockState.getBlock());

		if (location != null && location.getNamespace().equals("minecraft")) {
			// Don't bother checking vanilla blocks - they should work by default
			return originalCheck;
		}

		if (!ToolUtils.shouldUseDragonTools(player.getItemInHand(InteractionHand.MAIN_HAND))) {
			// If the player had a tool in the hand don't bother checking for dragon tools
			return originalCheck;
		}

		if (!originalCheck && player.level instanceof ServerLevel) {
			DragonStateHandler handler = DragonUtils.getHandler(player);

			if (!handler.isDragon()) {
				return false;
			}

			UUID id = UUID.randomUUID();
			FakePlayer fakePlayer = new FakePlayer((ServerLevel) player.level, new GameProfile(id, id.toString()));

			for (int toolSlot = 0; toolSlot < 4; toolSlot++) {
				ItemStack tool = handler.getClawToolData().getClawsInventory().getItem(toolSlot);

				// No tool or the level of the tool is lower than the harvest level of the dragon
				// TODO :: Do fake items for swords make sense (for harvesting related logic)?
				if (toolSlot != 0 && (tool == ItemStack.EMPTY || (tool.getItem() instanceof TieredItem tieredItem && tieredItem.getTier().getLevel() < handler.getDragonHarvestLevel(toolSlot)))) {
                    // To make the dragon harvest bonus work
					tool = handler.getFakeTool(blockState);
				}

                if (!tool.isCorrectToolForDrops(blockState)) {
                    continue;
                }

				// If certain mods have problems: Could also copy other stuff (inventory, capabilities etc.)
				fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, tool);
				boolean reCheck = blockState.canHarvestBlock(blockGetter, position, fakePlayer);

				if (reCheck) {
					return true;
				}
			}
		}

		return originalCheck;
	}
}