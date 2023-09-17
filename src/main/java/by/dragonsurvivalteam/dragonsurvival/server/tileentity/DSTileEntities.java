package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Objects;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

@Mod.EventBusSubscriber( bus = Mod.EventBusSubscriber.Bus.MOD )
public class DSTileEntities{
	public static BlockEntityType<SourceOfMagicTileEntity> sourceOfMagicTileEntity;
	public static BlockEntityType<SourceOfMagicPlaceholder> sourceOfMagicPlaceholder;
	public static BlockEntityType<HelmetTileEntity> helmetTile;
	public static BlockEntityType<DragonBeaconTileEntity> dragonBeacon;

	@SubscribeEvent
	public static void register(final RegisterEvent event) {
		if (!Objects.equals(event.getForgeRegistry(), ForgeRegistries.BLOCK_ENTITY_TYPES)) {
			return;
		}

		sourceOfMagicTileEntity = BlockEntityType.Builder.of(SourceOfMagicTileEntity::new, DSBlocks.caveSourceOfMagic, DSBlocks.seaSourceOfMagic, DSBlocks.forestSourceOfMagic).build(null);
		event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, new ResourceLocation(MODID, "dragon_nest"), () -> sourceOfMagicTileEntity);

		sourceOfMagicPlaceholder = BlockEntityType.Builder.of(SourceOfMagicPlaceholder::new, DSBlocks.forestSourceOfMagic, DSBlocks.seaSourceOfMagic, DSBlocks.caveSourceOfMagic).build(null);
		event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, new ResourceLocation(MODID, "placeholder"), () -> sourceOfMagicPlaceholder);

		helmetTile = BlockEntityType.Builder.of(HelmetTileEntity::new, DSBlocks.helmet1, DSBlocks.helmet2, DSBlocks.helmet3).build(null);
		event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, new ResourceLocation(MODID, "knight_helmet"), () -> helmetTile);

		dragonBeacon = BlockEntityType.Builder.of(DragonBeaconTileEntity::new, DSBlocks.dragonBeacon, DSBlocks.peaceDragonBeacon, DSBlocks.magicDragonBeacon, DSBlocks.fireDragonBeacon).build(null);
		event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, new ResourceLocation(MODID, "dragon_beacon"), () -> dragonBeacon);
	}
}