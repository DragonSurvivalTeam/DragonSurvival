package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.RequestClientData;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncSpinStatus;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncSize;
import by.dragonsurvivalteam.dragonsurvival.network.player.SynchronizeDragonCap;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncAltarCooldown;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;


public class DragonCommand{
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher){
		RootCommandNode<CommandSourceStack> rootCommandNode = commandDispatcher.getRoot();
		LiteralCommandNode<CommandSourceStack> dragon = literal("dragon").requires(commandSource -> commandSource.hasPermission(2)).executes(context -> {
			String type = context.getArgument("dragon_type", String.class);
			return runCommand(type, "central", 1, false, context.getSource().getPlayerOrException());
		}).build();

		ArgumentCommandNode<CommandSourceStack, String> dragonType = argument("dragon_type", StringArgumentType.string()).suggests((context, builder) -> {
			SuggestionsBuilder builder1 = null;
			for(String value : DragonTypes.getAllSubtypes()){
				String val = value.toLowerCase();
				builder1 = builder1 == null ? builder.suggest(val) : builder1.suggest(val);
			}
			builder1 = builder1 == null ? builder.suggest("human") : builder1.suggest("human");


			return builder1.buildFuture();
		}).executes(context -> {
			String type = context.getArgument("dragon_type", String.class);
			ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
			return runCommand(type, "central", 1, false, serverPlayer);
		}).build();
		
		ArgumentCommandNode<CommandSourceStack, String> dragonBody = argument("dragon_body", StringArgumentType.string()).suggests((context, builder) -> {
			SuggestionsBuilder sgBuilder = null;
			for (String val : DragonBodies.getBodies()) {
				sgBuilder = sgBuilder == null ? builder.suggest(val) : sgBuilder.suggest(val.toLowerCase());
			}

			return sgBuilder.buildFuture();
			
		}).executes(context -> {
			String type = context.getArgument("dragon_type", String.class);
			String body = context.getArgument("dragon_body", String.class);
			ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
			return runCommand(type, body, 1, false, serverPlayer);
		}).build();

		ArgumentCommandNode<CommandSourceStack, Integer> dragonStage = argument("dragon_stage", IntegerArgumentType.integer(1, 4)).suggests((context, builder) -> builder.suggest(1).suggest(2).suggest(3).suggest(4).buildFuture()).executes(context -> {
			String type = context.getArgument("dragon_type", String.class);
			String body = context.getArgument("dragon_body", String.class);
			int stage = context.getArgument("dragon_stage", Integer.TYPE);
			ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
			return runCommand(type, body, stage, false, serverPlayer);
		}).build();

		ArgumentCommandNode<CommandSourceStack, Boolean> giveFlight = argument("flight", BoolArgumentType.bool()).executes(context -> {
			String type = context.getArgument("dragon_type", String.class);
			String body = context.getArgument("dragon_body", String.class);
			int stage = context.getArgument("dragon_stage", Integer.TYPE);
			boolean flight = context.getArgument("flight", Boolean.TYPE);
			ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
			return runCommand(type, body, stage, flight, serverPlayer);
		}).build();

		ArgumentCommandNode<CommandSourceStack, EntitySelector> target = argument("target", EntityArgument.players()).executes(context -> {
			String type = context.getArgument("dragon_type", String.class);
			String body = context.getArgument("dragon_body", String.class);
			int stage = context.getArgument("dragon_stage", Integer.TYPE);
			boolean flight = context.getArgument("flight", Boolean.TYPE);
			EntitySelector selector = context.getArgument("target", EntitySelector.class);
			List<ServerPlayer> serverPlayers = selector.findPlayers(context.getSource());
			serverPlayers.forEach(player -> runCommand(type, body, stage, flight, player));
			return 1;
		}).build();

		rootCommandNode.addChild(dragon);
		dragon.addChild(dragonType);
		dragonType.addChild(dragonBody);
		dragonBody.addChild(dragonStage);
		dragonStage.addChild(giveFlight);
		giveFlight.addChild(target);
	}

	private static int runCommand(String type, String body, int stage, boolean flight, ServerPlayer player){
		DragonStateHandler cap = DragonUtils.getHandler(player);
		AbstractDragonType dragonType1 = type.equalsIgnoreCase("human") ? null : DragonTypes.getStaticSubtype(type);
		AbstractDragonBody dragonBody = body.equalsIgnoreCase("none") ? null : DragonBodies.getStatic(body);

		if(dragonType1 == null && cap.getType() != null){
			reInsertClawTools(player, cap);
		}

		cap.setType(dragonType1, player);
		cap.setBody(dragonBody, player);
		cap.setHasFlight(flight);
		cap.getMovementData().spinLearned = flight;
		DragonLevel dragonLevel = DragonLevel.values()[Mth.clamp(stage - 1, 0, DragonLevel.values().length-1)];
		float size = stage == 4 ? 40f : dragonLevel.size;
		cap.setSize(size, player);
		cap.setPassengerId(DragonStateHandler.NO_ENTITY);
		cap.growing = true;

		NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),new SyncAltarCooldown(player.getId(), Functions.secondsToTicks(ServerConfig.altarUsageCooldown)));
		NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),new SynchronizeDragonCap(player.getId(), cap.isHiding(), cap.getType(), cap.getBody(), cap.getSize(), cap.hasFlight(), 0));
		NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),new SyncSpinStatus(player.getId(), cap.getMovementData().spinAttack, cap.getMovementData().spinCooldown, cap.getMovementData().spinLearned));
		NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new SyncSize(player.getId(), size));
		NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new RequestClientData(cap.getType(), cap.getBody(), cap.getLevel()));
		player.refreshDimensions();
		return 1;
	}

	public static void reInsertClawTools(Player player, DragonStateHandler dragonStateHandler){
		for(int i = 0; i < 4; i++){
			ItemStack stack = dragonStateHandler.getClawToolData().getClawsInventory().getItem(i);

			if(player instanceof ServerPlayer serverPlayer){
				if(!serverPlayer.addItem(stack)){
					serverPlayer.level.addFreshEntity(new ItemEntity(serverPlayer.level, serverPlayer.position().x, serverPlayer.position().y, serverPlayer.position().z, stack));
				}
			}
		}

		dragonStateHandler.getClawToolData().getClawsInventory().clearContent();
	}
}