package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public enum ModifierType implements StringRepresentable {
    DRAGON_TYPE("type"),
    DRAGON_BODY("body"),
    DRAGON_STAGE("stage"),
    CUSTOM("custom");

    public static final Codec<ModifierType> CODEC = StringRepresentable.fromEnum(ModifierType::values);

    private static final RandomSource RANDOM = RandomSource.create();
    private final String path;

    ModifierType(final String path) {
        this.path = DragonSurvival.MODID + "/" + path + "/";
    }

    public String path() {
        return path;
    }

    public ResourceLocation randomId(final Holder<Attribute> attribute, final AttributeModifier.Operation operation) {
        String attributeId = attribute.getRegisteredName().replace(":", ".");
        int hash = Objects.hash(String.valueOf(RANDOM.nextInt(100_000)), operation.getSerializedName());
        return DragonSurvival.res(path() + hash + "/" + attributeId);
    }

    @Override
    public @NotNull String getSerializedName() {
        return path();
    }
}
