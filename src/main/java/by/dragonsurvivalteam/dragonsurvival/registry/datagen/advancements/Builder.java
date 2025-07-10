package by.dragonsurvivalteam.dragonsurvival.registry.datagen.advancements;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Builder {
    public record NamedCriterion(@Nullable String key, Criterion<?> criterion) {}

    private final String path;
    private final List<NamedCriterion> criteria = new ArrayList<>();

    private AdvancementHolder parent;

    private ItemStack displayItem;
    private ResourceLocation background;
    private AdvancementType type = AdvancementType.TASK;

    private int experienceReward;

    private boolean showToast;
    private boolean announceChat;
    private boolean hidden;

    private boolean showDescription = true;

    public Builder(final String path) {
        this.path = path;
    }

    public Builder parent(final AdvancementHolder parent) {
        this.parent = parent;
        return this;
    }

    public Builder displayItem(final ItemStack displayItem) {
        this.displayItem = displayItem;
        return this;
    }

    public Builder displayItem(final ItemLike item) {
        this.displayItem = item.asItem().getDefaultInstance();
        return this;
    }

    public Builder background(final ResourceLocation background) {
        this.background = background;
        return this;
    }

    public Builder type(final AdvancementType type) {
        this.type = type;
        return this;
    }

    public Builder criteria(final String key, final Criterion<?> criterion) {
        this.criteria.add(new NamedCriterion(key, criterion));
        return this;
    }

    public Builder criteria(final Criterion<?> criterion) {
        this.criteria.add(new NamedCriterion(null, criterion));
        return this;
    }

    public Builder experienceReward(final int experienceReward) {
        this.experienceReward = experienceReward;
        return this;
    }

    public Builder showToast() {
        this.showToast = true;
        return this;
    }

    public Builder announceChat() {
        this.announceChat = true;
        return this;
    }

    public Builder hidden() {
        this.hidden = true;
        return this;
    }

    public Builder noDescription() {
        this.showDescription = false;
        return this;
    }

    public AdvancementHolder build(final Consumer<AdvancementHolder> saver, final ExistingFileHelper helper) {
        Advancement.Builder advancement = Advancement.Builder.advancement();

        if (parent != null) {
            advancement.parent(parent);
        }

        advancement.display(
                displayItem,
                Component.translatable(Translation.Type.ADVANCEMENT.wrap(path)),
                showDescription ? Component.translatable(Translation.Type.ADVANCEMENT_DESCRIPTION.wrap(path)) : Component.empty(),
                background,
                type,
                showToast,
                announceChat,
                hidden
        );

        int counter = 0;

        for (NamedCriterion criterion : criteria) {
            if (criterion.key() == null) {
                advancement.addCriterion("criterion_" + counter, criterion.criterion());
                counter++;
            } else {
                advancement.addCriterion(criterion.key(), criterion.criterion());
            }
        }

        if (experienceReward > 0) {
            advancement.rewards(AdvancementRewards.Builder.experience(experienceReward));
        }

        return advancement.save(saver, DragonSurvival.res(path), helper);
    }
}