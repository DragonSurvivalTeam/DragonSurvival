package by.dragonsurvivalteam.dragonsurvival.registry.datagen.advancements;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AdvancementBuilder {
    public record NamedCriterion(@Nullable String key, CriterionTriggerInstance criterion) {}

    private final String path;
    private final List<NamedCriterion> criteria = new ArrayList<>();

    private Advancement parent;

    private ItemStack displayItem;
    private ResourceLocation background;
    private FrameType type = FrameType.TASK;

    private int experienceReward;

    private boolean showToast;
    private boolean announceChat;
    private boolean hidden;
    private boolean orRequirements;

    private boolean showDescription = true;

    public AdvancementBuilder(final String path) {
        this.path = path;
    }

    public AdvancementBuilder parent(final Advancement parent) {
        this.parent = parent;
        return this;
    }

    public AdvancementBuilder displayItem(final ItemStack displayItem) {
        this.displayItem = displayItem;
        return this;
    }

    public AdvancementBuilder displayItem(final ItemLike item) {
        this.displayItem = item.asItem().getDefaultInstance();
        return this;
    }

    public AdvancementBuilder background(final ResourceLocation background) {
        this.background = background;
        return this;
    }

    public AdvancementBuilder type(final FrameType type) {
        this.type = type;
        return this;
    }

    public AdvancementBuilder criteria(final String key, final CriterionTriggerInstance criterion) {
        this.criteria.add(new NamedCriterion(key, criterion));
        return this;
    }

    public AdvancementBuilder criteria(final CriterionTriggerInstance criterion) {
        this.criteria.add(new NamedCriterion(null, criterion));
        return this;
    }

    public AdvancementBuilder experienceReward(final int experienceReward) {
        this.experienceReward = experienceReward;
        return this;
    }

    public AdvancementBuilder showToast() {
        this.showToast = true;
        return this;
    }

    public AdvancementBuilder announceChat() {
        this.announceChat = true;
        return this;
    }

    public AdvancementBuilder hidden() {
        this.hidden = true;
        return this;
    }

    public AdvancementBuilder orRequirements() {
        this.orRequirements = true;
        return this;
    }

    public AdvancementBuilder noDescription() {
        this.showDescription = false;
        return this;
    }

    public Advancement build(final Consumer<Advancement> saver) {
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
        if (orRequirements) {
            advancement.requirements(RequirementsStrategy.OR);
        }

        return advancement.save(saver, DragonSurvival.res(path).toString());
    }
}
