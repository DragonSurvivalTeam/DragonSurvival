package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ResourceLocationWrapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MineBlockUnderLavaTrigger extends DragonCriterionTrigger<MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance> {
    public MineBlockUnderLavaTrigger() {
        super(DragonSurvival.res("mine_block_under_lava"));
    }

    public void trigger(ServerPlayer player, BlockState state) {
        // If no block is specified it will act as any block should trigger the advancement
        this.trigger(player, instance -> instance.block.map(state::is).orElse(true));
    }

    @Override
    protected MineBlockUnderLavaInstance createInstance(
            final JsonObject json,
            final ContextAwarePredicate player,
            final DeserializationContext context
    ) {
        return new MineBlockUnderLavaInstance(
                json.has("player") ? Optional.of(player) : Optional.empty(),
                json.has("block") ? Optional.of(blocks(json.get("block"))) : Optional.empty()
        );
    }

    private static HolderSet<Block> blocks(final JsonElement json) {
        if (json.isJsonArray()) {
            List<Holder<Block>> holders = new ArrayList<>();
            json.getAsJsonArray().forEach(value -> holders.add(block(value.getAsString())));
            return HolderSet.direct(holders);
        }

        String value = json.getAsString();
        if (value.startsWith("#")) {
            if (value.startsWith("#c:")) {
                List<Holder<Block>> holders = ResourceLocationWrapper.getEntries(value, BuiltInRegistries.BLOCK)
                        .stream()
                        .map(id -> block(id.toString()))
                        .toList();
                return HolderSet.direct(holders);
            }
            TagKey<Block> tag = TagKey.create(
                    BuiltInRegistries.BLOCK.key(),
                    new ResourceLocation(value.substring(1))
            );
            Optional<HolderSet.Named<Block>> entries = BuiltInRegistries.BLOCK.getTag(tag);
            return entries.<HolderSet<Block>>map(holders -> holders).orElseGet(HolderSet::direct);
        }
        return HolderSet.direct(block(value));
    }

    private static Holder<Block> block(final String value) {
        ResourceLocation id = new ResourceLocation(value);
        return BuiltInRegistries.BLOCK.getHolder(
                ResourceKey.create(BuiltInRegistries.BLOCK.key(), id)
        ).orElseThrow(() -> new IllegalArgumentException("Unknown block: " + id));
    }

    public static class MineBlockUnderLavaInstance extends DragonCriterionTrigger.Instance {
        private final Optional<HolderSet<Block>> block;

        public MineBlockUnderLavaInstance(
                final Optional<ContextAwarePredicate> player,
                final Optional<HolderSet<Block>> block
        ) {
            super(DragonSurvival.res("mine_block_under_lava"), player);
            this.block = block;
        }

        public Optional<HolderSet<Block>> block() {
            return block;
        }

        @Override
        public JsonObject serializeToJson(final SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            block.ifPresent(value -> json.add("block", serializeBlocks(value)));
            return json;
        }

        private static JsonElement serializeBlocks(final HolderSet<Block> blocks) {
            return blocks.unwrap().map(
                    tag -> new JsonPrimitive("#" + tag.location()),
                    holders -> {
                        if (holders.size() == 1) {
                            return new JsonPrimitive(
                                    holders.get(0).unwrapKey().orElseThrow().location().toString()
                            );
                        }
                        JsonArray values = new JsonArray();
                        holders.forEach(holder -> values.add(
                                holder.unwrapKey().orElseThrow().location().toString()
                        ));
                        return values;
                    }
            );
        }
    }
}
