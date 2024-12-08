package by.dragonsurvivalteam.dragonsurvival.config;


import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class ConfigUtils{

	public static boolean containsEntity(List<? extends String> values, Entity entity){
		String type = entity.getEncodeId();
		return values.contains(type) || values.contains("entity:" + type);
	}

	public static List<Item> parseConfigItemList(List<? extends String> values){
		List<Item> result = new ArrayList<>();

		for(String entry : values.stream().map(s -> s.replace("tag:", "").replace("item:", "")).toList()){
			String[] spEntry = entry.split(":");
			String ent = spEntry[0] + ":" + spEntry[1];
			if(ResourceLocation.isValidResourceLocation(ent)){
				ResourceLocation rlEntry = new ResourceLocation(ent);
				TagKey<Item> tagKey = TagKey.create(Registry.ITEM_REGISTRY, rlEntry);
				result.addAll(ForgeRegistries.ITEMS.tags().getTag(tagKey).stream().toList());
				result.add(ForgeRegistries.ITEMS.getValue(rlEntry));
			}
		}

		return result;
	}

	public static List<Block> parseConfigBlockList(List<? extends String> values){
		List<Block> result = new ArrayList<>();

		for(String entry : values.stream().map(s -> s.replace("tag:", "").replace("block:", "")).toList()){
			String[] spEntry = entry.split(":");
			String ent = spEntry[0] + ":" + spEntry[1];
			if(ResourceLocation.isValidResourceLocation(ent)){
				ResourceLocation rlEntry = new ResourceLocation(ent);
				TagKey<Block> tagKey = TagKey.create(Registry.BLOCK_REGISTRY, rlEntry);
				result.addAll(ForgeRegistries.BLOCKS.tags().getTag(tagKey).stream().toList());
				Block block = ForgeRegistries.BLOCKS.getValue(rlEntry);
				if(block != Blocks.AIR){
					result.add(block);
				}
			}
		}

		return result;
	}
}